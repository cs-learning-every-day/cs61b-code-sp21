package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

    // Command Function =============================

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
        Utils.writeContents(master, firstCommit.getId());
    }

    public static void add(String filepath) {
        checkRepositoryExist();
        String fileRelativePath = getFileRelativePath(filepath);
        if (!fileExistWorkspace(fileRelativePath)) {
            System.err.println("File does not exist.");
            System.exit(0);
        }
        // create new blob object
        var blob = new Blob(fileRelativePath);

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
        newCommit.addParent(currCommit);
        newCommit.putAllBlob(stageAdded.getCache());
        newCommit.save();

        updateCurrentCommit(newCommit);

        stageAdded.clear();
        saveAddedStage();
    }

    public static void rm(String filepath) {
        initialized();
        String fileRelativePath = getFileRelativePath(filepath);
        var blob = new Blob(fileRelativePath);

        if (stageAdded.containsBlob(blob)) {
            blob.remove();
            stageAdded.removeBlob(blob);
            saveAddedStage();
        } else if (currCommit.containsBlob(blob)) {
            workspaceFileDelete(fileRelativePath);
            currCommit.removeBlob(blob);
            updateCurrentCommit(currCommit);
            stageRemoval.addBlob(blob);
            saveRemovalStage();
        } else {
            System.err.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    public static void log() {
        initialized();
        var p = currCommit;
        int size;
        while (true) {
            size = p.getParents().size();
            logPrintCommit(p);

            if (size == 0) {
                break;
            } else {
                // TODO: test merge case
                // merge case : print first parent  commit ignore another
                p = p.getParents().get(0);
            }
        }
    }

    private static void logPrintCommit(Commit c) {
        System.out.println("===");
        System.out.println("commit " + c.getId());
        if (c.getParents().size() > 1) {
            String id1 = c.getParents().get(0).getId();
            String id2 = c.getParents().get(1).getId();
            System.out.printf("Merge: %s %s", id1, id2);
        }
        System.out.println("Date: " + c.getTimestamp());
        System.out.println(c.getMessage());
        System.out.println();
    }

    public static void globalLog() {
        List<Commit> allCommit = getAllCommit();
        for (Commit c : allCommit) {
            logPrintCommit(c);
        }
    }

    public static void find(String commitMsg) {
        List<Commit> allCommit = getAllCommit();
        if (allCommit.isEmpty()) {
            System.out.println("Found no commit with that message.");
            return;
        }
        for (Commit c : allCommit) {
            if (c.getMessage().equals(commitMsg)) {
                System.out.println(c.getId());
            }
        }
    }

    public static void status() {
        initialized();
        System.out.println("=== Branches ===");
        statusPrintBranch();

        System.out.println();
        System.out.println("=== Staged Files ===");
        statusPrintStage(stageAdded);

        System.out.println();
        System.out.println("=== Removed Files ===");
        statusPrintStage(stageRemoval);

        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        statusPrintModify();


        System.out.println();
        System.out.println("=== Untracked Files ===");
        var q = new PriorityQueue<String>();
        statusPrintUntracked(q, CWD);
        q.forEach(System.out::println);
    }


    // Helper Function =============================

    private static void statusPrintModify() {
        var q = new PriorityQueue<String>();
        // case 1: Tracked in the current commit, changed in the working directory, but not staged
        currCommit.getCache().forEach((filepath, blobId) -> {
            if (stageRemoval.containsBlob(filepath) ||
                    stageAdded.containsBlob(filepath)) {
                return;
            }
            if (!fileExistWorkspace(filepath)) {
                return;
            }
            Blob blob = new Blob(filepath);
            if (!blob.id().equals(blobId)) {
                File file = new File(filepath);
                q.add(file.getName() + " (modified)");
            }
        });
        // case 2: Staged for addition, but with different contents than in the working directory
        stageAdded.getCache().forEach((filepath, blobId) -> {
            if (!fileExistWorkspace(filepath)) {
                return;
            }
            Blob blob = new Blob(filepath);
            if (!blob.id().equals(blobId)) {
                File file = new File(filepath);
                q.add(file.getName() + " (modified)");
            }
        });
        // case 3: Staged for addition, but deleted in the working directory
        stageAdded.getCache().forEach((filepath, blobId) -> {
            if (fileExistWorkspace(filepath)) {
                return;
            }
            File file = new File(filepath);
            q.add(file.getName() + " (deleted)");
        });
        // case 4: Not staged for removal, but tracked in the current commit and deleted from the working directory
        currCommit.getCache().forEach((filepath, blobId) -> {
            if (stageRemoval.containsBlob(filepath) ||
                    fileExistWorkspace(filepath)) {
                return;
            }
            File file = new File(filepath);
            q.add(file.getName() + " (deleted)");
        });
        q.forEach(System.out::println);
    }

    private static void statusPrintUntracked(PriorityQueue<String> q, File curDir) {
        File[] files = curDir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                if (f.getName().equals(".gitlet")) {
                    continue;
                }
                // FIXME: gitlet ignore any subdirectories
                statusPrintUntracked(q, f);
            } else {
                String fileRelativePath = getFileRelativePath(f.getPath());
                if (!currCommit.containsBlob(fileRelativePath) &&
                        !stageAdded.containsBlob(fileRelativePath)) {
                    q.add(f.getName());
                }
            }
        }
    }

    private static void statusPrintBranch() {
        String cbn = getCurrBranchName();
        PriorityQueue<String> q = getAllBranchName();
        for (String name : q) {
            if (name.equals(cbn)) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        }
    }

    private static PriorityQueue<String> getAllBranchName() {
        var res = new PriorityQueue<String>();
        File[] files = REF_HEADS_DIR.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    res.add(file.getName());
                }
            }
        }
        return res;
    }

    private static void statusPrintStage(Stage stage) {
        PriorityQueue<String> q = new PriorityQueue<>();
        for (String fp : stage.getCache().keySet()) {
            q.add(getFileName(fp));
        }
        q.forEach(System.out::println);
    }

    private static String getFileName(String path) {
        String[] sp = path.split(File.pathSeparator);
        return sp[sp.length - 1];
    }

    /**
     * 返回文件f相对于当前工作目录的相对路径
     */
    private static String getFileRelativePath(String filepath) {
        String res;
        try {
            File f = new File(filepath);
            String fp = f.getCanonicalPath();
            res = fp.replace(CWD.getCanonicalPath(), "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 移除路径前缀
        // /pom.xml -> pom.xml
        return res.substring(1);
    }


    private static List<Commit> getAllCommit() {
        List<Commit> res = new ArrayList<>();
        File[] files = OBJECT_COMMIT_DIR.listFiles();
        if (files == null) return res;

        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }

            List<String> filenames = Utils.plainFilenamesIn(file);
            if (filenames == null) {
                continue;
            }

            for (String filename : filenames) {
                res.add(readCommit(join(file, filename)));
            }
        }
        return res;
    }

    public static Commit readCommit(String id) {
        return Utils.readObject(
                Utils.join(OBJECT_COMMIT_DIR, id.substring(0, 2), id.substring(2)),
                Commit.class);
    }

    public static Commit readCommit(File f) {
        return Utils.readObject(f, Commit.class);
    }

    private static void updateCurrentCommit(Commit commit) {
        // update current branch
        Utils.writeContents(Utils.join(REF_HEADS_DIR, getCurrBranchName()), commit.getId());
        currCommit = commit;
    }

    private static String getCurrBranchName() {
        return Utils.readContentsAsString(HEAD);
    }

    private static String getCurrCommitId() {
        return Utils.readContentsAsString(Utils.join(REF_HEADS_DIR, getCurrBranchName()));
    }

    private static void readCurrCommit() {
        currCommit = readCommit(getCurrCommitId());
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

    private static void checkRepositoryExist() {
        if (!GITLET_DIR.exists()) {
            System.err.println("fatal: not a git repository: .gitlet.");
            System.exit(0);
        }
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
