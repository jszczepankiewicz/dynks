package dynks.cache.test.integration;

import org.junit.Test;

import static dynks.cache.test.DynksAssertions.assertThat;

/**
 * @author jszczepankiewicz
 * @since 2015-09-05
 */
public class ServerPayloadConditionTest {

    private ServerPayloadCondition condition = new ServerPayloadCondition();

    @Test
    public void shouldMatchPayloadFromServer(){

        //  when
        boolean matches1 = condition.matches(of("shouldMatchPayloadFromServer\n2015-09-05 08:16:30.822\nąśćźżęłóĄŚĆŻŹĘŁÓ\n"));
        boolean matches2 = condition.matches(of("shouldMatchPayloadFromServer\n2015-09-05 08:16:30.822\nąśćźżęłóĄŚĆŻŹĘŁÓ"));
        boolean matches3 = condition.matches(of("\nshouldMatchPayloadFromServer\n2015-09-05 08:16:30.822\nąśćźżęłóĄŚĆŻŹĘŁÓ"));

        //  then
        assertThat(matches1).isTrue();
        assertThat(matches2).isTrue();
        assertThat(matches3).isTrue();
    }

    @Test
    public void shouldNotMatchNonCompliantDateFormat(){

        //  when
        boolean matches1 = condition.matches(of("shouldNotMatchNonCompliantDateFormat\n2015-09-05 08:16:30\nąśćźżęłóĄŚĆŻŹĘŁÓ\n"));

        //  then
        assertThat(matches1).isFalse();
    }

    @Test
    public void shouldNotMatchIfUriEmpty(){

        //  when
        boolean matches1 = condition.matches(of("\n2015-09-05 08:16:30.822\nąśćźżęłóĄŚĆŻŹĘŁÓ\n"));
        boolean matches2 = condition.matches(of("\n2015-09-05 08:16:30.822\nąśćźżęłóĄŚĆŻŹĘŁÓ"));
        boolean matches3 = condition.matches(of("2015-09-05 08:16:30.822\nąśćźżęłóĄŚĆŻŹĘŁÓ"));

        //  then
        assertThat(matches1).isFalse();
        assertThat(matches2).isFalse();
        assertThat(matches3).isFalse();
    }

    @Test
    public void shouldNotMatchIfTimestampNotPresent(){

        //  when
        boolean matches1 = condition.matches(of("shouldNotMatchIfTimestampNotPresent\n"));
        boolean matches2 = condition.matches(of("shouldNotMatchIfTimestampNotPresent\n\n"));

        //  then
        assertThat(matches1).isFalse();
        assertThat(matches2).isFalse();
    }

    @Test
    public void shouldNotMatchNulledServerResponse(){

        //  when
        boolean matches = condition.matches(null);

        //  then
        assertThat(matches).isFalse();
    }

    @Test
    public void shouldNotMatchNulledPayload(){

        //  when
        boolean matches = condition.matches(of(null));

        //  then
        assertThat(matches).isFalse();
    }


    @Test
    public void shouldNotMatchEmptyPayload(){

        //  when
        boolean matches1 = condition.matches(of(" "));
        boolean matches2 = condition.matches(of(""));

        //  then
        assertThat(matches1).isFalse();
        assertThat(matches2).isFalse();

    }

    private ServerResponse of(String payload){
        return new ServerResponse(payload, 0, null, null);
    }

}