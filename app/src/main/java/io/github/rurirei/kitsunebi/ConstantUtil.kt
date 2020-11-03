package io.github.rurirei.kitsunebi

object ConstantUtil {

    object BROADCAST {
        const val VPN_STOPPED = "vpn_stopped"
        const val VPN_STARTED = "vpn_started"
        const val VPN_START_ERR = "vpn_start_err"
        const val VPN_START_ERR_DNS = "vpn_start_err_dns"
        const val VPN_START_ERR_CONFIG = "vpn_start_err_config"
        const val PONG = "pong"
        val LIST = listOf(
                "vpn_stopped",
                "vpn_started",
                "vpn_start_err",
                "vpn_start_err_dns",
                "vpn_start_err_config",
                "pong"
        )
        const val PING = "ping"
        const val STOP_VPN = "stop_vpn"
    }

    object INTENT {
        const val POSITION = "position"
        const val REQUEST_FILE_CHOOSER = 0
        const val REQUEST_VPN_PREPARE = 1
        const val REQUEST_FILE_EXPORT = 2
        const val REQUEST_FILE_IMPORT = 3
    }

}