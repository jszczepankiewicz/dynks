package dynks;

/**
 * Created by jszczepankiewicz on 2015-04-06.
 */
public class Strings {
    public static void recycle(StringBuilder builder){
        builder.delete(0, builder.length());
    }
}
