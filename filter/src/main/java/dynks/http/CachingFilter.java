package dynks.http;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import dynks.Frontend;
import dynks.ProbeFactory.Probe;
import dynks.cache.*;
import dynks.jmx.JmxServer;
import dynks.redis.RedisCacheRepositoryConfigBuilder;
import org.slf4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static dynks.ProbeFactory.getProbe;
import static dynks.cache.CacheRegion.Cacheability.PASSTHROUGH;
import static dynks.http.ETag.*;
import static dynks.http.HttpMethod.GET;
import static dynks.jmx.Configuration.HARDENED_MODE;
import static java.lang.System.nanoTime;
import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Filter for acting as web cache layer using ETag cache negotiation.
 *
 * @author jszczepankiewicz
 * @since 2015-04-14
 */
public class CachingFilter implements Filter {

  private static final Logger LOG = getLogger(CachingFilter.class);

  private CacheRepository cache;
  private CacheByURIRegionRepository policy;
  private boolean hardenedModeEnabled;
  private JmxServer jmxServer;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    Config config = ConfigFactory.load("dynks");
    hardenedModeEnabled = config.getBoolean(HARDENED_MODE);
    cache = RedisCacheRepositoryConfigBuilder.build(config);
    policy = ResponseCacheByURIBuilder.build(config);
    Frontend.initialize(cache, policy);
    jmxServer = new JmxServer(config);
    jmxServer.start();
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    if (GET.equalsIgnoreCase(request.getMethod())) {

      final Probe probe = getProbe(LOG);
      final long nanoStart = nanoTime();

      try {

        probe.log(request.getRequestURI());

        CacheRegion cacheRegion = policy.getfor(request);

        if (cacheRegion.getCacheability() == PASSTHROUGH) {
          doFiltering(chain, probe, req, res);
          probe.log("passthrough");
          return;
        }

        String key = cacheRegion.getKeyStrategy().keyFor(request, cacheRegion);
        String requestEtag = getFrom(request);
        probe.start('f');
        CacheQueryResult result = cache.fetchIfChanged(key, requestEtag);
        probe.stop();

        if (result.isUpsertNeeded()) {
          probe.log("upsert");
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          CachedResponseWrapper wrappedResponse = new CachedResponseWrapper(response, baos);
          //  invoking "production" of content from underlying resources
          doFiltering(chain, probe, req, wrappedResponse);
          //  caching response for future use
          String encoding = wrappedResponse.getCharacterEncoding();
          String generated = baos.toString(encoding);
          probe.log(baos.size());
          String etag = of(generated, new StringBuilder(SIZEOF_ETAG));
          probe.log(etag);
          probe.start('u');
          cache.upsert(key, generated, etag, wrappedResponse.getContentType(), encoding, cacheRegion);
          probe.stop();
          writeIn(response, etag);
          //  now we need to copy from generated stream into original stream
          res.getOutputStream().write(baos.toByteArray());
          res.getOutputStream().flush();

          return;
        } else {
          if (result.getStoredEtag() == null) {
            //  client already has latest version
            response.setStatus(SC_NOT_MODIFIED);
            probe.log("not-changed");
            return;
          } else {
            //  client has old version or access this for first time, we need to sent him latest one
            res.setCharacterEncoding(result.getEncoding());
            res.setContentType(result.getContentType());

            //  writing to response should be done AFTER encoding was set
            writeIn(response, result.getStoredEtag());
            response.setStatus(SC_OK);
            res.getOutputStream().write(result.getPayload().getBytes(result.getEncoding()));

            res.getOutputStream().flush();
            probe.log("new-or-changed");
            return;
          }
        }
      } catch (CacheRepositoryException e) {
        onRepositoryError(e, request, response, chain, probe);
      } finally {
        probe.stop('a', nanoStart);
        probe.flushLog();
      }
    } else {
      //  passthrough anything else than GET without checking & saving in cache, not even logging perf
      chain.doFilter(req, res);
      return;
    }
  }

  private void onRepositoryError(CacheRepositoryException e, HttpServletRequest request, HttpServletResponse response, FilterChain chain, Probe probe) throws ServletException, IOException {

    if (hardenedModeEnabled) {
      probe.log("passthrough-on-error");
      LOG.warn("Passthrough-on-error {}:{}", e.getCause().getClass().toString(), e.getCause().getMessage());
      chain.doFilter(request, response);
    } else {
      LOG.error("CacheRepositoryException occurred and hardened mode is disabled", e);
      throw new ServletException(e);
    }
  }

  private void doFiltering(FilterChain chain, Probe probe, ServletRequest req, ServletResponse res) throws IOException, ServletException {
    probe.start('g');
    chain.doFilter(req, res);
    probe.stop();
  }

  @Override
  public void destroy() {

    if (cache != null) {
      cache.dispose();
    }
  }
}
