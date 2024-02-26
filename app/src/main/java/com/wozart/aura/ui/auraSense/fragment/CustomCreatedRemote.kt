package com.wozart.aura.ui.auraSense.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.wozart.aura.R
import com.wozart.aura.ui.adapter.ChannelListAdapter
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.base.projectbase.BaseFragment
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Common
import kotlinx.android.synthetic.main.fragment_recyclerview.*

class CustomCreatedRemote(var recyclerItemClicked: RecyclerItemClicked?) : BaseFragment() {

    fun newInstance(): CustomCreatedRemote {
        return CustomCreatedRemote(recyclerItemClicked)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return (inflater.inflate(R.layout.fragment_recyclerview, container, false))
    }

    var activity: Activity? = null
    var adapter: ChannelListAdapter? = null
    var internetChannelList: MutableList<RemoteIconModel> = arrayListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerview.setHasFixedSize(true)
        initialize()
        setListener()
    }

    fun initialize() {
        adapter = ChannelListAdapter(requireContext(), recyclerItemClicked!!)
        recyclerview.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerview.adapter = adapter
//        if (internetChannelList.isEmpty()) internetChannelList = Common.getDummyInternetChannel()
//        (recyclerview.adapter as ChannelListAdapter).setData(internetChannelList)
    }

    private fun setListener() {

    }

}