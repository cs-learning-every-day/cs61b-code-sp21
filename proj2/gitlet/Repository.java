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
    public static final File INDEX_FILE = join(GITLET_DIR, "index");
    public static final File REF_HEADS_DIR = join(GITLET_DIR, "refs/heads");
    public static final File OBJECT_BLOB_DIR = join(GITLET_DIR, "objects/blobs");
    public static final File OBJECT_COMMIT_DIR = join(GITLET_DIR, "objects/commits");
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    public static Stage stage = new Stage();
    public static Commit currCommit;

    private Repository() {
    }

    private static void checkRepositoryExist() {
        if (!GITLET_DIR.exists()) {
            System.err.println("fatal: not a git repository: .gitlet.");
            System.exit(0);
        }
    }

    /**
     * 设置默认分支为master, 提交个init commit
     */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.err.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();

        join(GITLET_DIR, "refs", "heads").mkdirs();

        var master = join(GITLET_DIR, "refs", "heads", DEFAULT_BRANCH_NAME);
        try {
            master.createNewFile();
            HEAD.createNewFile();
            Utils.writeContents(HEAD, DEFAULT_BRANCH_NAME);
            INDEX_FILE.createNewFile();
            stage.save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        var firstCommit = new Commit(DEFAULT_INIT_MSG, new Date(0));
        firstCommit.save();
        Utils.writeContents(master, firstCommit.id());
    }

    public static void add(String filepath) {
        checkRepositoryExist();
        checkFileExist(filepath);
        // create new blob object
        var blob = new Blob(filepath);

        readCurrCommit();
        if (currCommit.containsBlob(blob)) {
            return;
        }
        blob.save();
        stage = Stage.readStage();

        stage.addBlob(blob);
        stage.save();
    }

    private static void readCurrCommit() {
        String currBranchName = Utils.readContentsAsString(HEAD);
        String id = Utils.readContentsAsString(Utils.join(REF_HEADS_DIR, currBranchName));
        currCommit = Commit.readCommit(id);
    }

    protected static void checkFileExist(String filepath) {
        if (!Utils.join(CWD, filepath).exists()) {
            System.err.println("File does not exist.");
            System.exit(0);
        }
    }
}
