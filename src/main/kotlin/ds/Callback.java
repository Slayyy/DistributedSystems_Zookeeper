package ds;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.Arrays;

public class Callback implements AsyncCallback.StatCallback {

    private ZooKeeper zooKeeper;
    private String zNode;
    private DataMonitorListener listener;

    public Callback(ZooKeeper zooKeeper, String zNode, DataMonitorListener listener) {
        this.zooKeeper = zooKeeper;
        this.zNode = zNode;
        this.listener = listener;
    }

    byte prevData[];

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        boolean exists;
        switch (rc) {
            case KeeperException.Code.Ok:
                exists = true;
                break;
            case KeeperException.Code.NoNode:
                exists = false;
                break;
            case KeeperException.Code.SessionExpired:
            case KeeperException.Code.NoAuth:
                listener.closing(rc);
                return;
            default:
                zooKeeper.exists(zNode, true, this, null);
                return;
        }

        byte b[] = null;
        if (exists) {
            try {
                b = zooKeeper.getData(zNode, false, null);
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        if ((b == null && b != prevData) || (b != null && !Arrays.equals(prevData, b))) {
            listener.exists(b);
            prevData = b;
        }
    }
}
