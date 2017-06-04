package ds

import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.concurrent.thread


fun RunStreamWriterPipeThread(inputStream: InputStream, outputStream: OutputStream) {
    thread {
        val b = ByteArray(1024)
        var rc: Int
        try {
            while (true) {
                rc = inputStream.read(b)
                if (rc == 0) {
                    break
                }
                outputStream.write(b, 0, rc)
            }
        } catch (ignored: Exception) {
        }
    }
}
class Executor @Throws(KeeperException::class, IOException::class, InterruptedException::class)
constructor(hostPort: String,
            private val zNode: String,
            private val exec: Array<String>)
    :  Watcher, Runnable, DataMonitorListener {

    private val zooKeeper: ZooKeeper = ZooKeeper(hostPort, 3000, this)
    private val dataMonitor: DataMonitor  = DataMonitor(zooKeeper, zNode, this)
    private var child: Process? = null

    override fun process(event: WatchedEvent) {
        dataMonitor.process(event)
    }

    @Throws(KeeperException::class, InterruptedException::class)
    private fun show(child: String, path: String, i: Int) {
        (0 until i).forEach { _ -> print("\t") }
        println((if (i == 0) "" else "/") + child)
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

    override fun run() {
        val scanner = Scanner(System.`in`)
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when (line) {
                "show" -> try {
                    show(zNode, zNode, 0)
                } catch (e: KeeperException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun exists(data: ByteArray?) {
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
                RunStreamWriterPipeThread(child!!.inputStream, System.out)
                RunStreamWriterPipeThread(child!!.errorStream, System.err)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    override fun closing(rc: Int) {
        System.exit(0)
    }
}