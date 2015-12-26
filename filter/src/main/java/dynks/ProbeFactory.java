package dynks;

import org.slf4j.Logger;

import static java.lang.System.nanoTime;

/**
 * Retrieves probes for building comprehensive log communicate. Creates/retrieves
 * one of the two probes:
 * <ul>
 * <li>NOP probe will ignore any operations invoked on probe</li>
 * <li>debug probe which will cummulate perfomance tracking and log instruction and
 * print that to provided logger using debug level</li>
 * </ul>
 * Factory returns NOP probe if provided logger will have <pre>isDebugEnabled()</pre>
 * equals to <pre>false</pre>.
 *
 * @author jszczepankiewicz
 * @since 2015-09-18
 */
public class ProbeFactory {

  private static final Probe NOP = new NOPProbe();

  public static Probe getProbe(Logger log) {
    if (log.isDebugEnabled()) {
      return new DebugProbe(log);
    } else {
      return NOP;
    }
  }

  public interface Probe {

    /**
     * starts tracking time for given category
     */
    void start(char category);

    /**
     * stops tracking time
     */
    void stop();

    /**
     * stops tracking time assuming there was no stop. It may be used to
     * interleave with regular start category.
     *
     * @param nanoStart
     */
    void stop(char category, long nanoStart);

    /**
     * adds debug line
     */
    void log(String activity);

    void log(String... activities);

    void log(int value);

    void log(long value);

    /**
     * prints logs
     */
    void flushLog();
  }

  /**
   * Lazy b...d does nothing saving air on planet Earth.
   */
  static class NOPProbe implements Probe {
    @Override
    public void start(char category) {
    }

    @Override
    public void stop() {
    }

    @Override
    public void stop(char category, long nanoStart) {
    }

    @Override
    public void log(String activity) {
    }

    @Override
    public void log(String... activities) {
    }

    @Override
    public void flushLog() {
    }

    @Override
    public void log(int value) {
    }

    @Override
    public void log(long value) {
    }
  }

  static class DebugProbe implements Probe {

    private final StringBuilder activityBuffer = new StringBuilder(120);
    private final StringBuilder trackingBuffer = new StringBuilder(170);

    private final Logger log;
    private long nanoStart;

    DebugProbe(Logger log) {
      this.log = log;
    }

    @Override
    public void start(char category) {

      if (nanoStart > 0) {
        trackingBuffer.append(' ');
      }

      trackingBuffer.append(category);
      trackingBuffer.append(':');
      nanoStart = nanoTime();
    }

    @Override
    public void stop(char category, long nanoStart) {

      if (this.nanoStart > 0) {
        trackingBuffer.append(' ');
      }

      this.nanoStart = nanoStart;

      trackingBuffer.append(category);
      trackingBuffer.append(':');
      trackingBuffer.append((nanoTime() - nanoStart) / 1000);
    }

    @Override
    public void stop() {
      trackingBuffer.append((nanoTime() - nanoStart) / 1000);
    }

    @Override
    public void log(String activity) {
      activityBuffer.append(activity);
      activityBuffer.append('|');
    }

    @Override
    public void log(String... activities) {
      for (String activity : activities) {
        activityBuffer.append(activity);
        activityBuffer.append('|');
      }
    }

    @Override
    public void flushLog() {
      trackingBuffer.append(" µs|");
      trackingBuffer.append(activityBuffer.toString());
      log.debug(trackingBuffer.toString());
    }

    @Override
    public void log(int value) {
      log(String.valueOf(value));
    }

    @Override
    public void log(long value) {
      log(String.valueOf(value));
    }
  }
}
