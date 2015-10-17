package dynks.test.it;

import dynks.Frontend;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static java.lang.Integer.valueOf;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Servlet used for testing eviction of entries through REST api. Although usually eviction of regions will be
 * used by interacting directly withing classloader using static access to Fronted class for integration testing
 * purposes we will use http protocol cause it is much easier for that purpose.
 *
 * @author jszczepankiewicz
 * @since 2015-10-14
 */
public class PurgingServlet extends HttpServlet {

  private static final Logger LOG = getLogger(PurgingServlet.class);

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    if (paramNotSet("region", req, resp)) {
      return;
    }

    String region = req.getParameter("region");
    String param = req.getParameter("maxEntriesDeletedInOneBatch");

    Frontend frontend = Frontend.get();

    long evicted;
    int maxEntriesDeletedInOneBatch;

    if (param != null) {
      maxEntriesDeletedInOneBatch = valueOf(param);
      evicted = frontend.evictRegion(region, maxEntriesDeletedInOneBatch);
    } else {
      maxEntriesDeletedInOneBatch = frontend.getDefaultMaxEntriesDeletedInOneBatch();
      evicted = frontend.evictRegion(region);
    }

    LOG.info("Evicted {} entries from region: '{}' with maxEntriesDeletedInOneBatch: {}", evicted, region, maxEntriesDeletedInOneBatch);

    resp.setContentType("text/plain; charset=utf-8");
    resp.setCharacterEncoding("utf-8");

    PrintWriter out = resp.getWriter();
    out.write(format("Evicted %d entries from region: %s with maxEntriesDeletedInOneBatch: %d", evicted, region, maxEntriesDeletedInOneBatch));
    out.close();
  }


  private boolean paramNotSet(String name, HttpServletRequest req, HttpServletResponse resp) throws IOException {

    if (req.getParameter(name) == null || req.getParameter(name).trim().length() == 0) {
      resp.setStatus(400);
      PrintWriter out = resp.getWriter();
      out.write(format("Parameter: '%s' not set. I Refuse to Go Another Step!", name));
      out.close();
      return true;
    }

    return false;
  }
}
