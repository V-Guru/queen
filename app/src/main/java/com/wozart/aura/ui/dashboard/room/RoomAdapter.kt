package com.wozart.aura.ui.dashboard.room


import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.wozart.aura.R
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.list_room_view.view.*

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 09/08/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0.2
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class RoomAdapter(private val context: Context, onAllDeviceChecked: OnAllDevice, offAllDeviceChecked: OffAllDevice, private val roomModel: ArrayList<RoomModel>) : androidx.recyclerview.widget.RecyclerView.Adapter<RoomAdapter.RoomsViewHolder>() {

    var usertype = true
    private val localSqlUtils = UtilsTable()
    private var mDbUtils: SQLiteDatabase? = null
    var listRoom: MutableList<RoomModelJson> = ArrayList()
    var IpListDevices: MutableList<IpModel> = ArrayList()
    var ondeviceListner: OnAllDevice = onAllDeviceChecked
    var offDeviceListner: OffAllDevice = offAllDeviceChecked
    private var IP = IpHandler()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_room_view, parent, false)

        val dbUtils = UtilsDbHelper(context)
        mDbUtils = dbUtils.writableDatabase
        IpListDevices = IP.getIpDevices()

        listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")

        for (x in listRoom) {
            if (x.name == Constant.HOME) {
                if (x.sharedHome == "guest") {
                    usertype = false
                }
            }
        }

        return RoomsViewHolder(v)
    }

    override fun getItemCount(): Int {
        return roomModel.size
    }

    override fun onBindViewHolder(holder: RoomsViewHolder, position: Int) {
        val romDetails = roomModel[position]
        holder.bindRoom(romDetails)

        holder.itemView.roomCardList!!.setOnClickListener {
            val intent = Intent(context, RoomActivity::class.java)
            intent.putExtra(Constant.ROOM_NAME, romDetails.name_room)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }

        holder.itemView.switch_on.setOnClickListener {
            if (holder.text_on_off.text == "All off") {
                if(romDetails.room_total_device_count == "0"){
                    Toast.makeText(context,"${romDetails.name_room} has no device.",Toast.LENGTH_SHORT).show()
                    holder.itemView.on_switch.setImageResource(R.drawable.ic_power_off)
                }else{
                    holder.itemView.on_switch.setImageResource(R.drawable.ic_power_on)
                    ondeviceListner.onAlldevice(romDetails.name_room, true, position)
                }
            } else {
                holder.itemView.on_switch.setImageResource(R.drawable.ic_power_off)
                offDeviceListner.offAllDevice(romDetails.name_room, false, position)
            }

        }

    }

    inner class RoomsViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        private var roomView: View = itemView
        val on_switch = itemView.findViewById<ImageView>(R.id.on_switch)
        var text_on_off = itemView.findViewById<TextView>(R.id.text_on)

        fun init() {
            itemView.setOnClickListener {
                this
            }

        }

        fun bindRoom(roomModelJson: RoomModel) {

            roomView.name_room.text = roomModelJson.name_room
            roomView.totalDevicesCountTv?.text = roomModelJson.room_total_device_count
            roomView.devicesOffCountTv?.text = roomModelJson.room_deviceCount_off
            roomView.devicesOnCountTv?.text = roomModelJson.room_deviceCount_on
            if (roomModelJson.room_deviceCount_on == roomModelJson.room_total_device_count) {
                if (roomModelJson.room_total_device_count == "0") {
                    text_on_off.text = "All off"
                    on_switch.setImageResource(R.drawable.ic_power_off)
                } else {
                    text_on_off.text = "All on"
                    on_switch.setImageResource(R.drawable.ic_power_on)
                }

            } else {
                if (roomModelJson.room_total_device_count == roomModelJson.room_deviceCount_off) {
                    text_on_off.text = "All off"
                    on_switch.setImageResource(R.drawable.ic_power_off)
                }
            }
        }

    }


    interface OnAllDevice {
        fun onAlldevice(name_room: String?, b: Boolean, position: Int)
    }

    interface OffAllDevice {
        fun offAllDevice(name_room: String?, b: Boolean, position: Int)
    }

}