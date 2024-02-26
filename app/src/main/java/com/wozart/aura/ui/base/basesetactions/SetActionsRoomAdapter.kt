package com.wozart.aura.ui.base.basesetactions


import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.createautomation.baseadapters.BaseRoomAdapter
import kotlinx.android.synthetic.main.item_select_room.view.*

/**
 * Created by Niranjan P on 3/22/2018.
 */

 class SetActionsRoomAdapter : BaseRoomAdapter(){

    override fun bindAdapter(holder: ViewHolder?, room: RoomModel) {
        holder?.itemView?.listDevices?.adapter = SetActionsDevicesAdapter(room.deviceList)
    }

}
