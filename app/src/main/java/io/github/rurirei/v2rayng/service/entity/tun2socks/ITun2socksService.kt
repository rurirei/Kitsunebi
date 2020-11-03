package io.github.rurirei.v2rayng.service.entity.tun2socks

interface ITun2socksService {

    fun handlePackets(): ByteArray?

    fun packetFlow(data: ByteArray?)

}
