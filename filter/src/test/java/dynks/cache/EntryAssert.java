package dynks.cache;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;

/**
 * Created by jszczepankiewicz on 2015-04-11.
 */
public class EntryAssert extends AbstractAssert<EntryAssert, Entry> {

    /**
     * Creates a new <code>{@link EntryAssert}</code> to make assertions on actual Entry.
     * @param actual the Entry we want to make assertions on.
     */
    public EntryAssert(Entry actual) {
        super(actual, EntryAssert.class);
    }

    /**
     * An entry point for EntryAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myEntry)</code> and get specific assertion with code completion.
     * @param actual the Entry we want to make assertions on.
     * @return a new <code>{@link EntryAssert}</code>
     */
    public static EntryAssert assertThat(Entry actual) {
        return new EntryAssert(actual);
    }

    public EntryAssert hasContent(String content) {
        // check that actual Entry we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected content of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualContent = actual.getContent();
        if (!Objects.areEqual(actualContent, content)) {
            failWithMessage(assertjErrorMessage, actual, content, actualContent);
        }

        // return the current assertion for method chaining
        return this;
    }

    public EntryAssert hasEtag(String etag) {
        // check that actual Entry we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected etag of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualEtag = actual.getEtag();
        if (!Objects.areEqual(actualEtag, etag)) {
            failWithMessage(assertjErrorMessage, actual, etag, actualEtag);
        }

        // return the current assertion for method chaining
        return this;
    }
}
