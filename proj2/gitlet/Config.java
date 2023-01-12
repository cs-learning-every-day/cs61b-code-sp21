package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Config implements Serializable {
    private static final long serialVersionUID = 42L;

    // remote name -> remote path
    private Map<String, String> remoteMap = new HashMap<>();

    public Config() {

    }

    public boolean existRemoteName(String remoteName) {
        return remoteMap.containsKey(remoteName);
    }

    public void addRemote(String remoteName, String remotePath) {
        remoteMap.put(remoteName, remotePath);
    }

    public void removeRemote(String remoteName) {
        remoteMap.remove(remoteName);
    }
}
