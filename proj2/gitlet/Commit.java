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

    private static final long serialVersionUID = 42L;
    /**
     * The message of this Commit.
     */
    private String message;
    private String timestamp;
    private String id;
    private final List<Commit> parents = new ArrayList<>();
    private final Map<String, String> cache = new HashMap<>();


    public Commit(String msg, Date date) {
        this.message = msg;
        this.timestamp = dateConvert2Timestamp(date);
        this.id = generateID();
    }

    public void addParent(Commit blob) {
        parents.add(blob);
        cache.putAll(blob.cache);
    }

    public String getId() {
        return this.id;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getMessage() {
        return message;
    }

    public Map<String,String> getCache() {
        return cache;
    }

    public List<Commit> getParents() {
        return parents;
    }

    public boolean containsBlob(Blob blob) {
        return cache.containsKey(blob.filepath());
    }

    public boolean containsBlob(String filepath) {
        return cache.containsKey(filepath);
    }

    @Override
    public String toString() {
        return "Commit{" +
                "message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    private String generateID() {
        return Utils.sha1(message, timestamp, parents.toString(), cache.toString());
    }

    /**
     * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/SimpleDateFormat.html#
     * return Thu Nov 9 17:01:33 2017 -0800
     */
    private String dateConvert2Timestamp(Date date) {
        var dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
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


    public void removeBlob(Blob blob) {
        cache.remove(blob.filepath());
    }

    public void putAllBlob(Map<String, String> addedCache) {
        // also update current exist file id
        cache.putAll(addedCache);
    }
}
