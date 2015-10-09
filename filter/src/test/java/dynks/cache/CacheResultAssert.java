package dynks.cache;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;

/**
 * Created by jszczepankiewicz on 2015-04-12.
 */
public class CacheResultAssert  extends AbstractAssert<CacheResultAssert, CacheQueryResult> {

    /**
     * Creates a new <code>{@link CacheResultAssert}</code> to make assertions on actual Entry.
     * @param actual the CacheResult we want to make assertions on.
     */
    public CacheResultAssert(CacheQueryResult actual) {
        super(actual, CacheResultAssert.class);
    }

    /**
     * An entry point for CacheResultAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myEntry)</code> and get specific assertion with code completion.
     * @param actual the CacheResult we want to make assertions on.
     * @return a new <code>{@link CacheResultAssert}</code>
     */
    public static CacheResultAssert assertThat(CacheQueryResult actual) {
        return new CacheResultAssert(actual);
    }

    public CacheResultAssert hasPayload(String content) {
        // check that actual Entry we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected payload of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualContent = actual.getPayload();
        if (!Objects.areEqual(actualContent, content)) {
            failWithMessage(assertjErrorMessage, actual, content, actualContent);
        }

        // return the current assertion for method chaining
        return this;
    }

    public CacheResultAssert hasStoredEtag(String etag) {
        // check that actual Entry we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected etag of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualEtag = actual.getStoredEtag();
        if (!Objects.areEqual(actualEtag, etag)) {
            failWithMessage(assertjErrorMessage, actual, etag, actualEtag);
        }

        // return the current assertion for method chaining
        return this;
    }

    public CacheResultAssert isUpsertNeeded() {
        // check that actual Entry we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected upsertNeeded of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        boolean actualUpsertNeeded = actual.isUpsertNeeded();
        if (!actualUpsertNeeded) {
            failWithMessage(assertjErrorMessage, actual, true, false);
        }

        // return the current assertion for method chaining
        return this;
    }

    public CacheResultAssert isUpsertNotNeeded() {
        // check that actual Entry we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected upsertNeeded of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        boolean actualUpsertNeeded = actual.isUpsertNeeded();
        if (actualUpsertNeeded) {
            failWithMessage(assertjErrorMessage, actual, false, true);
        }

        // return the current assertion for method chaining
        return this;
    }

}
