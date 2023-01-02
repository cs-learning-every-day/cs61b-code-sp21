package gitlet;

// TODO: any imports you need here

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Repository.OBJECT_COMMIT_DIR;

/**
 * Represents a gitlet commit object.
 *
 * @author ChillyForest
 */
public class Commit implements Serializable {

    /**
     * The message of this Commit.
     */
    private String message;
    private String timestamp;
    private String id;
    private Commit parent;
    private Map<String, String> filepathIdMap = new HashMap<>();


    public Commit(String msg, Date date) {
        parent = this;
        this.message = msg;
        this.timestamp = dateConvert2Timestamp(date);
        this.id = generateID();
    }

    public void setParent(Commit blob) {
        parent = blob;
        filepathIdMap.putAll(parent.filepathIdMap);
    }

    public String id() {
        return this.id;
    }

    public boolean containsBlob(Blob blob) {
        return filepathIdMap.containsKey(blob.filepath());
    }

    @Override
    public String toString() {
        return "Commit{" +
                "message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    private String generateID() {
        assert parent != null;
        return Utils.sha1(message, timestamp, parent.toString(), filepathIdMap.toString());
    }

    /**
     * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/SimpleDateFormat.html#
     * return 00:00:00 UTC, Thursday, 1 January 1970
     */
    private String dateConvert2Timestamp(Date date) {
        var dateFormat = new SimpleDateFormat("HH:mm:ss z, EEE, d MMM yyyy", Locale.US);
//        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    public void save() {
        try {
            var pathFile = Utils.join(OBJECT_COMMIT_DIR, id.substring(0, 2));
            pathFile.mkdirs();
            var file = Utils.join(pathFile, id.substring(2));
            file.createNewFile();
            Utils.writeObject(file, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Commit readCommit(String id) {
        return Utils.readObject(
                Utils.join(OBJECT_COMMIT_DIR, id.substring(0, 2), id.substring(2)),
                Commit.class);
    }

    public void removeBlob(Blob blob) {
        filepathIdMap.remove(blob.filepath());
    }

    public void putAllBlob(Map<String, String> addedCache) {
        // also update current exist file id
        filepathIdMap.putAll(addedCache);
    }
}
