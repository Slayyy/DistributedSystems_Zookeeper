package ds

fun main(args: Array<String>) {
    if (args.size < 2) {
        System.err.println("USAGE: hostPort program [args ...]")
        System.exit(2)
    }

    val hostPort = args[0]
    val zNode = "/znode_testowy"

    try {
        Executor(hostPort, zNode, args.sliceArray(1 until args.size)).run()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}