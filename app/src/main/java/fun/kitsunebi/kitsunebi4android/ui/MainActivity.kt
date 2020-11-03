package `fun`.kitsunebi.kitsunebi4android.ui

import `fun`.kitsunebi.kitsunebi4android.R
import `fun`.kitsunebi.kitsunebi4android.common.showToast
import `fun`.kitsunebi.kitsunebi4android.ui.servers.MainRecyclerAdapter
import `fun`.kitsunebi.kitsunebi4android.ui.servers.ServerDetailActivity
import `fun`.kitsunebi.kitsunebi4android.ui.servers.helper.SimpleItemTouchHelperCallback
import `fun`.kitsunebi.kitsunebi4android.ui.settings.SettingsActivity
import android.app.Activity
import android.content.*
import android.net.VpnService
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.v2ray.ang.dto.AngConfigManager
import io.github.rurirei.kitsunebi.ConstantUtil
import io.github.rurirei.kitsunebi.PreferencesUtil.get
import io.github.rurirei.kitsunebi.PreferencesUtil.put
import io.github.rurirei.kitsunebi.PreferencesUtil.sharedPreferences
import io.github.rurirei.v2rayng.dto.V2rayConfigManager
import io.github.rurirei.v2rayng.service.V2RayProxyService
import io.github.rurirei.v2rayng.service.V2RayVpnService
import io.github.rurirei.v2rayng.service.entity.V2RayServiceManager
import io.github.rurirei.v2rayng.util.ActivitiesUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_content.*


class MainActivity : AppCompatActivity() {

    // V2ray Manager
    private val angConfigManager: AngConfigManager by lazy { AngConfigManager(this) }
    private val v2rayConfigManager: V2rayConfigManager by lazy { V2rayConfigManager(this) }

    // RecyclerView Adapter
    private val mainRecyclerAdapter by lazy { MainRecyclerAdapter(this) }
    private val simpleItemTouchHelperCallback by lazy { SimpleItemTouchHelperCallback(mainRecyclerAdapter) }
    private val itemTouchHelper: ItemTouchHelper by lazy { ItemTouchHelper(simpleItemTouchHelperCallback) }

    private val localBroadcastManager: LocalBroadcastManager by lazy { LocalBroadcastManager.getInstance(this) }
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ConstantUtil.BROADCAST.VPN_STOPPED -> {
                    fab.setImageResource(android.R.drawable.ic_media_play)
                    // showAlert(this@MainActivity, getString(R.string.vpn_stopped))
                    showToast(this@MainActivity, getString(R.string.vpn_stopped))
                }
                ConstantUtil.BROADCAST.VPN_STARTED -> {
                    fab.setImageResource(android.R.drawable.ic_media_pause)
                    // showAlert(this@MainActivity, getString(R.string.vpn_started))
                    showToast(this@MainActivity, getString(R.string.vpn_started))
                }
                ConstantUtil.BROADCAST.VPN_START_ERR -> {
                    fab.setImageResource(android.R.drawable.ic_media_play)
                    // showAlert(this@MainActivity, getString(R.string.vpn_start_err))
                    showToast(this@MainActivity, getString(R.string.vpn_start_err))
                }
                ConstantUtil.BROADCAST.VPN_START_ERR_DNS -> {
                    fab.setImageResource(android.R.drawable.ic_media_play)
                    // showAlert(this@MainActivity, getString(R.string.vpn_start_err_dns))
                    showToast(this@MainActivity, getString(R.string.vpn_start_err_dns))
                }
                ConstantUtil.BROADCAST.VPN_START_ERR_CONFIG -> {
                    fab.setImageResource(android.R.drawable.ic_media_play)
                    // showAlert(this@MainActivity, getString(R.string.vpn_start_err_config))
                    showToast(this@MainActivity, getString(R.string.vpn_start_err_config))
                }
                ConstantUtil.BROADCAST.PONG -> {
                    fab.setImageResource(android.R.drawable.ic_media_pause)
                    // showAlert(this@MainActivity, getString(R.string.pong))
                    showToast(this@MainActivity, getString(R.string.pong))
                }
            }
        }
    }

    private fun startV2Ray() {
        V2RayServiceManager.startService(this)
    }

    // TODO: stop Button not work
    // use STOP Intent over notification temporarily
    private fun stopV2Ray() {
        V2RayServiceManager.stopService(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = mainRecyclerAdapter
        itemTouchHelper.attachToRecyclerView(recycler_view)

        fab.setOnClickListener {
            if (V2RayVpnService.isRunning || V2RayProxyService.isRunning) {
                stopV2Ray()
            } else {
                val intent = VpnService.prepare(this)
                if (intent == null) {
                    startV2Ray()
                } else {
                    startActivityForResult(intent, ConstantUtil.INTENT.REQUEST_VPN_PREPARE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ConstantUtil.INTENT.REQUEST_FILE_CHOOSER -> {
                    (data?.data ?: return).let { uri ->
                        contentResolver.openInputStream(uri)?.buffered()?.use {
                            try {
                                v2rayConfigManager.addServer(it.bufferedReader().readText())
                                angConfigManager.addServer()
                            } catch (ignored: NullPointerException) {
                            } catch (ignored: com.google.gson.JsonSyntaxException) {
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            it.close()
                        }
                    }
                }
                ConstantUtil.INTENT.REQUEST_VPN_PREPARE -> {
                    startV2Ray()
                }
                else -> {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ConstantUtil.BROADCAST.LIST.forEach { localBroadcastManager.registerReceiver(broadcastReceiver, IntentFilter(it)) }
        mainRecyclerAdapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        localBroadcastManager.unregisterReceiver(broadcastReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_clipboard_btn -> {
                try {
                    v2rayConfigManager.addServer((getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip?.getItemAt(0)?.text as String)
                    angConfigManager.addServer()
                } catch (ignored: NullPointerException) {
                } catch (ignored: com.google.gson.JsonSyntaxException) {
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mainRecyclerAdapter.notifyDataSetChanged()
                true
            }
            R.id.new_file_btn -> {
                try {
                    Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "application/json"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }.let {
                        startActivityForResult(it, ConstantUtil.INTENT.REQUEST_FILE_CHOOSER)
                    }
                } catch (ignored: NullPointerException) {
                } catch (ignored: com.google.gson.JsonSyntaxException) {
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mainRecyclerAdapter.notifyDataSetChanged()
                true
            }
            R.id.new_manually_btn -> {
                ActivitiesUtil.startActivity(this, ServerDetailActivity(), angConfigManager.angConfigsCount ?: 0)
                mainRecyclerAdapter.notifyDataSetChanged()
                true
            }
            R.id.settings_btn -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
