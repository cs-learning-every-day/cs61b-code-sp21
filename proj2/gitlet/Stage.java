package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class Stage implements Serializable {
    /**
     * Cache blob path - blob id
     */
    private Map<String, String> cache = new HashMap<>();

    public Stage() {

    }

    public static Stage readStage(File file) {
        return Utils.readObject(file, Stage.class);
    }

    public boolean containsBlob(Blob blob) {
        return cache.containsKey(blob.filepath());
    }

    public void addBlob(Blob blob) {
        cache.put(blob.filepath(), blob.id());
    }

    /**
     * 清空 索引区
     */
    public void clear() {
        cache.clear();
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public Map<String, String> getCache() {
        return cache;
    }

    public void removeBlob(Blob b) {
        cache.remove(b.filepath());
    }
}
