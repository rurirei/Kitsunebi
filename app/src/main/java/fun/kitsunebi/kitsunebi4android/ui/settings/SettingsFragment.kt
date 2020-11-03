package `fun`.kitsunebi.kitsunebi4android.ui.settings

import `fun`.kitsunebi.kitsunebi4android.BuildConfig
import `fun`.kitsunebi.kitsunebi4android.R
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.*
import io.github.rurirei.kitsunebi.PreferencesUtil.get
import io.github.rurirei.kitsunebi.PreferencesUtil.sharedPreferences
import io.github.rurirei.v2rayng.service.entity.V2RayServiceManager
import io.github.rurirei.v2rayng.service.entity.tun2socks.Tun2socksManager
import libv2ray.Libv2ray


class SettingsFragment : PreferenceFragmentCompat() ,
        SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val PREF_VERSION = "pref_version"
        const val PREF_PROXY_ONLY = V2RayServiceManager.PREF_PROXY_ONLY
        const val PREF_FAKE_DNS = Tun2socksManager.PREF_FAKE_DNS
        const val PREF_PARSE_DOMAIN_NEXT_ENABLED = "pref_parse_domain_next_enabled"
        const val PREF_LOG_LEVEL = "pref_log_level"
    }

    private val sharedPreferences: SharedPreferences by lazy { requireActivity().sharedPreferences }

    private val proxyOnly by lazy { findPreference<SwitchPreference>(PREF_PROXY_ONLY)!! }
    private val parseDomainEnabled by lazy { findPreference<SwitchPreference>(PREF_PARSE_DOMAIN_NEXT_ENABLED)!! }
    private val fakeDns by lazy { findPreference<SwitchPreference>(PREF_FAKE_DNS)!! }
    private val logLevel by lazy { findPreference<ListPreference>(PREF_LOG_LEVEL)!! }
    private val version by lazy { findPreference<Preference>(PREF_VERSION)!! }

    private fun setupInitial() {
        logLevel.summary = sharedPreferences.get(PREF_LOG_LEVEL, "info")
        version.summary = "${BuildConfig.VERSION_NAME} (${Libv2ray.version()})"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PREF_LOG_LEVEL -> {
                sharedPreferences.getString(PREF_LOG_LEVEL, "info").let {
                    logLevel.summary = it
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupInitial()
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

}
