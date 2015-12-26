package dynks.cache.test;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Hamcrest matcher to assert whether cause of exception matches class and message.
 *
 * @author jszczepankiewicz
 * @since 2015-12-25
 */
public class CauseMatcher extends TypeSafeMatcher<Throwable> {

  private final Class<? extends Throwable> expectedType;
  private final String expectedMessage;

  public CauseMatcher(Class<? extends Throwable> expectedType, String expectedMessage) {
    this.expectedType = expectedType;
    this.expectedMessage = expectedMessage;
  }

  @Override
  protected boolean matchesSafely(Throwable item) {
    return item.getClass().isAssignableFrom(expectedType)
            && item.getMessage().contains(expectedMessage);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("expects expectedType ")
            .appendValue(expectedType)
            .appendText(" and a message ")
            .appendValue(expectedMessage);
  }
}
