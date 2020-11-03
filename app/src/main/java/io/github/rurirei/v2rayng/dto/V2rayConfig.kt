package io.github.rurirei.v2rayng.dto

import io.github.rurirei.kitsunebi.GsonUtil

data class V2rayConfig(
        val stats: StatsBean? = StatsBean(),
        val log: LogBean? = LogBean(),
        val policy: PolicyBean? = PolicyBean(),
        val dns: DnsBean? = null,
        val routing: RoutingBean? = null,
        val reverse: ReverseBean? = null,
        val inbounds: ArrayList<InboundBean>,
        val outbounds: ArrayList<OutboundBean>) {

    override fun toString(): String {
        return GsonUtil.fromV2ray(this) ?: ""
    }

    data class DnsBean(val tag: String?,
                       val clientIp: String?,
                       val servers: ArrayList<ServersBean>?,
                       val hosts: Map<String, String>?
    ) {
        data class ServersBean(val address: String,
                               val port: Int?,
                               val domains: List<String>?,
                               val expectIPs: List<String>?)
    }

    data class RoutingBean(val domainStrategy: String = "AsIs",
                           val rules: ArrayList<RulesBean>,
                           val balancer: ArrayList<BalancerBean>?,
                           val strategy: String = "rules") {
        data class RulesBean(val type: String = "field",
                             val ip: List<String>?,
                             val domain: List<String>?,
                             val protocol: List<String>?,
                             val source: List<String>?,
                             val user: List<String>?,
                             val network: String?,
                             val attrs: String?,
                             val port: String?,
                             val sourcePort: String?,
                             val outboundTag: String?,
                             val balancerTag: String?,
                             val inboundTag: List<String>?)
        data class BalancerBean(val tag: String?,
                                val selector: List<String>)
    }

    data class ReverseBean(val bridges: ArrayList<BridgesBean>?,
                           val portals: ArrayList<PortalsBean>?) {
        data class BridgesBean(val tag: String, val domain: String)
        data class PortalsBean(val tag: String, val domain: String)
    }

    data class StatsBean(val `null`: String? = null)

    data class LogBean(val access: String? = null,
                       val error: String? = null,
                       val loglevel: String? = "none")

    data class PolicyBean(val levels: Map<String, LevelBean>? = mapOf(Pair("0", LevelBean())),
                          val system: SystemBean? = SystemBean()) {
        data class LevelBean(
                val handshake: Int? = 4,
                val connIdle: Int? = 300,
                val uplinkOnly: Int? = 2,
                val downlinkOnly: Int? = 0,
                val bufferSize: Int? = 0,
                val statsUserUplink: Boolean? = true,
                val statsUserDownlink: Boolean? = true)
        data class SystemBean(
                val statsInboundUplink: Boolean? = true,
                val statsInboundDownlink: Boolean? = true,
                val statsOutboundUplink: Boolean? = true,
                val statsOutboundDownlink: Boolean? = true
        )
    }

    data class InboundBean(
            val tag: String?,
            val port: Int,
            val protocol: String,
            val listen: String,
            val settings: SettingsBean,
            val sniffing: SniffingBean? = SniffingBean(),
            val allocate: AllocateBean? = null) {

        data class SettingsBean(val auth: String?,
                                val udp: Boolean?,
                                val allowTransparent: Boolean?,
                                val followRedirect: Boolean?,
                                val accounts: ArrayList<AccountBean>?,
                                val users: ArrayList<UsersBean>?,
                                val userLevel: Int?,
                                val address: String?,
                                val ip: String?,
                                val port: Int?,
                                val network: String?,
                                val timeout: Int?,
                                val level: Int?,
                                val method: String?,
                                val password: String?,
                                val ota: Boolean?,
                                val clients: ArrayList<ClientsBean>?,
                                val decryption: String?,
                                val disableInsecureEncryption: Boolean?,
                                val fallbacks: ArrayList<FallbacksBean>?) {
            data class AccountBean(val user: String,
                                   val pass: String
            )
            data class UsersBean(val secret: String,
                                 val level: Int?
            )
            data class ClientsBean(val id: String,
                                   val alterId: Int?,
                                   val level: Int?
            )
            data class FallbacksBean(val alpn: String?,
                                     val path: String?,
                                     val dest: Int,
                                     val xver: Int?
            )
        }

        data class SniffingBean(val enabled: Boolean = false,
                                val destOverride: List<String> = listOf("http", "tls")
        )

        data class AllocateBean(val strategy: String,
                                val refresh: Int,
                                val concurrency: Int
        )
    }

    data class OutboundBean(val tag: String?,
                            val protocol: String,
                            val sendThrough: String?,
                            val settings: SettingsBean,
                            val streamSettings: StreamSettingsBean?,
                            val proxySettings: ProxySettingsBean?,
                            val mux: MuxBean? = MuxBean()) {

        data class SettingsBean(val vnext: ArrayList<VNextBean>?,
                                val servers: ArrayList<ServersBean>?,
                                val userLevel: Int?,
                                val network: String? = null,
                                val address: String? = null,
                                val port: Int? = null,
                                val domainStrategy: String? = null,
                                val redirect: String? = null,
                                val response: ResponseBean? = null) {

            data class VNextBean(val address: String,
                                 val port: Int,
                                 val users: ArrayList<UsersBean>) {
                // VMESS: security
                // VLess: encryption
                data class UsersBean(val id: String,
                                     val alterId: Int?,
                                     val vmessAEAD: Boolean?,
                                     val level: Int?,
                                     val security: String?,
                                     val encryption: String?)
            }

            data class ServersBean(val address: String,
                                   val port: Int,
                                   val users: ArrayList<UsersBean>?,
                                   val level: Int?,
                                   val method: String?,
                                   val password: String?,
                                   val ota: Boolean?) {
                data class UsersBean(val user: String,
                                     val pass: String,
                                     val level: Int?)
            }

            data class ResponseBean(val type: String = "none")
        }

        data class StreamSettingsBean(val network: String,
                                      val security: String,
                                      val tlsSettings: TlsSettingsBean?,
                                      val tcpSettings: TcpSettingsBean?,
                                      val kcpSettings: KcpSettingsBean?,
                                      val wsSettings: WsSettingsBean?,
                                      val httpSettings: HttpSettingsBean?,
                                      val dsSettings: DomainSocketSettingsBean?,
                                      val quicSettings: QuicSettingBean?
        ) {

            data class TlsSettingsBean(val allowInsecure: Boolean?,
                                       val serverName: String?,
                                       val alpn: List<String>? = listOf("http/1.1"))

            data class TcpSettingsBean(val acceptProxyProtocol: Boolean? = false,
                                       val header: HeaderBean? = HeaderBean()) {
                data class HeaderBean(val type: String = "none")
            }

            data class KcpSettingsBean(val mtu: Int = 1350,
                                       val tti: Int = 20,
                                       val uplinkCapacity: Int = 5,
                                       val downlinkCapacity: Int = 20,
                                       val congestion: Boolean = false,
                                       val readBufferSize: Int = 1,
                                       val writeBufferSize: Int = 1,
                                       val seed: String? = null,
                                       val header: HeaderBean? = HeaderBean()) {
                data class HeaderBean(val type: String = "none")
            }

            data class WsSettingsBean(val acceptProxyProtocol: Boolean? = false,
                                      val path: String?,
                                      val headers: HeadersBean?) {
                data class HeadersBean(val host: String)
            }

            data class HttpSettingsBean(val host: List<String>?,
                                        val path: String?)

            data class DomainSocketSettingsBean(val path: String,
                                                val abstract: Boolean)

            data class QuicSettingBean(val security: String,
                                       val key: String,
                                       val header: HeaderBean? = HeaderBean()) {
                data class HeaderBean(val type: String = "none")
            }
        }

        data class ProxySettingsBean(val tag: String)

        data class MuxBean(val enabled: Boolean = false,
                           val concurrency: Int = 8)
    }

}
