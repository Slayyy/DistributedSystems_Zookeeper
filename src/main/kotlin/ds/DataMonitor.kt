package ds

import org.apache.zookeeper.AsyncCallback.StatCallback
import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper
import org.apache.zookeeper.data.Stat


class DataMonitor @Throws(KeeperException::class, InterruptedException::class)
constructor(private val zk: ZooKeeper,
            private val znode: String,
            private val chainedWatcher: Watcher?,
            private val listener: DataMonitor.DataMonitorListener)
    : Watcher, StatCallback {

    internal var prevData: ByteArray = kotlin.ByteArray(32)

    init {
        zk.exists(znode, true, this, null)
    }

    interface DataMonitorListener {
        fun exists(data: ByteArray)
        fun closing(rc: Int)
    }

    override fun process(event: WatchedEvent) {
        val path = event.path
        when (event.type) {
            Watcher.Event.EventType.None -> {
                when (event.state) {
                    Watcher.Event.KeeperState.SyncConnected -> {
                    }
                    Watcher.Event.KeeperState.Expired -> listener.closing(KeeperException.Code.SessionExpired)
                    else -> {}
                }
            }
            Watcher.Event.EventType.NodeChildrenChanged -> {
                try {
                    println(zk.getChildren(znode, this).size)
                } catch (e: KeeperException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
            else -> {
                if (path != null && path == znode) {
                    zk.exists(znode, true, this, null)
                }
            }
        }

    }


    override fun processResult(rc: Int, path: String, ctx: Any, stat: Stat) {
//        val exists: Boolean
//        when (rc) {
//            Code.Ok -> exists = true
//            Code.NoNode -> exists = false
//            Code.SessionExpired, Code.NoAuth -> {
//                listener.closing(rc)
//                return
//            }
//            else -> {
//                zk.exists(znode, true, this, null)
//                return
//            }
//        }
//
//        var b: ByteArray? = null
//        if (exists) {
//            try {
//                b = zk.getData(znode, false, null)
//            } catch (e: KeeperException) {
//                e.printStackTrace()
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
//
//        }
//        if (b == null && b != prevData || b != null && !Arrays.equals(prevData, b)) {
//            listener.exists(b)
//            prevData = b
//        }
    }
}