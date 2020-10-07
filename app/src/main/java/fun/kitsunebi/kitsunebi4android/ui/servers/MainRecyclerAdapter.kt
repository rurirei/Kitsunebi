package `fun`.kitsunebi.kitsunebi4android.ui.servers

import `fun`.kitsunebi.kitsunebi4android.R
import `fun`.kitsunebi.kitsunebi4android.ui.MainActivity
import `fun`.kitsunebi.kitsunebi4android.ui.servers.helper.ItemTouchHelperAdapter
import `fun`.kitsunebi.kitsunebi4android.ui.servers.helper.ItemTouchHelperViewHolder
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.v2ray.ang.dto.AngConfig
import com.v2ray.ang.dto.AngConfigManager
import io.github.rurirei.v2rayng.dto.V2rayConfigUtil
import io.github.rurirei.v2rayng.util.ActivitiesUtil
import kotlinx.android.synthetic.main.servers_recycler_main.view.*


class MainRecyclerAdapter(private val mActivity: MainActivity) : RecyclerView.Adapter<MainRecyclerAdapter.BaseViewHolder>(), ItemTouchHelperAdapter {

    private companion object {
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_FOOTER = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                MainViewHolder(inflater
                        .inflate(R.layout.servers_recycler_main, parent, false))
            }
            VIEW_TYPE_FOOTER -> {
                val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                FooterViewHolder(inflater
                        .inflate(R.layout.servers_recycler_footer, parent, false))
            }
            else -> throw NoSuchElementException()
        }
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is MainViewHolder) {
            // index is the same value as position
            val name = angConfigs?.server?.get(position)?.name ?: ""

            holder.name.text = name
            holder.radio.isChecked = (position == angConfigs?.order)
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            try {
                val custom = V2rayConfigUtil.outboundAddressPort(mActivity, position.toString())
                holder.statistics.text = "${custom[0] ?: ""} : ${custom[1] ?: ""}"
            } catch (ignored: NullPointerException) {
            } catch (ignored: com.google.gson.JsonSyntaxException) {
            } catch (e: Exception) {
                e.printStackTrace()
            }

            holder.layoutEdit.setOnClickListener {
                ActivitiesUtil.startActivity(mActivity, ServerDetailActivity(), position)
            }

            holder.layoutRemove.setOnClickListener {
                mActivity.let {
                    AlertDialog.Builder(it).apply {
                        setTitle(R.string.del_config_comfirm)
                        setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                            if (angConfigManager.deleteServer(position)) {
                                ActivitiesUtil.toast(it, R.string.toast_success)
                                notifyItemRemoved(position)
                                updateSelectedItem(position)
                            } else {
                                ActivitiesUtil.toast(it, R.string.toast_failure)
                            }
                        }
                        setNegativeButton(android.R.string.cancel) { dialog, _ ->
                            dialog.cancel()
                        }
                        create()
                        show()
                    }
                }
            }

            holder.infoContainer.setOnClickListener {
                angConfigManager.activeServer(position)
                notifyDataSetChanged()
            }
        }

        if (holder is FooterViewHolder) {
            holder.layoutEdit.visibility = View.INVISIBLE
        }
    }

    open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class MainViewHolder(itemView: View) : BaseViewHolder(itemView), ItemTouchHelperViewHolder {
        val subId: TextView = itemView.tv_subid
        val radio = itemView.btn_radio!!
        val name = itemView.tv_name!!
        val testResult = itemView.tv_test_result!!
        val type = itemView.tv_type!!
        val statistics = itemView.tv_statistics!!
        val infoContainer = itemView.info_container!!
        val layoutEdit = itemView.layout_edit!!
        val layoutRemove = itemView.layout_remove!!

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(0)
        }
    }

    class FooterViewHolder(itemView: View) : BaseViewHolder(itemView), ItemTouchHelperViewHolder {
        val layoutEdit = itemView.layout_edit!!

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(0)
        }
    }

    override fun onItemDismiss(position: Int) {
    }

    override fun onItemMoveCompleted() {
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        angConfigManager.moveServer(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        updateSelectedItem(if (fromPosition < toPosition) fromPosition else toPosition)
        return true
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == angConfigs?.server?.count()) {
            VIEW_TYPE_FOOTER
        } else {
            VIEW_TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return angConfigManager.angConfigsCount ?: -1
    }

    private fun updateSelectedItem(pos: Int) {
        //notifyItemChanged(pos)
        notifyItemRangeChanged(pos, itemCount + 1 - pos)
    }

    init {
        notifyDataSetChanged()
    }

    private val angConfigManager: AngConfigManager by lazy { AngConfigManager(mActivity) }
    private val angConfigs: AngConfig? get() = angConfigManager.angConfigs

}
