package com.wozart.aura.ui.base.baseselectdevices

import com.wozart.aura.ui.createautomation.baseadapters.BaseRoomAdapter
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.item_select_room.view.*

/**
 * Created by Saif on 3/22/2018.
 */

class SelectRoomAdapter(var onClick: RecyclerItemClicked) : BaseRoomAdapter(),RecyclerItemClicked {
    override fun bindAdapter(holder: ViewHolder?, room: RoomModel) {
        holder?.itemView?.listDevices?.adapter = SelectDevicesAdapter(room.deviceList,onClick)
        holder?.itemView?.listDevices?.layoutManager = androidx.recyclerview.widget.GridLayoutManager(holder?.itemView?.context, 3)
    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
       onClick.onRecyclerItemClicked(position,data as Device,viewType)
    }

}
