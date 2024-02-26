package com.wozart.aura.utilities.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.wozart.aura.R
import com.wozart.aura.ui.adapter.ChannelListAdapter
import com.wozart.aura.ui.auraSense.AddChannelActivity
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.dialog_favourite_channel.*


/**
 * Created by Saif on 31/08/20.
 */
class InternetChannelDialog(var activity: Activity,var type: Boolean,var onSubmit:OnSubmitClick) : Dialog(activity, R.style.full_screen_dialog),View.OnClickListener,RecyclerItemClicked{

    private var remoteList : MutableList<RemoteIconModel> = ArrayList()
    lateinit var adapter : ChannelListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_favourite_channel)
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.BOTTOM)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        init()

    }

    fun init(){
        if(type){
            btnAddChannel.visibility = View.GONE
            tvTitle.visibility = View.GONE
        }
        adapter = ChannelListAdapter(activity,this)
        adapter.setData(remoteList)
        rvChannel.layoutManager = androidx.recyclerview.widget.GridLayoutManager(activity, 3)
        rvChannel.adapter = adapter
        btnAddChannel.setOnClickListener(this)
        ivClose.setOnClickListener(this)
    }

    fun setRemoteData(channelList: MutableList<RemoteIconModel>){
        this.remoteList.clear()
        this.remoteList.addAll(channelList)
    }


    interface OnSubmitClick{
        fun onSubmit(context: Context, data: Any, type: String)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnAddChannel -> {
                activity.startActivity(Intent(activity,AddChannelActivity::class.java))
                dismiss()
            }
            R.id.ivClose -> {
                dismiss()
            }
        }
    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if(data is RemoteIconModel){
            when(viewType){
                Constant.CHANEL_CREATED -> {
                    onSubmit.onSubmit(activity,data, Constant.CHANEL_CREATED)
                }
                Constant.INTERNET_CHANNEL -> {
                    onSubmit.onSubmit(activity,data, Constant.INTERNET_CHANNEL)
                }
                Constant.INTERNET_CHANNEL_LEARN -> {
                    onSubmit.onSubmit(activity,data, Constant.INTERNET_CHANNEL_LEARN)
                }
                Constant.CHANNEL_ADDED -> {
                    onSubmit.onSubmit(activity,data, Constant.CHANNEL_ADDED)
                }

            }

        }

    }

}