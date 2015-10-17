package dynks.cache;

import javax.servlet.http.HttpServletRequest;

/**
 * Key retrieving strategy that applies namespace in in form:
 * <pre>namespace:uri</pre> which can later be saved in repository.
 *
 * @author jszczepankiewicz
 * @since 2015-04-17
 */
public class NamespacedURIKeyStrategy implements KeyStrategy {

  public static final String EMPTY_NAMESPACE = "";
  private final String namespace;

  public NamespacedURIKeyStrategy(String namespace) {

    if (namespace == null) {
      throw new NullPointerException("Namespace should not be null");
    }

    this.namespace = namespace;
  }

  public static NamespacedURIKeyStrategy keyStrategyWithEmptyNamespace() {
    return new NamespacedURIKeyStrategy(EMPTY_NAMESPACE);
  }

  @Override
  public String wildcardKeyFor(CacheRegion region) {
    StringBuilder builder = new StringBuilder(3 + namespace.length() + region.getId().length());
    builder.append(namespace);
    builder.append(':');
    builder.append(region.getId());
    builder.append(":*");
    return builder.toString();
  }

  @Override
  public String keyFor(HttpServletRequest request, CacheRegion region) {

    String uri = request.getRequestURI();
    //  this should be pooled probably
    StringBuilder builder = new StringBuilder(uri.length() + 2 + namespace.length() + region.getId().length());
    builder.append(namespace);
    builder.append(':');
    builder.append(region.getId());
    builder.append(':');
    builder.append(uri);
    return builder.toString();
  }

  @Override
  public int hashCode() {
    return this.getClass().toGenericString().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return true;
  }
}
