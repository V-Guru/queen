package com.wozart.aura.ui.dashboard.home

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.aura.ui.dashboard.home.Home
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.utilities.Constant.Companion.EDIT_HOME
import com.wozart.aura.utilities.Constant.Companion.SELECTED_HOME
import com.wozart.aura.utilities.Constant.Companion.SHARE_HOME
import kotlinx.android.synthetic.main.item_home.view.*

/***
 * Created by Saif on 16-06-2019.
 */
class HomeListAdapter(private val homeList: ArrayList<Home>, val listener: (Home, Int) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<HomeListAdapter.HomeHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHolder {
        val inflatedView = LayoutInflater.from(parent?.context).inflate(R.layout.item_home, parent, false)
        return HomeHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return homeList.size
    }

    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        holder.bind(homeList.get(position), listener)
    }

    class HomeHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        @SuppressLint("ResourceType")
        fun bind(home: Home, listener: (Home, Int) -> Unit) = with(itemView) {
            itemView.homeTitle.text = home.title
            if (!home.sharedFlag) {
                if (home.isInActive) {
                    itemView.home_wallpaper.visibility = View.VISIBLE
                    itemView.home_wallpaper.setImageResource(Utils.getWallpaperDrawables(home.bgUrl))
                    itemView.selected_home.visibility =View.VISIBLE
                    itemView.homeTitle.setTextColor(resources.getColor(R.color.colorAccent))
                    itemView.shareHome.setImageResource(R.drawable.svg_share_active)
                    itemView.editHome.setImageResource(R.drawable.svg_edit_active)
                } else {
                    itemView.home_wallpaper.visibility = View.VISIBLE
                    itemView.selected_home.visibility =View.GONE
                    itemView.homeTitle.setTextColor(resources.getColor(R.color.colorAccent))
                    itemView.shareHome.setImageResource(R.drawable.svg_share_active)
                    itemView.home_wallpaper.setImageResource(Utils.getWallpaperDrawables(home.bgUrl))
                    itemView.editHome.setImageResource(R.drawable.svg_edit_active)
                }
            } else {
                if (home.isInActive) {
                    itemView.home_wallpaper.visibility = View.VISIBLE
                    itemView.home_wallpaper.setImageResource(Utils.getWallpaperDrawables(home.bgUrl))
                    itemView.selected_home.visibility =View.VISIBLE
                    itemView.homeTitle.setTextColor(resources.getColor(R.color.colorAccent))
                    itemView.shareHome.visibility = View.INVISIBLE
                    itemView.editHome.visibility = View.INVISIBLE
                } else {
                    itemView.home_wallpaper.visibility = View.VISIBLE
                    itemView.selected_home.visibility =View.GONE
                    itemView.homeTitle.setTextColor(resources.getColor(R.color.colorAccent))
                    itemView.home_wallpaper.setImageResource(Utils.getWallpaperDrawables(home.bgUrl))
                    itemView.shareHome.visibility = View.INVISIBLE
                    itemView.editHome.visibility = View.INVISIBLE
                }
            }

            itemView.shareHome.setOnClickListener() {
                listener(home, SHARE_HOME)
            }

            itemView.setOnClickListener {
                listener(home, SELECTED_HOME)
            }

            itemView.editHome.setOnClickListener() {
                listener(home, EDIT_HOME)
            }

        }
    }
}