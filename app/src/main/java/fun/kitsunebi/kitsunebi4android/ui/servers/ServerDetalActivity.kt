package `fun`.kitsunebi.kitsunebi4android.ui.servers

import `fun`.kitsunebi.kitsunebi4android.R
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.v2ray.ang.dto.AngConfig
import com.v2ray.ang.dto.AngConfigManager
import io.github.rurirei.kitsunebi.ConstantUtil
import io.github.rurirei.v2rayng.dto.V2rayConfigManager
import io.github.rurirei.v2rayng.util.ActivitiesUtil
import kotlinx.android.synthetic.main.activity_server_detail.*


class ServerDetailActivity: AppCompatActivity() {

    private val angConfigManager: AngConfigManager by lazy { AngConfigManager(this) }
    private val angConfigs: AngConfig? get() = angConfigManager.angConfigs

    private val v2rayConfigManager: V2rayConfigManager by lazy { V2rayConfigManager(this) }

    private var index: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        try {
            index = intent.getIntExtra(ConstantUtil.INTENT.POSITION, -1)
            et_remarks.text = Editable.Factory.getInstance().newEditable(angConfigs?.getName(index))
            tv_content.text = Editable.Factory.getInstance().newEditable(v2rayConfigManager.v2rayConfigString(index))
        } catch (ignored: NullPointerException) {
        } catch (ignored: com.google.gson.JsonSyntaxException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_server_action, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        R.id.del_config -> {
            try {
                this.let {
                    AlertDialog.Builder(it).apply {
                        setTitle(R.string.del_config_comfirm)
                        setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                            if (angConfigManager.deleteServer(index)) {
                                ActivitiesUtil.toast(it, R.string.toast_success)
                            } else {
                                ActivitiesUtil.toast(it, R.string.toast_failure)
                            }
                            finish()
                        }
                        setNegativeButton(android.R.string.cancel) { dialog, _ ->
                            dialog.cancel()
                        }
                        create()
                        show()
                    }
                }
            } catch (ignored: NullPointerException) {
            } catch (ignored: com.google.gson.JsonSyntaxException) {
            } catch (e: Exception) {
                e.printStackTrace()
            }
            true
        }
        R.id.save_config -> {
            try {
                if (index == angConfigManager.angConfigsCount) {
                    v2rayConfigManager.addServer(tv_content.text.toString())
                    angConfigManager.addServer(et_remarks.text.toString())
                } else {
                    angConfigManager.editServer(index, et_remarks.text.toString())
                    v2rayConfigManager.editServer(index, tv_content.text.toString())
                }
            } catch (ignored: NullPointerException) {
            } catch (ignored: com.google.gson.JsonSyntaxException) {
            } catch (e: Exception) {
                e.printStackTrace()
            }
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}
