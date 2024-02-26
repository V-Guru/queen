package com.wozart.aura.ui.auraSense

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.activity_sense_appliances_list.*

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-02-04
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/

class DeviceListActivity : AppCompatActivity(), View.OnClickListener,RecyclerItemClicked {


    private var listAppliances: MutableList<RemoteIconModel> = ArrayList()
    lateinit var adapter: SenseAppliancesAdapter
    lateinit var appliances_item_list: androidx.recyclerview.widget.RecyclerView
    var remoteDevice = RemoteListModel()

    private var btnName = arrayOf("TV", "AC" , "Set-top box" , "Projector" , "Home Theatres")

    var appliance_images = arrayOf(R.drawable.tv_images, R.drawable.ac_images, R.drawable.projector_image, R.drawable.home_threatre, R.drawable.settop_box)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sense_appliances_list)
        val info = intent.getStringExtra("REMOTE_DATA")
        remoteDevice = Gson().fromJson(info, RemoteListModel::class.java)


        init()
    }

    fun init() {
        appliances_item_list = findViewById(R.id.list_appliances)
        back_btn.setOnClickListener(this)

        for (i in 0 until btnName.size) {
            val remote = RemoteIconModel()
            remote.remoteIconButton = appliance_images.get(i)
            remote.remoteButtonName = btnName.get(i)
            listAppliances.add(remote)

        }
        adapter = SenseAppliancesAdapter(listAppliances,this)
        appliances_item_list.adapter = adapter
        appliances_item_list.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.back_btn -> {
                this.finish()
            }
        }
    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if(data is RemoteIconModel){
            val intent = Intent(this, DownloadRemoteActivity::class.java)
            intent.putExtra("REMOTE_DATA", Gson().toJson(remoteDevice))
            intent.putExtra(Constant.APPLIANCE_TYPE,data.remoteButtonName)
            startActivity(intent)
        }
    }
}