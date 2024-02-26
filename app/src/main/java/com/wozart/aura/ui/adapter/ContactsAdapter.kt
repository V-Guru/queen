package com.wozart.aura.aura.ui.adapter


import android.app.Activity
import android.app.Dialog
import com.google.android.material.textfield.TextInputLayout
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.Glide
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.User
import com.wozart.aura.aura.utilities.Utils
import kotlinx.android.synthetic.main.item_select_contact.view.*
import java.util.*

class ContactsAdapter(private val activity: Activity, contacts: List<User>, private val listener: VerticalListListener) : androidx.recyclerview.widget.RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {
    private var contacts: MutableList<User> = ArrayList()
    private var selectedContacts: MutableList<User> = ArrayList()

    init {
        this.contacts.clear()
        this.contacts.addAll(contacts)
    }

    fun update(contacts: MutableList<User>) {
        updateContacts(contacts)
    }

    fun updateContacts(contacts: MutableList<User>) {

        this.contacts = contacts

        notifyDataSetChanged()
    }

    fun updateSelectedContacts(users: MutableList<User>) {
        this.selectedContacts = ArrayList()
        this.selectedContacts = users
        notifyDataSetChanged()
    }

    fun update(contacts: MutableList<User>, selectedContacts: MutableList<User>) {
        this.selectedContacts = ArrayList()
        this.selectedContacts = selectedContacts
        updateContacts(contacts)
    }

    fun checkAll(isSelectAll: Boolean) {
        this.selectedContacts = ArrayList()
        selectedContacts.clear()
        if (isSelectAll) {
            for (contact in contacts) {
                if (contact.email != null)
                    selectedContacts.add(contact)
            }
        }
        notifyDataSetChanged()
    }

    fun getSelectedContacts(): List<User> {
        return selectedContacts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_select_contact, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactsAdapter.ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.itemView?.tv_contact_name?.text = contact.firstName

        holder.itemView.img_contact.setImageDrawable(holder.itemView?.context?.getDrawable(Utils.getImage("round_account",
                holder.itemView.context)))
        //Load image from image URI
           // Glide.with(holder.itemView.context).load(contact.profileImage).into(holder.itemView.img_contact)
        if (contact.email != null && !contact.email!!.equals("", ignoreCase = true)) {
            holder.itemView.tv_email?.visibility = View.VISIBLE
            holder.itemView.tv_email?.text = contact.email
        } else
            holder.itemView.tv_email?.visibility = View.GONE
        holder.itemView.check_select_contact?.setOnCheckedChangeListener { _, checked ->
            if (checked && !selectedContacts.contains(contact)) {

                if (isDuplicateEmail(selectedContacts, contact)) {
                    holder.itemView.check_select_contact?.isChecked = false
                    Toast.makeText(activity, "Duplicate Email", Toast.LENGTH_LONG).show()
                } else {

                    selectedContacts.add(0, contact)
                }


            } else if (!checked) {
                selectedContacts.remove(contact)
            }

            listener.onVerticalListChecked(selectedContacts)
        }
        holder.itemView.setOnClickListener {
            if (contact.email == null || contact.email!!.isEmpty()) {
                openAddEmailDialog(holder, contact)
            } else {
                holder.itemView?.check_select_contact?.isChecked = !holder.itemView.check_select_contact.isChecked
            }
        }
        holder.itemView?.check_select_contact?.isChecked = selectedContacts.contains(contact)
        holder.itemView?.check_select_contact?.isEnabled = !TextUtils.isEmpty(contact.email)
    }

    private fun isDuplicateEmail(users: List<User>, user: User?): Boolean {

        for (user1 in users) {
            return user != null && user1.email != null && user.email != null && user.email!!.equals(user1.email!!, ignoreCase = true)
        }
        return false
    }

    override fun getItemCount(): Int {
        return if (contacts.isEmpty()) 0 else contacts.size
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface VerticalListListener {
        fun onVerticalListChecked(selectedUsers: MutableList<User>)
    }

    private fun openAddEmailDialog(holder: ViewHolder, contact: User) {

        val dialog = Dialog(holder.itemView.context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_request_email)
        val inputEmail = dialog.findViewById<View>(R.id.input_email) as EditText
        //val tilEmail = dialog.findViewById<View>(R.id.til_email) as TextInputLayout

        val btnCancel = dialog.findViewById<View>(R.id.btn_cancel) as Button
        btnCancel.setOnClickListener { dialog.dismiss() }

        val btnAdd = dialog.findViewById<View>(R.id.btn_add) as Button
        btnAdd.setOnClickListener {
            val email = inputEmail.text.toString()

            contact.email = email
            holder.itemView.tv_email?.visibility = View.VISIBLE
            holder.itemView.tv_email?.text = email
            holder.itemView.check_select_contact?.performClick()
            holder.itemView.check_select_contact?.isEnabled = true
            holder.itemView.check_select_contact?.isChecked = true
            dialog.cancel()
        }


        dialog.show()
    }
}
