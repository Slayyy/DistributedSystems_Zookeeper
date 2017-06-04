package ds

import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.stream.IntStream

class Executor @Throws(KeeperException::class, IOException::class, InterruptedException::class)
constructor(hostPort: String,
            private val zNode: String,
            private val exec: Array<String>)
    : Watcher, Runnable, DataMonitor.DataMonitorListener {

    private val zooKeeper: ZooKeeper = ZooKeeper(hostPort, 10000, this)
    private val dataMonitor: DataMonitor  = DataMonitor(zooKeeper, zNode, null, this)
    private var child: Process? = null

    override fun process(event: WatchedEvent) {
        dataMonitor.process(event)
    }

    override fun run() {
        val scanner = Scanner(System.`in`)
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when (line) {
                "tree" -> try {
                    show(zNode, zNode, 0)
                } catch (e: KeeperException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(KeeperException::class, InterruptedException::class)
    private fun show(child: String, path: String, i: Int) {
        IntStream.rangeClosed(0, i).forEach { _ -> print(" ") }
        println(child)
        zooKeeper.getChildren(path, dataMonitor).forEach { c ->
            try {
                show(c, path + "/" + c, i + 1)
            } catch (e: KeeperException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

    }

    override fun closing(rc: Int) {
        synchronized(this) {
            //notifyAll()
        }
    }

    internal class StreamWriter(var `is`: InputStream, var os: OutputStream) : Thread() {

        init {
            start()
        }

        override fun run() {
//            val b = ByteArray(80)
//            var rc: Int
//            try {
//                while ((rc = `is`.read(b)) > 0) {
//                    os.write(b, 0, rc)
//                }
//            } catch (ignored: IOException) {
//            }

        }
    }

    override fun exists(data: ByteArray) {
        if (data == null) {
            if (child != null) {
                println("Killing process")
                child!!.destroyForcibly()
                child!!.destroy()
                try {
                    child!!.waitFor()
                } catch (ignored: InterruptedException) {
                }

            }
            child = null
        } else {
            if (child != null) {
                println("Stopping child")
                child!!.destroy()
                try {
                    child!!.waitFor()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
            try {
                child = Runtime.getRuntime().exec(exec)
                println("Starting child ")
                StreamWriter(child!!.inputStream, System.out)
                StreamWriter(child!!.errorStream, System.err)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

}