package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Repository.INDEX_FILE;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class Stage implements Serializable {
    /**
     * blob path - blob id
     */
    protected Map<String, String> pathIdMap = new HashMap<>();

    public Stage() {

    }

    public static Stage readStage() {
        return Utils.readObject(Repository.INDEX_FILE, Stage.class);
    }

    public boolean containsBlob(Blob blob) {
        return pathIdMap.containsKey(blob.filepath());
    }

    public void addBlob(Blob blob) {
        pathIdMap.put(blob.filepath(), blob.id());
    }

    public void save() {
        Utils.writeObject(INDEX_FILE, this);
    }

    /**
     * 清空 索引区
     */
    public void clear() {
       pathIdMap.clear();
    }

    public boolean isEmpty() {
        return pathIdMap.isEmpty();
    }
}
