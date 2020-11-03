package io.github.rurirei.v2rayng.service.entity.vpn

import android.app.Service

interface IVPNService {

    fun getService(): Service

    fun vpnSetup()

    fun vpnShutdown()

    fun vpnProtect(socket: Int): Boolean

}
