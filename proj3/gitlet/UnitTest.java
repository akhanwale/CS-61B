package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.io.IOException;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Aniruddh Khanwale
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) throws IOException {
        System.exit(textui.runClasses(UnitTest.class));
    }


    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() {
    }

}


