package dynks.cache;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static dynks.cache.TestValues.UTF8;
import static dynks.cache.TestValues.UTF8_JSON;
import static dynks.cache.test.DynksAssertions.assertThat;
import static org.junit.rules.ExpectedException.none;

/**
 * Created by jszczepankiewicz on 2015-04-11.
 */
public class EntryTest {

    @Rule
    public ExpectedException thrown = none();

    @Test
    public void throwNPEonNulledContent(){

        //  then
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Content to put into cache should not be null");

        //  when
        new Entry(null, "sometag", UTF8_JSON, UTF8);
    }

    @Test
    public void throwNPEonNulledEtag(){

        //  then
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Etag to put into cache should not be null");

        //  when
        new Entry("sometag", null, UTF8_JSON, UTF8);
    }

    @Test
    public void initializeEntryWithContentAndEtag(){

        //  given
        final String content = "[]";
        final String etag = "980";

        //  when
        Entry entry = new Entry(content, etag, UTF8_JSON, UTF8);

        //  then
        assertThat(entry).hasContent(content).hasEtag(etag);
    }

}