package com.wozart.aura.ui.auraSense.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.ui.adapter.ChannelListAdapter
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.base.projectbase.BaseFragment
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked

class FavouriteChannelFragment : BaseFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return (inflater.inflate(R.layout.fragment_recyclerview, container, false))
    }

    var favouriteRemoteList : MutableList<RemoteIconModel> = arrayListOf()
    var activity: Activity? = null
    var adapter: ChannelListAdapter? = null
    var recyclerItemClicked: RecyclerItemClicked? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initialize()
        setListener()
    }

    fun initialize() {

    }

    private fun setListener() {

    }
}