package io.github.rurirei.v2rayng.service.entity.tun2socks

import `fun`.kitsunebi.kitsunebi4android.ui.settings.SettingsFragment
import android.content.Context
import io.github.rurirei.kitsunebi.PreferencesUtil.get
import io.github.rurirei.kitsunebi.PreferencesUtil.sharedPreferences
import io.github.rurirei.v2rayng.dto.V2rayConfigManager
import io.github.rurirei.v2rayng.dto.V2rayNGConfigUtil


class Tun2socksManager(private val context: Context) {

    companion object {
        const val PREF_FAKE_DNS = "pref_fake_dns"
    }

    val fakeIPRange: String get() =
            if (useFakeDns) {
                "198.18.0.1/16"
            } else {
                ""
            }

    val socksAddress: String get() =
            "127.0.0.1:$socksPort"

    val logLevel: String get() =
            context.sharedPreferences.get(SettingsFragment.PREF_LOG_LEVEL, "info")

    val useIPv6: Boolean get() =
            true

    val useFakeDns: Boolean get() =
            context.sharedPreferences.get(PREF_FAKE_DNS, false)

    private val socksPort: String get() =
        V2rayConfigManager(context).v2rayConfigStringCurrent().let {
            try {
                V2rayNGConfigUtil.parseSocksPort(it!!)
            } catch (ignored: NullPointerException) {
                ""
            } catch (ignored: com.google.gson.JsonSyntaxException) {
                ""
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

}
