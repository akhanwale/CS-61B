package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

/** Represents the Staging Area of the gitlet repository.
 * @author Aniruddh Khanwale */
public class StagingArea implements Serializable, Dumpable {

    /** Path to HEAD file, in case a previous stage exist. */
    private File headPath;

    /** Constructs a new staging area from the content in the specified dir.
     *
     * @param gitDir The gitlet directory used for persistence.
     */
    StagingArea(File gitDir) {
        gitletDir = gitDir;
        stagePath = Utils.join(gitDir, "stage");
        headPath = Utils.join(gitDir, "HEAD");
        try {
            if (headPath.isFile() && headPath.length() != 0) {
                copyHead();
            }
            if (stagePath.isFile() && stagePath.length() != 0) {
                copyStage();
            } else {
                stagePath.createNewFile();
            }
        } catch (IOException e) {
            return;
        }
    }

    /** Removes the specified file from the staging area.
     *
     * @param rm The file to remove.
     */
    public void rmFile(File rm) {
        boolean reasonToRemove = false;
        if (blobNames.containsKey(rm.getName())) {
            String originalHash = blobNames.get(rm.getName()).getHash();
            blobNames.remove(rm.getName());
            blobTreeMap.remove(originalHash);
            trackedFiles.remove(rm.getName());
            reasonToRemove = true;
        }
        if (headStage != null
                && headStage.getTrackedFiles().containsKey(rm.getName())) {
            trackedFiles.remove(rm.getName());
            removedFiles.add(rm.getName());
            Utils.restrictedDelete(rm);
            reasonToRemove = true;
        }
        if (!reasonToRemove) {
            throw new GitletException("No reason to remove the file.");
        }
    }

    /** Adds the given blob to the Stage.
     *
     * @param toStage the File/Blob to stage.
     */
    public void stageFile(Blob toStage) {
        if (headStage != null
                && headStage.blobTreeMap.containsKey(toStage.getHash())) {
            if (blobTreeMap.containsKey(toStage.getHash())) {
                blobTreeMap.remove(toStage.getHash());
                blobNames.remove(toStage.getName());
            }
        } else if (blobNames != null
                && blobNames.containsKey(toStage.getName())) {
            String originalHash = blobNames.get(toStage.getName()).getHash();
            blobNames.remove(toStage.getName());
            blobTreeMap.remove(originalHash);
            blobTreeMap.put(toStage.getHash(), toStage);
            blobNames.put(toStage.getName(), toStage);
            trackedFiles.put(toStage.getName(), toStage);
        } else {
            blobTreeMap.put(toStage.getHash(), toStage);
            blobNames.put(toStage.getName(), toStage);
            trackedFiles.put(toStage.getName(), toStage);
        }
        if (removedFiles.contains(toStage.getName())) {
            removedFiles.remove(toStage.getName());
        }
    }

    /** Sets the staging area to be identical to that of the parent.*/
    private void copyHead() {
        String parentCommitID = Utils.readContentsAsString(headPath);
        Commit parentCommit = Utils.readObject(Utils.join(gitletDir,
                "commits", parentCommitID), Commit.class);
        headStage = parentCommit.getStage();
        if (headStage != null) {
            trackedFiles.putAll(headStage.getTrackedFiles());
        }
        if (headStage != null && headStage.removedFiles.size() > 0) {
            for (String name : headStage.removedFiles) {
                if (size() > 0) {
                    Blob oFile = blobNames.get(name);
                    blobNames.remove(oFile.getName());
                    blobTreeMap.remove(oFile.getHash());
                }
            }
        }
    }

    /** Copies the current state of the staging area.*/
    private void copyStage() {
        StagingArea parent = Utils.readObject(stagePath, StagingArea.class);
        blobTreeMap.putAll(parent.blobTreeMap);
        blobNames.putAll(parent.blobNames);
        removedFiles.addAll(parent.getRemovedFiles());
        trackedFiles.putAll(parent.getTrackedFiles());
        for (String fName : removedFiles) {
            if (trackedFiles.containsKey(fName)) {
                trackedFiles.remove(fName);
            }
        }

    }

    /** Returns the size of the staging area. */
    int size() {
        return Math.max(Math.max(blobNames.size(), blobTreeMap.size()),
                removedFiles.size());
    }

    /** Return the gitlet directory for this staging area. */
    File getGitletDir() {
        return gitletDir;
    }
    /** Returns the staging area path. */
    File getStagePath() {
        return stagePath;
    }

    /** Returns the head path. */
    File getHeadPath() {
        return headPath;
    }

    /** Returns the blobTreeMap of this staging area. */
    TreeMap<String, Blob> getBlobTreeMap() {
        return blobTreeMap;
    }
    /** Returns the blobNames of this staging area. */
    TreeMap<String, Blob> getBlobNames() {
        return blobNames;
    }
    /** Returns the removed files of this staging area. */
    ArrayList<String> getRemovedFiles() {
        return removedFiles;
    }

    /** Returns the head's staging area. */
    StagingArea getHeadStage() {
        return headStage;
    }

    /** Returns the tracked files. */
    TreeMap<String, Blob> getTrackedFiles() {
        return trackedFiles;
    }
    /** File object containing staging area reference. */
    private File stagePath;

    /** File object containing gitlet directory reference. */
    private File gitletDir;

    /** Treemap containing all blobs in the staging area,
     * used to ensure lgN search time.  */
    private TreeMap<String, Blob> blobTreeMap = new TreeMap<String, Blob>();

    /** Treemap containing all the filenames in the staging area,
     * used to overwrite files. */
    private TreeMap<String, Blob> blobNames = new TreeMap<String, Blob>();

    /** The files currently tracked. */
    private TreeMap<String, Blob> trackedFiles = new TreeMap<String, Blob>();

    /** The files which will be removed from the next commit. */
    private ArrayList<String> removedFiles = new ArrayList<String>();

    /** Staging Area of previous Commit. */
    private StagingArea headStage;

    @Override
    public void dump() {
        if (blobNames == null && blobTreeMap == null) {
            System.out.println("No blobs added");
        } else {
            System.out.println("Blob Names :");
            System.out.println(blobNames.toString());
            System.out.println("Blob Tree Map: ");
            System.out.println(blobTreeMap.toString());
            System.out.println();
        }
    }
}
