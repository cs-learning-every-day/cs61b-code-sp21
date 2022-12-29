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
    protected Map<String, String> addedCache = new HashMap<>();
    protected Map<String, String> removedCache = new HashMap<>();

    public Stage() {

    }

    public static Stage readStage() {
        return Utils.readObject(Repository.INDEX_FILE, Stage.class);
    }

    public boolean containsAddedBlob(Blob blob) {
        return addedCache.containsKey(blob.filepath());
    }

    public void addBlob(Blob blob) {
        addedCache.put(blob.filepath(), blob.id());
    }

    public void save() {
        Utils.writeObject(INDEX_FILE, this);
    }

    /**
     * 清空 索引区
     */
    public void clear() {
       addedCache.clear();
    }

    public boolean isEmpty() {
        return addedCache.isEmpty();
    }

    public void addRemovedBlob(Blob blob) {
        removedCache.put(blob.filepath(), blob.id());
    }
}
