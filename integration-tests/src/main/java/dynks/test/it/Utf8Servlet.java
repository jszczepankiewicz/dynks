package dynks.test.it;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import static java.util.Locale.ENGLISH;
import static java.util.TimeZone.getTimeZone;
import static org.apache.commons.lang3.time.FastDateFormat.getInstance;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Servlet responding with timestamp of generation and requesting URI name.
 * Supports all http method types through implementation of <pre>service</pre>
 * method.
 *
 * @author jszczepankiewicz
 * @since 2015-09-06
 */
public class Utf8Servlet extends HttpServlet {

  private static final Logger LOG = getLogger(Utf8Servlet.class);

  private static final FastDateFormat time = getInstance("yyyy-MM-dd HH:mm:ss.SSS", getTimeZone("UTC"), ENGLISH);

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String uri = req.getRequestURI();
    String stamp = time.format(Calendar.getInstance().getTime());
    LOG.info("Generating response for method: {}, URI: {}, stamp: {}", req.getMethod(), uri, stamp);


    resp.setContentType("application/json; charset=utf-8");
    resp.setCharacterEncoding("utf-8");

    PrintWriter out = resp.getWriter();
    out.write(uri);
    out.write('\n');
    out.write(stamp);
    out.write('\n');
    out.write("ąśćźżęłóĄŚĆŻŹĘŁÓ");
    out.close();
  }
}
