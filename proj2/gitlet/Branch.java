package gitlet;

import java.util.Date;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class Branch extends Commit {
    /**
     * branch name
     */
    private String name;

    public Branch(String name, String msg, Date date) {
        super(msg, date);
        this.name = name;
    }
}
