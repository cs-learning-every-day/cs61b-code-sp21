package gitlet;

import java.util.Date;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class Branch extends Commit {
    private static final long serialVersionUID = 42L;
    /**
     * branch name
     */
    private String name;

    public Branch(String name, String msg, Date date) {
        super(msg, date);
        this.name = name;
    }
}
