package ds

import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper


class DataMonitor @Throws(KeeperException::class, InterruptedException::class)
constructor(private val zooKeeper: ZooKeeper,
            private val zNode: String,
            private val listener: DataMonitorListener)
    : Watcher {

    var prevData: ByteArray? = null
    var callback = Callback(zooKeeper, zNode, listener)

    init {
        zooKeeper.exists(zNode, true, callback, null)
    }


    override fun process(event: WatchedEvent) {
        val path = event.path

        when (event.type) {
            Watcher.Event.EventType.None -> {
                when (event.state) {
                    Watcher.Event.KeeperState.Expired -> listener.closing(KeeperException.Code.SessionExpired)
                    else -> {}
                }
            }
            Watcher.Event.EventType.NodeChildrenChanged -> {
                try {
                    println(zooKeeper.getChildren(zNode, this).size)
                } catch (e: KeeperException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
            else -> if (path != null && path == zNode) {
                zooKeeper.exists(zNode, true, callback, null)
            }
        }
    }


}