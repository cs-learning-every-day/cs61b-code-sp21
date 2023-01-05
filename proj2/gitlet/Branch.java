package gitlet;

import java.io.File;
import java.io.IOException;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class Branch extends Commit {
    private static final long serialVersionUID = 42L;
    /**
     * branch name
     */
    private final String name;

    public Branch(String name, Commit commit) {
        super(commit);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void save() {
        try {
            File f = Utils.join(Repository.REF_HEADS_DIR, name);
            f.createNewFile();
            Utils.writeContents(f, getId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
