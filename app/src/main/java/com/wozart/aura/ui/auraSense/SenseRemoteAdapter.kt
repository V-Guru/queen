package com.wozart.aura.ui.auraSense

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.utilities.Common
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.dummy_remote_layout.view.*


/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-01-30
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/

class SenseRemoteAdapter(private var context: Context, private var remote_button: MutableList<RemoteIconModel>, var type: String, var listner: (RemoteIconModel, String, Boolean) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<SenseRemoteAdapter.SenseViewHolder>() {


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SenseViewHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.dummy_remote_layout, p0, false)
        return SenseViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return remote_button.size
    }

    override fun onBindViewHolder(p0: SenseViewHolder, p1: Int) {

        p0.bind(remote_button[p1], p1, listner)
    }

    inner class SenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(remote_button_data: RemoteIconModel, position: Int, listner: (RemoteIconModel, String, Boolean) -> Unit) = with(itemView) {

            itemView.btn_control.visibility = View.GONE
            itemView.rlImage.background = itemView.context.getDrawable(R.drawable.circle_out_bg)
            if (remote_button_data.remoteButtonName == "KEY_POWER") {
                itemView.btn_control.visibility = View.VISIBLE
                itemView.ac_temp_text!!.visibility = View.GONE
                itemView.btn_control.setBackgroundResource(Utils.getAcRemoteButtonIcon(remote_button_data.remoteButtonName!!))
            } else {
                itemView.btn_control.visibility = View.GONE
                itemView.ac_temp_text!!.visibility = View.VISIBLE
                itemView.ac_temp_text!!.setText(remote_button_data.remoteButtonName)
            }

            itemView.ac_temp_text!!.setOnClickListener {
                val name_btn = remote_button_data.remoteButtonName
                if (remote_button_data.remoteButtonName == "ChanList") {
                    if (Common.numberChannelList.size == 10) {
                        //internetDialog.openInternetDialog(false)
                    } else {
                        SingleBtnDialog.with(context).setHeading(context.getString(R.string.alert)).setMessage(context.getString(R.string.please_learn_number)).show()
                    }
                } else {
                    val vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (vibrator.hasVibrator()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
                        } else {
                            vibrator.vibrate(200)
                        }

                    }
                    listner(remote_button_data, name_btn!!, false)
                }

            }

            itemView.ac_temp_text!!.setOnLongClickListener {
                val name_btn = remote_button_data.remoteButtonName
                listner(remote_button_data, name_btn!!, true)
                true
            }


            itemView.btn_control.setOnClickListener {
                if (remote_button_data.remoteButtonName == "Number") {
                    // openDialog.openNumPad(remote_button_data)
                } else if (remote_button_data.remoteButtonName == "KEY_INTERNET") {
                    //internetDialog.openInternetDialog(true)
                }
                val name_btn = remote_button_data.remoteButtonName
                val vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
                    } else {
                        vibrator.vibrate(200)
                    }

                }
                listner(remote_button_data, name_btn!!, false)
            }

            itemView.btn_control.setOnLongClickListener {
                val name_btn = remote_button_data.remoteButtonName
                listner(remote_button_data, name_btn!!, true)
                true
            }

            setLearnButtonView(remote_button_data)

        }

        private fun setLearnButtonView(remoteButtonData: RemoteIconModel) {
            if (remoteButtonData.isSelected) {
                itemView.rlImage.setBackgroundResource(R.drawable.circle_no_theme)
            } else {
                itemView.rlImage.setBackgroundResource(R.drawable.circle_gray)
            }

        }
    }

    interface OpenInternetDialog {
        fun openInternetDialog(b: Boolean)
    }
}