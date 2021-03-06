package gitlet;


import jdk.jshell.execution.Util;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Aniruddh Khanwale
 */
public class Main {

    /** Current Working Directory. */
    static final File CWD = new File(".");

    /** File object of gitlet subdirectory. */
    private static File gitletDir = Utils.join(CWD, ".gitlet");

    /** Remotes directory **/
    static File remotesDir = Utils.join(gitletDir, "remotes");
    /** File object containing commits. */
    private static File commits = Utils.join(gitletDir, "commits");

    /** File object containing blobs. */
    private static File blobs = Utils.join(gitletDir, "blobs");

    /** File object containing head reference. */
    private static File head = Utils.join(gitletDir, "HEAD");

    /** File object containing ref to current branch. */
    private static File workingBranch = Utils.join(gitletDir,
            "current-branch");
    /** File object containing branch data. */
    private static File branches = Utils.join(gitletDir, "branches");

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        try {
            if (args.length == 0) {
                throw new GitletException("Please enter a command.");
            } else {
                switch (args[0]) {
                case "init":
                    init(args);
                    break;
                case "add":
                    add(args);
                    break;
                case "commit":
                    commit(args);
                    break;
                case "log":
                    log(args);
                    break;
                case "global-log":
                    globalLog(args);
                    break;
                case "status":
                    status(args);
                    break;
                case "rm":
                    rm(args);
                    break;
                case "checkout":
                    checkout(args);
                    break;
                case "branch":
                    branch(args);
                    break;
                case "reset":
                    reset(args);
                    break;
                case "find":
                    find(args);
                    break;
                case "rm-branch":
                    rmBranch(args);
                    break;
                case "merge":
                    merge(args);
                    break;
                case "add-remote":
                    addRemote(args);
                    break;
                case "rm-remote":
                    rmRemote(args);
                case "fetch":
                    fetch(args);
                    break;
                case "push":
//                    push(args);
                    break;
                case "pull":
//                    pull(args);
                    break;
                default:
                    throw new GitletException(
                            "No command with that name exists.");
                }
            }
        } catch (GitletException gitletErr) {
            System.err.print(gitletErr.getMessage());
            System.exit(0);
        }
    }

    /** Initializes a new gitlet repository.
     * @param args Not used. */
    public static void init(String[] args) throws GitletException {
        if (gitletDir.exists()) {
            throw new GitletException(
                    "A Gitlet version-control system already "
                            + "exists in the current directory.");
        } else if (args.length != 1) {
            throw new GitletException("Incorrect operands.");
        } else {
            gitletDir.mkdir();
            commits.mkdir();
            blobs.mkdir();
            branches.mkdir();
            InitialCommit initialCommit =
                    new InitialCommit("initial commit", 0);
            initialCommit.commit(head);
            initialCommit.persist(commits);
            File masterHead = Utils.join(branches, "master");
            try {
                workingBranch.createNewFile();
                masterHead.createNewFile();
                Utils.writeContents(workingBranch, "master");
                Utils.writeContents(masterHead, initialCommit.getHash());
            } catch (IOException e) {
                return;
            }
        }
    }

    /** Adds the specified file to the repository.
     *
     * @param args The file to add.
     * @throws GitletException
     */
    public static void add(String[] args) throws GitletException {
        if (!gitletDir.exists()) {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        } else if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        } else {
            File toAdd = Utils.join(CWD, args[1]);
            if (!toAdd.isFile()) {
                throw new GitletException("File does not exist.");
            } else {
                StagingArea myStage = new StagingArea(gitletDir);
                Blob toStage = new Blob(toAdd, blobs);
                myStage.stageFile(toStage);
                if (myStage.getStagePath().isFile()) {
                    Utils.writeObject(myStage.getStagePath(), myStage);
                }
            }
        }
    }

    /** Commits the current state of the repository.
     *
     * @param args The commit message
     * @throws GitletException
     */
    public static void commit(String[] args) throws GitletException {
        StagingArea currentStage = new StagingArea(gitletDir);
        if (!gitletDir.exists()) {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        } else if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        } else if (currentStage.size() == 0
                && currentStage.getRemovedFiles().size() == 0) {
            throw new GitletException("No changes added to the commit.");
        } else if (args[1].length() == 0) {
            throw new GitletException("Please enter a commit message.");
        } else {
            String currentBranch = Utils.readContentsAsString(workingBranch);
            Commit myCommit = new Commit(args[1], System.currentTimeMillis());
            myCommit.commit(currentStage, currentBranch);
            myCommit.persist(commits);
            File currBranch = Utils.join(branches, currentBranch);
            Utils.writeContents(currBranch, myCommit.getHash());
            currentStage.getStagePath().delete();
        }
    }

    /** Removes the specified file from the gitlet repo.
     *
     * @param args The file to remove.
     */
    public static void rm(String[] args) {
        if (!gitletDir.exists()) {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        } else if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        } else {
            StagingArea currStage = new StagingArea(gitletDir);
            File toRemove = Utils.join(CWD, args[1]);
            currStage.rmFile(toRemove);
            if (currStage.getStagePath().isFile()) {
                Utils.writeObject(currStage.getStagePath(), currStage);
            }
        }
    }

    /** Prints out a log of this branches commits. *
     *
     * @param args Not used
     */
    public static void log(String[] args) {
        if (!gitletDir.exists()) {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        } else if (args.length != 1) {
            throw new GitletException("Incorrect operands.");
        } else {
            File branchPath = Utils.join(branches, Utils.readContentsAsString(
                    workingBranch));
            String currCommitID = Utils.readContentsAsString(branchPath);
            while (currCommitID != null) {
                Commit prevCommit = Utils.readObject(
                        Utils.join(commits, currCommitID), Commit.class);
                currCommitID = prevCommit.log();
            }
        }
    }

    /** Prints the current status of the gitlet repo.
     *
     * @param args Not used
     */
    public static void status(String[] args) {
        if (!gitletDir.exists()) {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        } else if (args.length != 1) {
            throw new GitletException("Incorrect operands.");
        } else {
            String currBranch = Utils.readContentsAsString(workingBranch);
            System.out.println("=== Branches ===");
            System.out.println("*" + currBranch);
            for (File branch : branches.listFiles()) {
                if (!branch.getName().equals(currBranch)) {
                    System.out.println(branch.getName());
                }
            }
            System.out.println();
            System.out.println("=== Staged Files ===");
            StagingArea currStage = new StagingArea(gitletDir);
            for (String name : currStage.getBlobNames().keySet()) {
                System.out.println(name);
            }
            System.out.println();
            System.out.println("=== Removed Files ===");
            for (String name: currStage.getRemovedFiles()) {
                System.out.println(name);
            }
            System.out.println();
            HashMap<String, File> filesInDir = new HashMap<>();
            for (File f : CWD.listFiles()) {
                if (f.isFile()) {
                    filesInDir.put(f.getName(), f);
                }
            }

            System.out.println("=== Modifications Not Staged For Commit ===");
            for (String fName : filesInDir.keySet()) {
                String currFileData = Utils.readContentsAsString(filesInDir.get(fName));
                if (currStage.getTrackedFiles().containsKey(fName)) {
                    if (!currStage.getTrackedFiles().get(fName).getBlobString().equals(currFileData)) {
                        System.out.println(fName + " (modified)");
                    }
                } else if (currStage.getBlobNames().containsKey(fName) && !currStage.getBlobNames().get(fName).getBlobString().equals(currFileData)) {
                    System.out.println(fName + " (modified)");
                }
            }
            for (String fName : currStage.getBlobNames().keySet()) {
                if (!filesInDir.containsKey(fName)) {
                    System.out.println(fName + " (deleted)");
                }
            }
            for (String fName : currStage.getTrackedFiles().keySet()) {
                if (!filesInDir.containsKey(fName) && !currStage.getRemovedFiles().contains(fName)) {
                    System.out.println(fName + " (deleted)");
                }
            }
            System.out.println();
            System.out.println("=== Untracked Files ===");
            for (String fName : filesInDir.keySet()) {
                if (!currStage.getTrackedFiles().containsKey(fName) && !currStage.getBlobNames().containsKey(fName)) {
                    System.out.println(fName);
                }
            }
            System.out.println();
        }
    }

    /** Resets either a file or the repo to a specified commit/branch.
     *
     * @param args Specifies what to checkout
     */
    public static void checkout(String[] args) throws GitletException {
        if (!gitletDir.exists()) {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        }
        if (args[1].equals("--")) {
            try {
                checkoutHeadFile(args[2]);
            } catch (IOException dummy) {
                return;
            }
        }  else if (args.length == 2) {
                checkoutBranch(args[1]);
        } else if (args[2].equals("--")) {
            try {
                checkoutCommitFile(args[1], args[3]);
            } catch (IOException dummy) {
                return;
            }
        } else {
            throw new GitletException("Incorrect operands.");
        }
    }

    /** Creates a new branch in the gitlet repo.
     *
     * @param args The branch name.
     */
    public static void branch(String[] args) {
        if (!gitletDir.exists()) {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        } else if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        } else if (Utils.join(branches, args[1]).exists()) {
            throw new GitletException(
                    "A branch with that name already exists.");
        } else {
            File branchHead = Utils.join(branches, args[1]);
            try {
                branchHead.createNewFile();
                Utils.writeContents(branchHead,
                        Utils.readContentsAsString(head));
            } catch (IOException io) {
                return;
            }
        }
    }

    /** Finds all commits with the message specified.
     *
     * @param args the message to check for
     */
    public static void find(String[] args) {
        if (!gitletDir.exists()) {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        } else if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        } else {
            boolean found = false;
            for (File toFind : commits.listFiles()) {
                Commit toCheck = Utils.readObject(toFind, Commit.class);
                if (toCheck.getCommitMessage().equals(args[1])) {
                    System.out.println(toCheck.getHash());
                    found = true;
                }
            }
            if (!found) {
                throw new GitletException("Found no commit with that message.");
            }
        }
    }

    /** Prints a log of all commits ever made.
     *
     * @param args Not used
     */
    public static void globalLog(String[] args) {
        if (!gitletDir.exists()) {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        } else if (args.length != 1) {
            throw new GitletException("Incorrect operands.");
        } else {
            for (File commit : commits.listFiles()) {
                Commit obj = Utils.readObject(commit, Commit.class);
                obj.log();
            }
        }
    }

    /** Removes the specified branch.
     *
     * @param args The branch to remove.
     */
    public static void rmBranch(String[] args) {
        if (!gitletDir.exists()) {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        } else if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        } else if (args[1].equals(Utils.readContentsAsString(workingBranch))) {
            throw new GitletException("Cannot remove the current branch.");
        } else if (!Utils.join(branches, args[1]).exists()) {
            throw new GitletException(
                    "A branch with that name does not exist.");
        } else {
            Utils.join(branches, args[1]).delete();
        }
    }

    /** Resets the state of the repository to that of the commit specified.
     * @param args contains the commit to reset to. */
    public static void reset(String[] args) {
        if (!gitletDir.exists()) {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        } else if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        } else {
            String commitID = args[1];
            File readCommit = Utils.join(commits, commitID);
            if (!readCommit.exists()) {
                throw new GitletException("No commit with that id exists.");
            }
            Commit repoHead = Utils.readObject(Utils.join(
                    commits, Utils.readContentsAsString(head)), Commit.class);
            Commit setHead = Utils.readObject(Utils.join(
                    commits, commitID), Commit.class);
            if (setHead.getStage() != null) {
                for (String name : setHead.getStage().
                        getTrackedFiles().keySet()) {
                    File toWrite = Utils.join(CWD, name);
                    if (toWrite.exists()) {
                        if (repoHead.getStage() == null
                                || !repoHead.getStage().getTrackedFiles().
                                        containsKey(name)) {
                            throw new GitletException("There is an untracked"
                                     + " file in the way; delete it, or add and"
                                    + " commit it first.");
                        }
                    } else {
                        try {
                            toWrite.createNewFile();
                        } catch (IOException dummy) {
                            return;
                        }
                    }
                    Utils.writeContents(
                            toWrite, setHead.getStage().getTrackedFiles().
                                    get(name).getBlobString());
                }
            }
            if (repoHead.getStage() != null) {
                for (String fN : repoHead.getStage().getBlobNames().keySet()) {
                    File toDelete = Utils.join(CWD, fN);
                    if (setHead.getStage() == null) {
                        Utils.restrictedDelete(toDelete);
                    } else if (!setHead.getStage().getBlobNames().
                            keySet().contains(fN)) {
                        Utils.restrictedDelete(toDelete);
                    }
                }
            }
            new StagingArea(gitletDir).getStagePath().delete();
            Utils.writeContents(Utils.join(branches, Utils.readContentsAsString(
                    workingBranch)), commitID);
            Utils.writeContents(head, commitID);
        }

    }

    /** Merges the given branch into the working branch.
     *
     * @param args The given branch to merge into the current branch.
     */
    public static void merge(String[] args) throws GitletException{
        if (!gitletDir.exists()) {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        } else if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        } else if (!Utils.join(branches,
                args[1]).exists()) {
            throw new
                    GitletException("A branch with that name does not exist.");
        } else if (args[1].equals(Utils.readContentsAsString(workingBranch))) {
            throw new GitletException("Cannot merge a branch with itself.");
        } else {
            boolean mergeConflict = false;
            StagingArea currStage = new StagingArea(gitletDir);
            if (currStage.size() != 0) {
                throw new GitletException("You have uncommitted changes.");
            }
            String currentBranchHead = Utils.readContentsAsString(Utils.join(branches, Utils.readContentsAsString(workingBranch)));
            String givenBranchHead = Utils.readContentsAsString(Utils.join(branches, args[1]));
            String splitPointHash = findSplitPoint(givenBranchHead, currentBranchHead);
            if (splitPointHash.equals("checkout")) {
                checkout(new String[]{"checkout", args[1]});
                System.out.println("Current branch fast-forwarded.");
                return;
            }
            Commit splitPointCommit = Utils.readObject(Utils.join(commits, splitPointHash), Commit.class);
            StagingArea branchStage = Utils.readObject(
                    Utils.join(commits, givenBranchHead),
                    Commit.class).getStage();
            StagingArea headStage = Utils.readObject(
                    Utils.join(commits, currentBranchHead),
                    Commit.class).getStage();
            StagingArea splitStage = splitPointCommit.getStage();
            HashMap<String, Blob> modifiedInBranch = getModifiedFiles(splitStage, branchStage);
            HashMap<String, Blob> modifiedInHead = getModifiedFiles(splitStage, headStage);
            HashMap<String, File> filesInDir = new HashMap<>();
            for (File f : CWD.listFiles()) {
                filesInDir.put(f.getName(), f);
            }
            for (String fName : modifiedInBranch.keySet()) {
                if (!modifiedInHead.containsKey(fName)) {
                    if (!currStage.getTrackedFiles().containsKey(fName) && filesInDir.containsKey(fName)) {
                        throw new GitletException("There is an untracked file in the way; delete it, or add and commit it first.");
                    }
                }
            }
            for (String fName : modifiedInBranch.keySet()) {
                if (modifiedInBranch.get(fName) != null && !modifiedInHead.containsKey(fName)) {
                    checkout(new String[]{"checkout", givenBranchHead, "--", fName});
                    add(new String[]{"add", fName});
                    currStage = new StagingArea(gitletDir);
                } else if (modifiedInBranch.get(fName) == null && !modifiedInHead.containsKey(fName)) {
                    rm(new String[]{"rm", fName});
                    currStage = new StagingArea(gitletDir);
                }
            }
            Set<String> modifiedInBoth = new HashSet<>(modifiedInHead.keySet());
            modifiedInBoth.retainAll(modifiedInBranch.keySet());
            for (String fName : modifiedInBoth) {
                String conflict = "<<<<<<< HEAD\n";
                if (modifiedInHead.get(fName) != null && modifiedInBranch.get(fName) != null && !modifiedInHead.get(fName).equals(modifiedInBranch.get(fName))) {
                    conflict += modifiedInHead.get(fName).getBlobString();
                    conflict += "=======\n";
                    conflict += modifiedInBranch.get(fName).getBlobString();
                    conflict += ">>>>>>>";
                } else if (modifiedInHead.get(fName) == null && modifiedInBranch.get(fName) == null) {
                    conflict = "<<<<<<< HEAD\n";
                } else if (modifiedInHead.get(fName) == null) {
                    conflict += "=======\n";
                    conflict += modifiedInBranch.get(fName).getBlobString();
                    conflict += ">>>>>>>";
                } else if (modifiedInBranch.get(fName) == null) {
                    conflict += modifiedInHead.get(fName).getBlobString();
                    conflict += "=======\n";
                    conflict += ">>>>>>>";
                }
                if (!conflict.equals("<<<<<<< HEAD\n")) {
                    mergeConflict = true;
                    try {
                        File conflictFile = Utils.join(CWD, fName);
                        if (!conflictFile.exists()) {
                            conflictFile.createNewFile();
                        }
                        Utils.writeContents(conflictFile, conflict);
                        add(new String[]{"add", conflictFile.getName()});
                        currStage = new StagingArea(gitletDir);
                    } catch (IOException dummy) {
                        return;
                    }
                }
            }

            MergeCommit myMerge = new MergeCommit("Merged " + args[1] + " into " + Utils.readContentsAsString(workingBranch) + ".", System.currentTimeMillis(), givenBranchHead);
            String currentBranch = Utils.readContentsAsString(workingBranch);
            myMerge.commit(currStage, currentBranch);
            myMerge.persist(commits);
            File currBranch = Utils.join(branches, currentBranch);
            Utils.writeContents(currBranch, myMerge.getHash());
            currStage.getStagePath().delete();
            if (mergeConflict) {
                System.out.println("Encountered a merge conflict.");
            }
        }
    }


    /** Checks out the given file from the HEAD commit.
     *
     * @param fileName The file to checkout.
     * @throws IOException
     */
    private static void checkoutHeadFile(String fileName) throws IOException {
        String headCommitID = Utils.readContentsAsString(head);
        Commit headCommit = Utils.readObject(
                Utils.join(commits, headCommitID), Commit.class);
        if (headCommit.getStage() != null && headCommit.getStage().getBlobNames().containsKey(
                Utils.join(CWD, fileName).getName())) {
            Blob checkoutBlob = headCommit.getStage().getBlobNames().get(
                    Utils.join(CWD, fileName).getName());
            String newData = checkoutBlob.getBlobString();
            if (!Utils.join(CWD, fileName).exists()) {
                Utils.join(CWD, fileName).createNewFile();
            }
            Utils.writeContents(Utils.join(CWD, fileName), newData);
        } else {
            throw new GitletException(
                    "File does not exist in that commit.");
        }
    }

    /** Checks out the specified file from the specified commit.
     *
     * @param commitID The commit from which to checkout
     * @param fileName The file to checkout
     * @throws IOException Not used
     */
    private static void checkoutCommitFile(String commitID, String fileName)
            throws IOException {
        if (commitID.length() < ID_LENGTH) {
            for (File possible : commits.listFiles()) {
                if (possible.getName().contains(commitID)) {
                    commitID = possible.getName();
                    break;
                }
            }
        }
        File readCommit = Utils.join(commits, commitID);
        if (!readCommit.exists()) {
            throw new GitletException("No commit with that id exists.");
        }
        Commit chCommit = Utils.readObject(readCommit, Commit.class);
        File checkoutFile = Utils.join(CWD, fileName);
        if (chCommit.getStage().getBlobNames().containsKey(
                checkoutFile.getName())) {
            Blob checkoutBlob = chCommit.getStage().getBlobNames().get(
                    checkoutFile.getName());
            String newData = checkoutBlob.getBlobString();
            if (!checkoutFile.exists()) {
                checkoutFile.createNewFile();
            }
            Utils.writeContents(checkoutFile, newData);
        } else {
            throw new GitletException(
                    "File does not exist in that commit.");
        }
    }

    /** Checks out the specified branch.
     *
     * @param branch The branch to checkout
     */
    private static void checkoutBranch(String branch) {
        if (!Utils.join(branches, branch).exists()) {
            throw new GitletException("No such branch exists.");
        } else if (branch.equals(
                Utils.readContentsAsString(workingBranch))) {
            throw new GitletException(
                    "No need to checkout the current branch.");
        }
        Commit repoHead = Utils.readObject(Utils.join(
                commits, Utils.readContentsAsString(head)), Commit.class);
        File branchHeadPath = Utils.join(branches, branch);
        String branchHeadHash = Utils.readContentsAsString(branchHeadPath);
        Commit branchHead = Utils.readObject(Utils.join(
                commits, branchHeadHash), Commit.class);
        if (branchHead.getStage() != null) {
            for (String name : branchHead.getStage().
                    getTrackedFiles().keySet()) {
                File toWrite = Utils.join(CWD, name);
                if (toWrite.exists()) {
                    if (repoHead.getStage() == null
                            || !repoHead.getStage().getTrackedFiles().
                            containsKey(name)) {
                        throw new GitletException("There is an untracked file"
                                + " in the way; delete it, "
                                + "or add and commit it first.");
                    }
                } else {
                    try {
                        toWrite.createNewFile();
                    } catch (IOException dummy) {
                        return;
                    }
                }
                Utils.writeContents(
                        toWrite, branchHead.getStage().getTrackedFiles().
                                get(name).getBlobString());
            }
        }
        if (repoHead.getStage() != null) {
            for (String fName : repoHead.getStage().getTrackedFiles().keySet()) {
                File toDelete = Utils.join(CWD, fName);
                if (branchHead.getStage() == null) {
                    Utils.restrictedDelete(toDelete);
                } else if (!branchHead.getStage().getTrackedFiles().
                        keySet().contains(fName)) {
                    Utils.restrictedDelete(toDelete);
                }
            }
        }
        new StagingArea(gitletDir).getStagePath().delete();
        Utils.writeContents(workingBranch, branch);
        Utils.writeContents(head, Utils.readContentsAsString(
                Utils.join(branches, branch)));
    }

    /** Finds the split point of the two commits. Used in the merge command
     *
     * @param givenBranch The head hash of the given branch.
     * @param currentBranch The head hash of the current branch.
     * @return The commit ID of the split Point
     */
    private static String findSplitPoint(String givenBranch, String currentBranch) {
        HashMap<String, Commit> givenBranchCommits = new HashMap<>();
        HashMap<String, Commit> currentBranchCommits = new HashMap<>();
        Commit givenBranchHead = Utils.readObject(Utils.join(commits, givenBranch),
                Commit.class);
        Commit currentBranchHead = Utils.readObject(Utils.join(commits, currentBranch),
                Commit.class);
        givenBranchCommits.put(givenBranchHead.getHash(), givenBranchHead);
        currentBranchCommits.put(currentBranchHead.getHash(), currentBranchHead);
        addAncestors(givenBranchHead, givenBranchCommits);
        addAncestors(currentBranchHead, currentBranchCommits);
        Set<String> commonAncestorKeys = new HashSet<String>(givenBranchCommits.keySet());
        commonAncestorKeys.retainAll(currentBranchCommits.keySet());
        Set<String> commonAncestorCopy = new HashSet<>(commonAncestorKeys);
        for (String ancestorHash : commonAncestorCopy) {
            Commit ancestorCommit = currentBranchCommits.get(ancestorHash);
            if (commonAncestorKeys.contains(ancestorCommit.getParentUID())) {
                Commit toRemove = currentBranchCommits.get(ancestorCommit.getParentUID());
                while (commonAncestorKeys.contains(toRemove.getParentUID())) {
                    commonAncestorKeys.remove(toRemove.getHash());
                    toRemove = currentBranchCommits.get(toRemove.getParentUID());
                }
                commonAncestorKeys.remove(toRemove.getHash());
            }
        }
        if (commonAncestorKeys.size() > 1) {
            int minDist = Integer.MAX_VALUE;
            String splitPoint = "";
            for (String key : commonAncestorKeys) {
                Commit headCommit = Utils.readObject(Utils.join(commits, currentBranch), Commit.class);
                int dist = getDistance(headCommit, key);
                if (minDist == Integer.MAX_VALUE && dist == minDist) {
                    splitPoint = key;
                } else if (Math.min(dist, minDist) == dist) {
                    minDist = dist;
                    splitPoint = key;
                }
            }
            commonAncestorKeys = new HashSet<>();
            commonAncestorKeys.add(splitPoint);
        }
        if (commonAncestorKeys.contains(givenBranch)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (commonAncestorKeys.contains(currentBranch)) {
            return "checkout";
        } else {
            List<String> temp = new ArrayList<String>();
            temp.addAll(commonAncestorKeys);
            return temp.get(0);
        }
        return null;
    }
    private static void addAncestors(Commit head, HashMap<String, Commit> branchAncestors) {
        if (head instanceof InitialCommit) {
            branchAncestors.put(head.getHash(), head);
            return;
        } else if (head instanceof MergeCommit) {
            branchAncestors.put(head.getHash(), head);
            addAncestors(Utils.readObject(Utils.join(commits, head.getParentUID()), Commit.class), branchAncestors);
            addAncestors(Utils.readObject(Utils.join(commits, ((MergeCommit) head).getSecondaryParentUID()), Commit.class), branchAncestors);
        } else {
            branchAncestors.put(head.getHash(), head);
            addAncestors(Utils.readObject(Utils.join(commits, head.getParentUID()), Commit.class), branchAncestors);
            return;
        }
    }

    private static int getDistance(Commit head, String key) {
        if (head.getHash().equals(key)) {
            return 0;
        } else if (head instanceof InitialCommit) {
            return Integer.MAX_VALUE;
        } else if (head instanceof MergeCommit) {
           Commit firstParent = Utils.readObject(Utils.join(commits, head.getParentUID()), Commit.class);
           Commit secondParent = Utils.readObject(Utils.join(commits, ((MergeCommit) head).getSecondaryParentUID()), Commit.class);
           int childDist = Math.min(getDistance(firstParent, key), getDistance(secondParent, key));
           if (childDist == Integer.MAX_VALUE) {
               return Integer.MAX_VALUE;
           } else {
               return 1 + childDist;
           }
        } else {
            int childDist = getDistance(Utils.readObject(Utils.join(commits, head.getParentUID()), Commit.class), key);
            if (childDist == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            } else {
                return 1 + childDist;
            }
        }
    }
    private static HashMap<String, Blob> getModifiedFiles(StagingArea splitStage, StagingArea branchStage) {
        HashMap<String, Blob> modifiedFiles = new HashMap<>();
        if (splitStage != null) {
            for (String bName : branchStage.getTrackedFiles().keySet()) {
                if (splitStage.getTrackedFiles().containsKey(bName)) {
                    if (!splitStage.getTrackedFiles().get(bName).getHash().equals(branchStage.getTrackedFiles().get(bName).getHash())) {
                        modifiedFiles.put(bName, branchStage.getTrackedFiles().get(bName));
                    }
                } else {
                    modifiedFiles.put(bName, branchStage.getTrackedFiles().get(bName));
                }
            }
            for (String bName : splitStage.getTrackedFiles().keySet()) {
                if (branchStage.getRemovedFiles().contains(bName)) {
                    modifiedFiles.put(bName, null);
                }
            }
        } else {
            for (String tFile : branchStage.getTrackedFiles().keySet()) {
                modifiedFiles.put(tFile, branchStage.getTrackedFiles().get(tFile));
            }
            for (String rFile : branchStage.getRemovedFiles()) {
                modifiedFiles.put(rFile, null);
            }
        }
        return modifiedFiles;
    }

    public static void addRemote(String[] args) {
        if (!gitletDir.exists()) {
            throw new GitletException("Not in an initialized Gitlet directory.");
        } else {
            if (!remotesDir.exists()) {
                remotesDir.mkdir();
            }
            File remote = Utils.join(remotesDir, args[1]);
            if (remote.exists()) {
                throw new GitletException("A remote with that name already exists.");
            } else {
                try {
                    remote.createNewFile();
                } catch (IOException dummy) {
                    return;
                }
                String remotePath = args[2].replace('/', File.separatorChar);
                Utils.writeContents(remote, remotePath);
            }
        }
    }

    public static void rmRemote(String[] args) {
        if (!gitletDir.exists()) {
            throw new GitletException("Not in an initialized Gitlet directory.");
        } else {
            File remote = Utils.join(remotesDir, args[1]);
            if (!remote.exists()) {
                throw new GitletException("A remote with that name does not exist.");
            } else {
                remote.delete();
            }
        }
    }

    public static void push(String[] args) {
        if (!gitletDir.exists()) {
            throw new GitletException("Not in an initialized Gitlet directory.");
        } else {
            File remote = Utils.join(remotesDir, args[1]);
            if (!remote.exists()) {
                throw new GitletException("A remote with that name does not exist.");
            } else {
                remote.delete();
            }
        }
    }

    public static void fetch(String[] args) {
        if (!gitletDir.exists()) {
            throw new GitletException("Not in an initialized Gitlet directory.");
        } else {
            File remote = Utils.join(remotesDir, args[1]);
            if (!remote.exists()) {
                throw new GitletException("A remote with that name does not exist.");
            } else {
                remote.delete();
            }
        }
    }

    public static void pull(String[] args) {
        if (!gitletDir.exists()) {
            throw new GitletException("Not in an initialized Gitlet directory.");
        } else {
            File remote = Utils.join(remotesDir, args[1]);
            if (!remote.exists()) {
                throw new GitletException("A remote with that name does not exist.");
            }
            String remoteDir = Utils.readContentsAsString(remote);
            if (!new File(remoteDir).exists()) {
                throw new GitletException("Remote directory not found.");
            }
        }
    }
    /** Commit ID Length. */
    static final int ID_LENGTH = 40;
}
