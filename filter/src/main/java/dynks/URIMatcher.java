package dynks;

import java.util.regex.Pattern;

import static java.lang.Character.MIN_VALUE;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;

/**
 * Matcher for strings using very small subset of regexp. All operations are done without allocating any objects on heap
 * which happens when using java regexp (pattern + matcher).
 * <p>
 *  Perfomance comparision of matching (positive) while using:
 *  <ul>
 *      <li>Precompiled "/api/v1/bestsellers/\\d+" for java regexp</li>
 *      <li>"/api/v1/bestsellers/{D}" when using PatternedUrl</li>
 *  </ul>
 *  For Patterned url execution took: 64 ns whereas java regexp 462 ns on Intel i7-4790K.
 *
 * </p>
 * Example:
 * "Some{X}abc" where {X} is special driving character:
 * <ul>
 * <li>{D} - 1 or more characters that are integer numbers so any from 0123456789</li>
 * <li>{S} - 1 or more characters from abcdefghijklmnopqrstvzyx regardless or </li>
 * </ul>
 * Whole sentence will be tested so i.e. if we have pattern: "/books/{D}/authors"
 * if will match true for: <ul>
 * <li>/books/0/authors</li>
 * <li>/books/9999/authors</li>
 * </ul>
 * but will match false against:
 * <ul>
 * <li>/books/0a/authors</li>
 * <li>/books/1/author</li> *
 * </ul>
 * <p>
 * Not supported patterns:
 * <ul>
 * <li>...{D}{D}... - this can be just transformed to ...{D}...</li>
 * <li>...{S}{S}... - this can be just transformed to ...{S}...</li>
 * </ul>
 */
public class URIMatcher {

    private final String pattern;

    public URIMatcher(String pattern) {

        validatePattern(pattern);
        this.pattern = pattern.trim();
    }

    private void validatePattern(String pattern) {

        if (pattern == null) {
            throw new NullPointerException("Pattern can not be null");
        }

        //  checking open closed & special characters
        int size = pattern.length();

        boolean numericStarted = false;
        boolean stringStarted = false;
        boolean waitingForSpecialChar = false;
        boolean waitingForClosure = false;

        for (int i = 0; i < size; i++) {

            final char c = pattern.charAt(i);

            if(c == '{'){
                if(waitingForSpecialChar){
                    throw new IllegalArgumentException("Invalid syntax ({) at position: " + i);
                }
                waitingForSpecialChar = true;
            }
            else if(c == '}'){

                if(waitingForSpecialChar){
                    throw new IllegalArgumentException("Invalid syntax (}) at position: " + i);
                }

                numericStarted = false;
                stringStarted = false;
                waitingForSpecialChar = false;
                waitingForClosure = false;

            }
            else if(c == 'D'){
                if(numericStarted){
                    throw new IllegalArgumentException("Invalid syntax (D while waiting for }) at position: " + i);
                }
                if(stringStarted){
                    throw new IllegalArgumentException("Invalid syntax (D while waiting for }) at position: " + i);
                }

                if(waitingForSpecialChar){
                    numericStarted = true;
                    waitingForSpecialChar = false;
                    waitingForClosure = true;
                }
            }
            else if(c == 'S'){
                if(numericStarted){
                    throw new IllegalArgumentException("Invalid syntax (S while waiting for }) at position: " + i);
                }
                if(stringStarted){
                    throw new IllegalArgumentException("Invalid syntax (S while waiting for }) at position: " + i);
                }

                if(waitingForSpecialChar){
                    stringStarted = true;
                    waitingForSpecialChar = false;
                    waitingForClosure = true;
                }
            }
            else if(waitingForSpecialChar){
                throw new IllegalArgumentException("Unsupported special character (" + c + ") at position: " + i);
            }

        }

        if(waitingForClosure){
            throw new IllegalArgumentException("Unfinished closure");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        URIMatcher that = (URIMatcher) o;

        return pattern.equals(that.pattern);

    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }

    /**
     * Checks without allocation objects on heap (including TLAB) whether url matches pattern using simplified regexp notion.
     *
     * @param uri
     * @return
     */
    public boolean matches(final String uri) {

        if (uri == null) {
            throw new NullPointerException("String to match can not be null");
        }

        if ("".equals(uri)) {
            return "".equals(pattern);
        }

        //  flags on pattern processing
        boolean expectedString = false;
        boolean expectedNumber = false;

        //  flags on tested string processing
        boolean consumingString = false;
        boolean consumingNumber = false;

        boolean jumpImmediatelyToPatternEvaluation = false;
        //  length of pattern that can be decreased by special characters
        int remainingPatternLength = pattern.length();

        int patternCursorPosition = 0;
        char patternCursor = pattern.charAt(patternCursorPosition++);
        remainingPatternLength--;

        if (patternCursor == '{') {

            final char expected = getExpectedPattern(pattern, patternCursorPosition);

            if (expected == 'D') {
                expectedNumber = true;
            } else {
                //  only 'S' option possible here as any deviation from that will throw exception before that block
                expectedString = true;
            }

            patternCursorPosition += 2;   //  sizeof special character + '}'
            remainingPatternLength -= 2;

            //  we need to move cursor to next position in pattern if available
            if (remainingPatternLength > 0) {
                patternCursor = pattern.charAt(patternCursorPosition);
            }

        }

        //  we have now first character from pattern loaded
        final int urlLength = uri.length();

        for (int i = 0; i < urlLength; i++) {

            final char c = uri.charAt(i);

            if (expectedNumber) {
                if (c >= '0' && c <= '9') {
                    consumingNumber = true;
                    //  we assume next character will still be number
                    //  thus not moving pattern cursor
                    continue;
                } else {
                    //  expecting number but found something else
                    //  we need to check if we consumed at least one character of expected type
                    if (!consumingNumber) {
                        //  pattern indicates that we are expecting number but found first character to be non-number
                        return false;
                    }
                    //  we consumed at least one character of expected type

                    consumingNumber = false;
                    expectedNumber = false;

                    jumpImmediatelyToPatternEvaluation = true;


                }
            }

            if (expectedString) {
                if (((c >= 'a' && c <= 'z')) || (c >= 'A' && c <= 'Z')) {
                    consumingString = true;
                    //  we assume next character will still be string
                    //  thus not moving pattern cursor
                    continue;
                } else {
                    //  expecting String but found something else
                    //  we need to check if we consumed at least one character of expected type
                    if (!consumingString) {
                        //  pattern indicates that we are expecting number but found first character to be non-string
                        return false;
                    }
                    //  we consumed at least one character of expected type

                    consumingString = false;
                    expectedString = false;

                    jumpImmediatelyToPatternEvaluation = true;

                }
            }

            if (jumpImmediatelyToPatternEvaluation) {
                //  we went little big too far
                //  need to rewind original string by once to match the state of pattern string
                i--;
                //  reseting for next loop
                jumpImmediatelyToPatternEvaluation = false;
            } else {
                if (patternCursor != c) {
                    //  non special character different than current one
                    //  returning false and finish matching
                    return false;
                }
            }

            //  we found non special character that is the same as expected (from pattern)
            //  just going further with matching

            //  we are not waiting for string, nor number we need to check what's in next pattern
            //  duplicated block from before for loop
            if (remainingPatternLength > 0) {
                patternCursor = pattern.charAt(patternCursorPosition++);
                remainingPatternLength--;
            } else {
                if (i >= urlLength) {
                    return false;
                }
            }

            if (patternCursor == '{') {

                char expected = getExpectedPattern(pattern, patternCursorPosition);

                if(expected == MIN_VALUE){
                    //  some error ocurred, assume non-matched this probably
                    //  should be fixed by changing the algorithm above
                    return false;
                }

                if (expected == 'D') {
                    expectedNumber = true;
                } else {
                    //  only 'S' option possible here as any deviation from that will throw exception before that block
                    expectedString = true;
                }

                patternCursorPosition += 2;   //  sizeof special character + '}'
                remainingPatternLength -= 2;
                //  we need to move cursor to next position in pattern if available
                if (remainingPatternLength > 0) {
                    patternCursor = pattern.charAt(patternCursorPosition);
                } else {
                    if (i >= urlLength) {
                        return false;
                    }
                }
            }
        }

        //  need to check if all pattern was matched
        if (remainingPatternLength > 0) {
            //  some pattern remainings left this means shorter url than expected
            return false;
        } else {
            //  matching url is with url but we need to check if we were expecting special character but not found
            if (expectedNumber && !consumingNumber) {
                return false;
            }

            if (expectedString && !consumingString) {
                return false;
            }
        }

        //  nothing happened before that will negate that strings were different
        return true;
    }

    /**
     *
     * @param pattern
     * @param patternCursorPosition
     * @return D or S for pattern or Character.MIN_VALUE for error that should generally be treated as NON matched.
     */
    private char getExpectedPattern(final String pattern, int patternCursorPosition) {

        if(pattern.length()==patternCursorPosition){
            //  some error ocurred
            return MIN_VALUE;
        }

        char special = pattern.charAt(patternCursorPosition++);
        char expected;

        if (special == 'D') {
            expected = 'D';
        } else if (special == 'S') {
            expected = 'S';
        } else {
            throw new IllegalArgumentException(format("Invalid special character [%s] at position: %d", special, patternCursorPosition));
        }

        special = pattern.charAt(patternCursorPosition++);

        if (special == '}') {
            return expected;
        } else {
            throw new IllegalArgumentException(format("Invalid ending of pattern character [%s] at position: %d", special, patternCursorPosition));
        }
    }

    /**
     * Dirty and far from perfect benchmarking against regexp.
     *
     * @param args
     */
    public static void main(String... args) {

        //  last result 63 ns
        final int loop = 10000000;

        URIMatcher matcher = new URIMatcher("/api/v1/bestsellers/{D}");

        String[] urls = new String[loop];
        for (int i = 0; i < loop; i++) {
            urls[i] = ("/api/v1/bestsellers/" + i);
        }

        long nanoStart = System.nanoTime();

        for (String url : urls) {
            matcher.matches(url);
        }

        long duration = System.nanoTime() - nanoStart;
        long sam = duration / ((long) loop);

        System.out.println("Duration using simplified logic: " + sam + " ns");

        //  testing using regexp
        Pattern pat = compile("/api/v1/bestsellers/\\d+");

        nanoStart = System.nanoTime();

        for (String url : urls) {
            pat.matcher(url).matches();
        }

        duration = System.nanoTime() - nanoStart;
        sam = duration / ((long) loop);

        System.out.println("Duration using regexp: " + sam + " ns");

    }
}
