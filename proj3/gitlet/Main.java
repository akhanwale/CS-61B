package gitlet;
import javax.swing.*;
import java.util.Calendar;
import java.io.File;
import java.util.TimeZone;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Aniruddh Khanwale
 */
public class Main {

    /** Current Working Directory. */
    static final File CWD = new File(".");

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            return;
        }
        switch (args[0]) {
        case "init" :
            init(args);
            break;
        case "add":
            add(args);
            break;
        default:
            System.exit(0);
            break;
        }
    }

    public static void init(String [] args) {

        File gitletDir = Utils.join(CWD, ".gitlet");
        if (gitletDir.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        } else {
            gitletDir.mkdir();
            TimeZone tz = Calendar.getInstance().getTimeZone();
            long tzOffset = tz.getOffset(0);
            Commit initialCommit = new Commit("initial commit", -tzOffset);
            System.out.println(initialCommit);
        }

    }
    public static void add(String [] args) {

    }
}