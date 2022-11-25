package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import static gitlet.Utils.join;

/**
 * Represents a gitlet repository.
 *
 * @author ChillyForest
 */
public class Repository {

    private static final String DEFAULT_BRANCH_NAME = "master";
    private static final String DEFAULT_INIT_MSG = "initial commit";

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    private Repository() {
    }

    /**
     * 设置默认分支为master, 提交个init commit
     */
    public static void makeInitRepository() {
        if (GITLET_DIR.exists()) {
            System.err.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();

        join(GITLET_DIR, "refs", "heads").mkdirs();

        var master = join(GITLET_DIR, "refs", "heads", DEFAULT_BRANCH_NAME);
        var HEAD = join(GITLET_DIR, "HEAD");
        try {
            master.createNewFile();
            HEAD.createNewFile();
            Utils.writeContents(HEAD, DEFAULT_BRANCH_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        var firstCommit = new Commit(DEFAULT_INIT_MSG, new Date(0), null);
        firstCommit.save();
        Utils.writeContents(master, firstCommit.id());
    }

    private static void writeHEAD(String name) {

    }

    private static void commitHEAD() {
    }

    public static void addFile(String filepath) {
    }
}