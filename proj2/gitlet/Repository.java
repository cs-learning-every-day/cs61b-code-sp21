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
    public static final File STAGE_ADDITION_FILE = join(GITLET_DIR, "stageAdded");
    public static final File STAGE_REMOVAL_FILE = join(GITLET_DIR, "stageRemoval");
    public static final File REF_HEADS_DIR = join(GITLET_DIR, "refs/heads");
    public static final File OBJECT_BLOB_DIR = join(GITLET_DIR, "objects/blobs");
    public static final File OBJECT_COMMIT_DIR = join(GITLET_DIR, "objects/commits");
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    public static Stage stageAdded = new Stage();
    public static Stage stageRemoval = new Stage();

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
            STAGE_ADDITION_FILE.createNewFile();
            STAGE_REMOVAL_FILE.createNewFile();
            saveAddedStage();
            saveRemovalStage();
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
        if (!fileExistWorkspace(filepath)) {
            System.err.println("File does not exist.");
            System.exit(0);
        }
        // create new blob object
        var blob = new Blob(filepath);

        if (existBlobById(blob.id())) {
            return;
        }
        blob.save();
        initialized();

        stageAdded.addBlob(blob);
        saveAddedStage();
    }

    public static void commit(String msg) {
        initialized();
        if (stageAdded.isEmpty()) {
            System.err.println("No changes added to the commit.");
            System.exit(0);
        }

        var newCommit = new Commit(msg, new Date());
        newCommit.setParent(currCommit);
        newCommit.putAllBlob(stageAdded.getCache());
        newCommit.save();

        updateCurrentCommit(newCommit);

        stageAdded.clear();
        saveAddedStage();
    }

    public static void rm(String filepath) {
        initialized();
        var blob = new Blob(filepath);

        if (stageAdded.containsBlob(blob)) {
            blob.remove();
            stageAdded.removeBlob(blob);
            saveAddedStage();
        } else if (currCommit.containsBlob(blob)) {
            workspaceFileDelete(filepath);
            currCommit.removeBlob(blob);
            updateCurrentCommit(currCommit);
            stageRemoval.addBlob(blob);
        } else {
            System.err.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    private static void updateCurrentCommit(Commit commit) {
        // update current branch
        Utils.writeContents(Utils.join(REF_HEADS_DIR, getCurrBranchName()), commit.id());
        currCommit = commit;
    }

    private static String getCurrBranchName() {
        return Utils.readContentsAsString(HEAD);
    }

    private static String getCurrCommitId() {
        return Utils.readContentsAsString(Utils.join(REF_HEADS_DIR, getCurrBranchName()));
    }

    private static void readCurrCommit() {
        currCommit = Commit.readCommit(getCurrCommitId());
    }

    private static void readStage() {
        stageRemoval = Stage.readStage(STAGE_REMOVAL_FILE);
        stageAdded = Stage.readStage(STAGE_ADDITION_FILE);
    }

    public static void saveAddedStage() {
        Utils.writeObject(STAGE_ADDITION_FILE, stageAdded);
    }

    public static void saveRemovalStage() {
        Utils.writeObject(STAGE_REMOVAL_FILE, stageRemoval);
    }

    private static boolean fileExistWorkspace(String filepath) {
        return Utils.join(CWD, filepath).exists();
    }

    private static void workspaceFileDelete(String filepath) {
        Utils.join(CWD, filepath).delete();
    }

    private static void initialized() {
        readCurrCommit();
        readStage();
    }


    private static boolean existBlobById(String id) {
        return Utils.join(OBJECT_BLOB_DIR,
                        id.substring(0, 2),
                        id.substring(2))
                .isFile();
    }

}
