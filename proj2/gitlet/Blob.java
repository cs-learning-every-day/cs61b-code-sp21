package gitlet;

import java.io.IOException;
import java.io.Serializable;

import static gitlet.Repository.OBJECT_BLOB_DIR;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class Blob implements Serializable {
    private static final long serialVersionUID = 42L;
    private byte[] content;
    private String filepath;
    private String id;

    public Blob(String filepath) {
        this.filepath = filepath;
        content = Utils.readContents(Utils.join(Repository.CWD, filepath));
        id = generateId();
    }

    public void setContent(byte[] c) {
        this.content = c;
    }

    private String generateId() {
        return Utils.sha1(content, filepath);
    }

    public String id() {
        return id;
    }

    public String filepath() {
        return filepath;
    }

    public void save() {
        try {
            var pathFile = Utils.join(OBJECT_BLOB_DIR, id.substring(0, 2));
            pathFile.mkdirs();
            var file = Utils.join(pathFile, id.substring(2));
            if (!file.exists()) {
                file.createNewFile();
                Utils.writeObject(file, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getContent() {
        return content;
    }

    public static Blob readBlob(String id) {
        return Utils.readObject(
                Utils.join(OBJECT_BLOB_DIR, id.substring(0, 2), id.substring(2)),
                Blob.class);
    }

    public void remove() {
        Utils.join(OBJECT_BLOB_DIR,
                        id.substring(0, 2),
                        id.substring(2))
                .delete();
    }
}
