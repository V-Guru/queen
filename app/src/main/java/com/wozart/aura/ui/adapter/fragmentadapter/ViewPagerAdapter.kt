package com.wozart.aura.ui.adapter.fragmentadapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.wozart.aura.ui.auraSense.fragment.CustomCreatedRemote
import com.wozart.aura.ui.auraSense.fragment.FavouriteChannelFragment
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant

class ViewPagerAdapter(fragmentManager: FragmentManager, var context: Context) : FragmentStatePagerAdapter(fragmentManager) {

    var recyclerItemClicked: RecyclerItemClicked? = null
    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                CustomCreatedRemote(recyclerItemClicked).newInstance()
            }
            1 -> {
                FavouriteChannelFragment()
            }
            else -> {
                CustomCreatedRemote(recyclerItemClicked).newInstance()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> Constant.INTERNET_CHANNEL_LIST

            1 -> Constant.CHANNEL_SHORTCUT

            else -> Constant.INTERNET_CHANNEL_LIST
        }
    }
}