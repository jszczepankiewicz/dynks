package dynks;


import dynks.ProbeFactory.DebugProbe;
import dynks.ProbeFactory.NOPProbe;
import dynks.ProbeFactory.Probe;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import static dynks.ProbeFactory.getProbe;
import static dynks.cache.test.DynksAssertions.assertThat;
import static java.lang.System.nanoTime;
import static java.lang.Thread.sleep;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

/**
 * @author jszczepankiewicz
 * @since 2015-09-19
 */
public class DebugProbeTest {

    @Test
    public void nopLoggerShouldBeUsedForNonDebugLogger(){

        //  when
        Probe writer = getProbe(debugDisabledLogger());

        //  then
        assertThat(writer).isInstanceOf(NOPProbe.class);
    }

    @Test
    public void regularLoggerShouldBeCreatedIfDebugLevelIsOn(){

        //  when
        Probe writer = getProbe(debugEnabledLogger());

        //  then
        assertThat(writer).isInstanceOf(DebugProbe.class);
    }

    @Test
    public void shouldNotFailWithNoActionOnProbe(){

        //  given
        Logger log = debugEnabledLogger();
        Probe probe = getProbe(log);

        //  when
        probe.flushLog();

        //  then
        //  there is no exception
    }

    @Test
    public void shouldNotFailWithOnlyPerfomanceLogging(){

        //  given
        Logger log = debugEnabledLogger();
        Probe probe = getProbe(log);
        probe.start('c');
        probe.stop();

        //  when
        probe.flushLog();

        //  then
        //  there is no exception
    }

    @Test
    public void shouldNotFailWithOnlyLogEntries(){

        //  given
        Logger log = debugEnabledLogger();
        Probe probe = getProbe(log);
        probe.log("sssssomething");

        //  when
        probe.flushLog();

        //  then
        //  there is no exception
    }

    @Test
    public void timeShouldBeLoggedForStopWithArguments() throws InterruptedException{

        //  given
        Logger log = debugEnabledLogger();
        Probe probe = getProbe(log);

        //  when
        probe.log("starting");
        long nanoStart = nanoTime();
        sleep(2);
        probe.stop('s', nanoStart);
        probe.log("executing");
        nanoStart = nanoTime();
        sleep(0, 150);
        probe.stop('e', nanoStart);
        probe.log("finishing");
      probe.log("a1", "a2");

        //  then
        probe.flushLog();
      assertThat(getLoggedValue(log)).matches("s:\\d+ e:\\d+ µs\\|starting\\|executing\\|finishing\\|a1\\|a2\\|");
    }

    @Test
    public void timeAndActivitiesShouldBeLoggedForDebugLogger() throws InterruptedException {

        //  given
        Logger log = debugEnabledLogger();
        Probe probe = getProbe(log);

        //  when
        probe.log("starting");
        probe.start('s');
        sleep(2);
        probe.stop();
        probe.log("executing");
        probe.start('e');
        sleep(0, 150);
        probe.stop();
        probe.log("finishing");

        //  then
        probe.flushLog();
        assertThat(getLoggedValue(log)).matches("s:\\d+ e:\\d+ µs\\|starting\\|executing\\|finishing\\|");

    }

    private String getLoggedValue(Logger mock){
        ArgumentCaptor<String> argument = forClass(String.class);
        verify(mock).debug(argument.capture());
        return argument.getValue();
    }

    private Logger debugDisabledLogger(){
        Logger log = mock(Logger.class);
        when(log.isDebugEnabled()).thenReturn(false);
        return log;
    }

    private Logger debugEnabledLogger(){
        Logger log = mock(Logger.class);
        when(log.isDebugEnabled()).thenReturn(true);
        return log;
    }

}