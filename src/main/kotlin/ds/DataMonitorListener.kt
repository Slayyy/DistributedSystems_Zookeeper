package ds


interface DataMonitorListener {
    fun exists(data: ByteArray?)
    fun closing(rc: Int)
}
