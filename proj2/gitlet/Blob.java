package gitlet;

import java.io.Serializable;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class Blob implements Serializable {
    private byte[] content;
    private String filepath;
    private String id;

    public Blob(String filepath) {
        this.id = generateId();
    }

    private String generateId() {
        return Utils.sha1(content);
    }

    public void save() {

    }
}
