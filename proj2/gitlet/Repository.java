package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    public static final File REF_REMOTE_DIR = join(GITLET_DIR, "refs/remotes");
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    private static Stage stageAdded = new Stage();
    private static Stage stageRemoval = new Stage();

    private static Commit currCommit;

    private Repository() {
    }

    // Command Function =============================

    /**
     * 设置默认分支为master, 提交个init commit
     */
    public static void init() {
        if (GITLET_DIR.exists()) {
            Utils.existPrint(
                    "A Gitlet version-control system already exists in the current directory.");
        }
        GITLET_DIR.mkdir();

        REF_HEADS_DIR.mkdirs();
        REF_REMOTE_DIR.mkdirs();

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

        // gradescope not allowed
//        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        var firstCommit = new Commit(DEFAULT_INIT_MSG, new Date(0));
        firstCommit.save();
        Utils.writeContents(master, firstCommit.getId());
    }

    public static void add(String filepath) {
        String fileRelativePath = getFileRelativePath(filepath);
        if (!fileExistWorkspace(fileRelativePath)) {
            Utils.existPrint("File does not exist.");
        }
        // create new blob object
        var blob = new Blob(fileRelativePath);
        initialized();

        if (!existBlobById(blob.id())) {
            blob.save();
            stageAdded.addBlob(blob);
            saveAddedStage();
        } else if (stageRemoval.containsBlob(blob)) {
            stageRemoval.removeBlob(blob.filepath());
            saveRemovalStage();
        }
    }

    public static void commit(String msg) {
        initialized();
        if (stageAdded.isEmpty() && stageRemoval.isEmpty()) {
            Utils.existPrint("No changes added to the commit.");
        }

        var newCommit = new Commit(msg, new Date());
        newCommit.addParent(currCommit);
        newCommit.putAllBlob(stageAdded.getCache());
        newCommit.deleteAllBlob(stageRemoval.getCache());
        newCommit.save();

        updateCurrentCommit(newCommit);

        stageAdded.clear();
        saveAddedStage();

        stageRemoval.clear();
        saveRemovalStage();
    }

    public static void rm(String filepath) {
        initialized();
        String fileRelativePath = getFileRelativePath(filepath);
        if (!fileExistWorkspace(fileRelativePath)
                && currCommit.containsBlob(fileRelativePath)) {
            String blobId = currCommit.getCache().get(fileRelativePath);
            currCommit.removeBlob(fileRelativePath);
            updateCurrentCommit(currCommit);
            stageRemoval.addBlob(fileRelativePath, blobId);
            saveRemovalStage();
            return;
        }

        var blob = new Blob(fileRelativePath);

        if (stageAdded.containsBlob(blob)) {
            blob.remove();
            stageAdded.removeBlob(blob.filepath());
            saveAddedStage();
        } else if (currCommit.containsBlob(blob)) {
            workspaceFileDelete(fileRelativePath);
            currCommit.removeBlob(blob);
            updateCurrentCommit(currCommit);
            stageRemoval.addBlob(blob);
            saveRemovalStage();
        } else {
            Utils.existPrint("No reason to remove the file.");
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
            System.out.printf("Merge: %s %s\n", id1.substring(0, 7), id2.substring(0, 7));
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

        boolean find = false;
        for (Commit c : allCommit) {
            if (c.getMessage().equals(commitMsg)) {
                find = true;
                System.out.println(c.getId());
            }
        }
        if (!find) {
            System.out.println("Found no commit with that message.");
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
        List<String> fps = getAllUntrackedFilePath();
        for (String fp : fps) {
            q.add(new File(fp).getName());
        }
        q.forEach(System.out::println);
    }

    public static void checkoutByFilepath(String filepath) {
        // checkout -- filename
        initialized();
        String fileRelativePath = getFileRelativePath(filepath);

        if (!currCommit.containsBlob(fileRelativePath)) {
            Utils.existPrint("File does not exist in that commit.");
        }

        // copy blob to current workspace
        copyBlobContentToWorkspace(currCommit.getBlobId(fileRelativePath), fileRelativePath);
    }

    public static void checkoutByBranchName(String branchName) {
        // checkout branchName
        initialized();
        if (!existBranchName(branchName)) {
            Utils.existPrint("No such branch exists.");
        }

        if (getCurrBranchName().equals(branchName)) {
            Utils.existPrint("No need to checkout the current branch.");
        }

        String branchCommitId = getBranchCommitId(branchName);
        Commit bc = readCommit(branchCommitId);

        // 清除当前commit包含, 对应分支没有的文件
        removeTrackedFileNotExistInCommit(currCommit, bc);
        checkUntrackedFileNotExist(bc);

        restoreFile(bc);
        updateCurrentBranch(branchName);
    }

    public static void checkout(String commitId, String filepath) {
        // checkout commitId -- filename
        initialized();
        File f = getCommitFile(commitId);

        checkCommitExist(f);
        Commit commit = readCommit(f);
        String fileRelativePath = getFileRelativePath(filepath);
        if (!commit.containsBlob(fileRelativePath)) {
            Utils.existPrint("File does not exist in that commit.");
        }
        copyBlobContentToWorkspace(commit.getBlobId(fileRelativePath), fileRelativePath);
    }

    public static void branch(String branchName) {
        if (existBranchName(branchName)) {
            Utils.existPrint("A branch with that name already exists.");
        }
        initialized();
        Branch nb = new Branch(branchName, currCommit);
        nb.save();
    }

    public static void rmBranch(String branchName) {
        if (!existBranchName(branchName)) {
            Utils.existPrint("A branch with that name does not exist.");
        }

        if (getCurrBranchName().equals(branchName)) {
            Utils.existPrint("Cannot remove the current branch.");
        }
        Utils.join(REF_HEADS_DIR, branchName).deleteOnExit();
    }

    public static void reset(String commitId) {
        File f = getCommitFile(commitId);
        checkCommitExist(f);

        initialized();

        Commit cm = readCommit(f);
        checkUntrackedFileNotExist(cm);

        removeTrackedFileNotExistInCommit(currCommit, cm);
        restoreFile(cm);
        updateCurrentHeadCommit(cm);

        stageAdded.clear();
        saveAddedStage();

        stageRemoval.clear();
        saveRemovalStage();
    }

    public static void merge(String branchName) {
        initialized();
        checkMergeBranchNameValid(branchName);

        String cid = getBranchCommitId(branchName);
        Commit otherCommit = readCommit(cid);

        checkUntrackedFileNotExist(otherCommit);

        Commit splitCommit = getSplitPoint(currCommit, otherCommit).orElseThrow();
        checkMergeSplitCommitValid(splitCommit, otherCommit, branchName);


        Set<String> filepaths = new HashSet<>();
        filepaths.addAll(currCommit.getCache().keySet());
        filepaths.addAll(splitCommit.getCache().keySet());
        filepaths.addAll(otherCommit.getCache().keySet());

        Commit newCommit = new Commit("", new Date());

        newCommit.addParent(currCommit);
        newCommit.addParent(otherCommit);
        Map<String, String> cache = newCommit.getCache();

        boolean conflicted = false;

        for (String filepath : filepaths) {
            String oBlobId = otherCommit.getBlobId(filepath);
            String cBlobId = currCommit.getBlobId(filepath);
            String sBlobId = splitCommit.getBlobId(filepath);

            boolean splitCommitHasFile = splitCommit.containsBlob(filepath);
            boolean currCommitHasFile = currCommit.containsBlob(filepath);
            boolean otherCommitHasFile = otherCommit.containsBlob(filepath);


            if (splitCommitHasFile
                    && currCommitHasFile
                    && otherCommitHasFile) {
                // case 1: modified on otherCommit but not HEAD -> otherCommit
                if (!cBlobId.equals(oBlobId)
                        && sBlobId.equals(cBlobId)) {
                    cache.put(filepath, oBlobId);
                    stageAdded.addBlob(filepath, oBlobId);
                    continue;
                }
                // case 2: modified in HEAD but not other -> HEAD
                if (sBlobId.equals(oBlobId)
                        && !cBlobId.equals(oBlobId)) {
                    cache.put(filepath, cBlobId);
                    continue;
                }
            }
            //                                      in same way -> DNM (doesn't matter) (same)
            // case 3: modified in other and HEAD --
            //                                      in diff way -> CONFLICT
            if (!currCommitHasFile
                    && !otherCommitHasFile) {
                // in same way
                cache.remove(filepath);
                workspaceFileDelete(filepath);
                continue;
            }
            if (currCommitHasFile
                    && otherCommitHasFile
                    && cBlobId.equals(oBlobId)) {
                // in same way
                cache.put(filepath, cBlobId);
                continue;
            }
            // “Modified in different ways” can mean that the contents of
            // both are changed and different from other,
            // or the contents of one are changed and the other file is deleted,
            // or the file was absent at the split point and has different contents
            // in the given and current branches
            if (currCommitHasFile) {
                boolean isConflict = false;

                if ((splitCommitHasFile
                        && otherCommitHasFile
                        && !oBlobId.equals(cBlobId)
                        && !oBlobId.equals(sBlobId)
                        && !cBlobId.equals(sBlobId))) {
                    isConflict = true;
                }

                if (!otherCommitHasFile && splitCommitHasFile && !cBlobId.equals(sBlobId)) {
                    isConflict = true;
                }

                if (!splitCommitHasFile && otherCommitHasFile && !oBlobId.equals(cBlobId)) {
                    isConflict = true;
                }

                if (isConflict) {
                    writeConflictFileContent(cache, currCommit, otherCommit, filepath);
                    conflicted = true;
                    continue;
                }
            }
            // case 4: not in split nor other but in HEAD -> HEAD
            if (!splitCommitHasFile
                    && !otherCommitHasFile
                    && currCommitHasFile) {
                cache.put(filepath, cBlobId);
                continue;
            }
            // case 5: not in split nor HEAD but in other -> other
            if (!splitCommitHasFile
                    && !currCommitHasFile
                    && otherCommitHasFile) {
                cache.put(filepath, oBlobId);
                stageAdded.addBlob(filepath, oBlobId);
                continue;
            }
            // case 6: unmodified in HEAD but not present in other -> REMOVE
            if (splitCommitHasFile
                    && currCommitHasFile
                    && !otherCommitHasFile
                    && cBlobId.equals(sBlobId)) {
                cache.remove(filepath);
                stageRemoval.addBlob(filepath, sBlobId);
                workspaceFileDelete(filepath);
                continue;
            }
            // case 7: unmodified in other but not present in HEAD -> REMAIN REMOVE
            if (splitCommitHasFile
                    && !currCommitHasFile
                    && otherCommitHasFile
                    && oBlobId.equals(sBlobId)) {
                cache.remove(filepath);
                workspaceFileDelete(filepath);
                continue;
            }
        }

        if (conflicted) {
            System.out.println("Encountered a merge conflict.");
        }
        newCommit.setMessage(String.format("Merged %s into %s.", branchName, getCurrBranchName()));
        newCommit.save();
        for (String filepath : stageRemoval.getCache().keySet()) {
            workspaceFileDelete(filepath);
        }
        updateCurrentCommit(newCommit);
        restoreFile(newCommit);
//        saveAddedStage();
//        saveRemovalStage();
    }

    public static void addRemote(String remoteName, String remotePath) {
        if (existRemoteName(remoteName)) {
            Utils.existPrint("A remote with that name already exists.");
        }
        createRemote(remoteName, remotePath);
    }

    public static void rmRemote(String remoteName) {
        if (!existRemoteName(remoteName)) {
            Utils.existPrint("A remote with that name does not exist.");
        }
        join(REF_REMOTE_DIR, remoteName).delete();
    }


    // Helper Function =============================
    private static void createRemote(String remoteName, String remotePath) {
        File file = join(REF_REMOTE_DIR, remoteName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Utils.writeContents(file, convertPath(remotePath));
    }

    // 将/转换成系统依赖
    private static String convertPath(String path) {
        return path.replaceAll("//", File.separator);
    }

    private static boolean existRemoteName(String remoteName) {
        List<String> names = Utils.plainFilenamesIn(REF_REMOTE_DIR);
        if (names == null) {
            return false;
        }
        for (String name : names) {
            if (remoteName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static void checkMergeSplitCommitValid(Commit splitCommit, Commit otherCommit, String branchName) {
        if (splitCommit.equals(otherCommit)) {
            Utils.existPrint("Given branch is an ancestor of the current branch.");
        } else if (splitCommit.equals(currCommit)) {
            checkoutByBranchName(branchName);
            Utils.existPrint("Current branch fast-forwarded.");
        }
    }

    private static void checkMergeBranchNameValid(String branchName) {
        if (!stageRemoval.isEmpty()
                || !stageAdded.isEmpty()) {
            Utils.existPrint("You have uncommitted changes.");
        }

        if (!existBranchName(branchName)) {
            Utils.existPrint("A branch with that name does not exist.");
        }

        if (getCurrBranchName().equals(branchName)) {
            Utils.existPrint("Cannot merge a branch with itself.");
        }
    }

    /**
     * Write conflict file
     * <p>
     * <<<<<<< HEAD
     * contents of file in current branch
     * =======
     * contents of file in given branch
     * >>>>>>>
     */
    private static void writeConflictFileContent(Map<String, String> cache, Commit currCommit, Commit otherCommit, String filepath) {
        String curBlobId = currCommit.getBlobId(filepath);
        String otherBlobId = otherCommit.getBlobId(filepath);
        String curContent = "";
        String otherContent = "";
        if (curBlobId != null) {
            curContent = new String(Blob.readBlob(curBlobId).getContent(), StandardCharsets.UTF_8);
        }

        if (otherBlobId != null) {
            otherContent = new String(Blob.readBlob(otherBlobId).getContent(), StandardCharsets.UTF_8);
        }


        Utils.restrictedDelete(filepath);
        File file = new File(filepath);

        String content = "<<<<<<< HEAD" + System.lineSeparator()
                + curContent
                + "=======" + System.lineSeparator()
                + otherContent
                + ">>>>>>>" + System.lineSeparator();

        Utils.writeContents(file, content);

        Blob newBlob = new Blob(filepath);
        newBlob.save();
        cache.put(filepath, newBlob.id());
    }

    /**
     * 获得c1, c2分开之前的最后一次相同Commit
     *
     * @return null if not exist
     */
    public static Optional<Commit> getSplitPoint(Commit c1, Commit c2) {
        Map<Commit, Integer> m1 = getCommitDepth(c1);
        Map<Commit, Integer> m2 = getCommitDepth(c2);

        Commit res = null;
        int minD = -1;
        for (Commit k : m1.keySet()) {
            if (m2.containsKey(k)) {
                if (minD != -1 && m1.get(k) > minD) {
                    continue;
                }
                res = k;
                minD = m1.get(k);
            }
        }
        if (minD == -1) {
            return Optional.empty();
        }
        return Optional.ofNullable(res);
    }

    /**
     * 记录当前commit出发 之前所有的parent commit
     * 当前commit depth 为0
     * Commit -> depth
     */
    private static Map<Commit, Integer> getCommitDepth(Commit commit) {
        Map<Commit, Integer> res = new HashMap<>();
        int d = 0;
        Queue<Commit> q = new LinkedList<>();
        q.add(commit);
        while (!q.isEmpty()) {
            int n = q.size();
            for (int i = 0; i < n; i++) {
                Commit c = q.poll();
                res.put(c, d);
                q.addAll(c.getParents());
            }
            d++;
        }
        return res;
    }

    private static void checkCommitExist(File f) {
        if (!f.exists()) {
            Utils.existPrint("No commit with that id exists.");
        }
    }

    /**
     * 恢复commit中的文件
     */
    private static void restoreFile(Commit c) {
        c.getCache().forEach((filepath, blobId) -> {
            copyBlobContentToWorkspace(blobId, filepath);
        });
    }

    /**
     * 判断commitId 长度找到对应的commit文件
     *
     * @param commitId
     * @return
     */
    private static File getCommitFile(String commitId) {
        File f = null;
        if (commitId.length() != Utils.UID_LENGTH) {
            var tmpDir = Utils.join(OBJECT_COMMIT_DIR, commitId.substring(0, 2));
            List<String> files = Utils.plainFilenamesIn(tmpDir);
            assert (Objects.requireNonNull(files).size() > 0);
            String s = commitId.substring(2);
            for (String filename : files) {
                if (filename.startsWith(s)) {
                    f = Utils.join(tmpDir, filename);
                    break;
                }
            }
        } else {
            f = newCommitFile(commitId);
        }
        assert f != null;
        return f;
    }

    private static void updateCurrentHeadCommit(Commit c) {
        new Branch(getCurrBranchName(), c).save();
    }

    /**
     * If a working file is untracked in the current branch and
     * would be overwritten by the reset,
     * print `There is an untracked file in the way; delete it, or add and commit it first.`
     */
    private static void checkUntrackedFileNotExist(Commit c) {
        for (String fp : getAllUntrackedFilePath()) {
            if (c.containsBlob(fp)) {
                Utils.existPrint("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }
    }

    /**
     * 清除c1包含, c2没有的文件
     * Removes tracked files  that are not present in that c2
     */
    private static void removeTrackedFileNotExistInCommit(Commit c1, Commit c2) {
        c1.getCache().forEach((filepath, blobId) -> {
            if (!c2.containsBlob(filepath)) {
                workspaceFileDelete(filepath);
            }
        });
    }

    private static void updateCurrentBranch(String branchName) {
        Utils.writeContents(HEAD, branchName);
    }

    private static String getBranchCommitId(String branchName) {
        return Utils.readContentsAsString(join(REF_HEADS_DIR, branchName));
    }

    private static boolean existBranchName(String branchName) {
        List<String> allBranchName = Utils.plainFilenamesIn(REF_HEADS_DIR);
        assert allBranchName != null;
        return allBranchName.contains(branchName);
    }

    private static void copyBlobContentToWorkspace(String blobId, String filepath) {
        Blob oldBlob = Blob.readBlob(blobId);
        File file = new File(filepath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Utils.writeContents(file, oldBlob.getContent());
    }

    private static void statusPrintModify() {
        var q = new PriorityQueue<String>();
        // case 1: Tracked in the current commit, changed in the working directory, but not staged
        currCommit.getCache().forEach((filepath, blobId) -> {
            if (stageRemoval.containsBlob(filepath)
                    || stageAdded.containsBlob(filepath)) {
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
            if (stageRemoval.containsBlob(filepath)
                    || fileExistWorkspace(filepath)) {
                return;
            }
            File file = new File(filepath);
            q.add(file.getName() + " (deleted)");
        });
        q.forEach(System.out::println);
    }

    private static List<String> getAllUntrackedFilePath() {
        List<String> res = new ArrayList<String>();
        untrackedHelp(res, CWD);
        return res;
    }

    private static void untrackedHelp(List<String> res, File curDir) {
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
                untrackedHelp(res, f);
            } else {
                String fileRelativePath = getFileRelativePath(f.getPath());
                if (!currCommit.containsBlob(fileRelativePath)
                        && !stageAdded.containsBlob(fileRelativePath)) {
                    res.add(fileRelativePath);
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
        if (files == null) {
            return res;
        }

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
                newCommitFile(id),
                Commit.class);
    }


    public static File newCommitFile(String id) {
        return Utils.join(OBJECT_COMMIT_DIR, id.substring(0, 2), id.substring(2));
    }

    public static Commit readCommit(File f) {
        return Utils.readObject(f, Commit.class);
    }

    private static void updateCurrentCommit(Commit commit) {
        // update current branch
        updateCurrentHeadCommit(commit);
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
            Utils.existPrint("fatal: not a git repository: .gitlet.");
        }
    }

    private static void workspaceFileDelete(String filepath) {
        var f = Utils.join(CWD, filepath);
        if (!f.exists()) {
            return;
        }
        var res = f.delete();
        if (!res) {
            throw new RuntimeException("delete file " + filepath + " failed");
        }
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

    public static void checkValidGitletDirectory() {
        if (!Utils.join(CWD, ".gitlet").exists()) {
            Utils.existPrint("Not in an initialized Gitlet directory.");
        }
    }
}
