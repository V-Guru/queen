package com.wozart.aura.ui.dashboard.listener

interface RecyclerItemClicked {
    fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any)
}