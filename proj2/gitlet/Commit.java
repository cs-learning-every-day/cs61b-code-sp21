package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public static final File OBJECT_COMMIT_DIR = Utils.join(Repository.GITLET_DIR, "objects/commits");

    public Commit(String msg, Date date, Commit parent) {
        this.message = msg;
        this.timestamp = dateConvert2Timestamp(date);
        this.parent = parent;
        this.id = generateID();
    }

    public String id() {
        return this.id;
    }

    @Override
    public String toString() {
        return "Commit{" +
                "message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    private String generateID() {
        return Utils.sha1(message, timestamp);
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

    public static Commit fromFile(String id) {
        return Utils.readObject(
                Utils.join(OBJECT_COMMIT_DIR, id.substring(0, 2), id.substring(2)),
                Commit.class);
    }
}
