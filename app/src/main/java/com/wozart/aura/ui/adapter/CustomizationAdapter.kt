package com.wozart.aura.ui.adapter


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import aura.wozart.com.aura.entity.amazonaws.models.nosql.ThingTableDO
import com.wozart.aura.R
import com.wozart.aura.data.model.CustomizationList
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.dashboard.listener.UpgradeFirmwareListener
import com.wozart.aura.ui.dashboard.listener.onStartActivity
import kotlinx.android.synthetic.main.item_customization_list.view.*
import java.util.*


class CustomizationAdapter(private var context: Context, private val customizationArrayList: ArrayList<CustomizationList>, private var type: Boolean, onDeviceDeleted: onDeviceDeletedListener, private var auraSwitches: MutableList<AuraSwitch>, onStart: onActivtyStart) : androidx.recyclerview.widget.RecyclerView.Adapter<CustomizationAdapter.CustomizationViewHolder>(), CustomizationItemAdapter.onItemDeletedListener, onStartActivity {


    private var deviceListener: onDeviceDeletedListener = onDeviceDeleted
    var thing: ThingTableDO? = null
    var mDbUtils: SQLiteDatabase? = null
    private var onFinishActivity: onActivtyStart = onStart
    var upgradeListener: UpgradeFirmwareListener? = null


    override fun onItemDelete(name: String, ip: String, uiud: String, esp_dev_name: String) {
        deviceListener.onDeviceDeleted(name, ip, uiud, esp_dev_name)
    }

    class CustomizationViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomizationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_customization_list, parent, false)
        val dbUtils = UtilsDbHelper(parent.context)
        mDbUtils = dbUtils.writableDatabase
        return CustomizationViewHolder(view)
    }


    override fun onBindViewHolder(holder: CustomizationViewHolder, position: Int) {
        val customizationModel = customizationArrayList[position]
        holder.itemView.customization_label?.text = customizationModel.customizationLabel

        holder.itemView.item_recycler_view?.setHasFixedSize(true)
        holder.itemView.item_recycler_view?.isNestedScrollingEnabled = false

        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(holder.itemView.context, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        holder.itemView.item_recycler_view?.layoutManager = linearLayoutManager
        val adapter = CustomizationItemAdapter(context, position, this, customizationModel.customizationLabel, customizationModel.itemArrayList, type, this, auraSwitches, this)
        adapter.upgradeListener = upgradeListener
        holder.itemView.item_recycler_view?.adapter = adapter
    }

    override fun getItemCount(): Int {
        return customizationArrayList.size
    }

    interface onDeviceDeletedListener {
        fun onDeviceDeleted(name: String, ip: String, uiud: String, esp_dev_name: String)
    }

    interface onActivtyStart {
        fun onFinish()
    }

    override fun onStartActivity() {
        onFinishActivity.onFinish()
    }

}