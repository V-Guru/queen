package com.wozart.aura.aura.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.User
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.utilities.DialogListener
import kotlinx.android.synthetic.main.item_selected_contact.view.*

/**
 * Created by Drona Sahoo on 3/20/2018.
 */

class SelectedContactAdapter(private var contactList: MutableList<User>?, private val listener: SelectedContactListener) : androidx.recyclerview.widget.RecyclerView.Adapter<SelectedContactAdapter.ViewHolder>() {

    fun updateList(contactList: MutableList<User>) {
        this.contactList = contactList
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = this.contactList!![position]

        holder.itemView.imgThumbnail.setImageDrawable(
            holder?.itemView?.context?.getDrawable(Utils.getImage("round_account", holder?.itemView?.context!!)))
        //Load image from URI
       //     Glide.with(holder.itemView.context).load(contact.profileImage).into(holder.itemView.img_thumbnail)

        holder?.itemView?.tv_contact_name?.text = contact.firstName
        this.registerItemClickListener(holder, contact)

    }

    private fun registerItemClickListener(holder: ViewHolder, contact: User) {
        holder.itemView.setOnClickListener { onItemClicked(holder,contact) }

    }

    private fun onItemClicked(holder: ViewHolder,contact: User) {

        Utils.showCustomDialog(holder.itemView.context,"Share",holder.itemView.context.getString(R.string.dialog_message_remove_contact),R.layout.dialog_layout,object: DialogListener {
            override fun onOkClicked() {
                contactList!!.remove(contact)
                listener.onSelectedContactClick(contactList)
                notifyDataSetChanged()
            }

            override fun onCancelClicked() {

            }
        })


    }

    override fun getItemCount(): Int {
        return this.contactList!!.size
    }


    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {



    }

    interface SelectedContactListener {
        fun onSelectedContactClick(contactList: MutableList<User>?)

    }

}
