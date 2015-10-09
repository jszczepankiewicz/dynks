package dynks.cache.test.integration;

import org.assertj.core.api.Condition;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.UNIX_LINES;
import static java.util.regex.Pattern.compile;

/**
 * Matches response from servlet that always has following structure:
 * [relative uri invoked]
 * [yyyy-MM-dd HH:mm:ss.SSS]
 * @author jszczepankiewicz
 * @since 2015-09-05
 */
public class ServerPayloadCondition extends Condition<ServerResponse> {

    private static final Pattern PATTERN = compile(".+\\n\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\nąśćźżęłóĄŚĆŻŹĘŁÓ", UNIX_LINES);

    @Override
    public boolean matches(ServerResponse response) {

        if(response == null) {
            return false;
        }

        if(response.getPayload() == null){
            return false;
        }

        return PATTERN.matcher(response.getPayload().trim()).matches();
    }
}
