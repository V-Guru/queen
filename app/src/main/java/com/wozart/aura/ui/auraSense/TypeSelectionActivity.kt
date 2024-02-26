package com.wozart.aura.ui.auraSense

import android.content.Intent
import android.graphics.Color.red
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.activity_type_selection.*


/**
 * Created by Saif on 12/02/21.
 * mds71964@gmail.com
 */
class TypeSelectionActivity : AppCompatActivity() {

    var applianceList = arrayOf("TV", "AC", "Projector", "Home Theatres", "Set-top box", "Joysticks", "Camera")
    var remoteData = RemoteListModel()
    var appliancesTypeSelected: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_type_selection)
        init()
        setListner()
    }

    fun init() {
//        selectType.text = setSpan(selectType.text.toString())
        val info = intent.getStringExtra("REMOTE_DATA")
        remoteData = Gson().fromJson(info, RemoteListModel::class.java)
        type_tv.adapter = ArrayAdapter(this, R.layout.spinner_item_list, applianceList)
    }

    fun setListner() {
        type_tv.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                appliancesTypeSelected = (p0!!.selectedItem as String).toString()
            }

        }
        brand_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })
        etRemoteName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })


        model_number.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })

        back_btn.setOnClickListener {
            goBack()
        }

    }

    private fun goBack() {
        startActivity(Intent(this, DownloadRemoteActivity::class.java)
                .putExtra("REMOTE_DATA", Gson().toJson(remoteData)))
        finish()
    }

    fun proceedNextScreen(View: View) {
        if (validate()) {
            val remoteListModel = RemoteListModel()
            remoteListModel.auraSenseName = remoteData.auraSenseName
            remoteListModel.remoteLocation = remoteData.remoteLocation
            remoteListModel.typeAppliances = appliancesTypeSelected
            remoteListModel.zmote_ip = remoteData.zmote_ip
            remoteListModel.remoteName = etRemoteName.text.toString()
            remoteListModel.brandName = brand_name.text.toString()
            remoteListModel.modelNumber = model_number.text.toString()
            remoteListModel.senseUiud = remoteData.senseUiud
            remoteListModel.senseThing = remoteData.senseThing
            remoteListModel.home = remoteData.home
            remoteListModel.dataType = "rNew"
            if (appliancesTypeSelected != "AC") {
                val intent = Intent(this, RemoteCreateActivity::class.java)
                intent.putExtra("remote", Gson().toJson(remoteListModel))
                intent.putExtra(Constant.REMOTE, "Create and Learn Remote")
                startActivity(intent)
                this.finish()
            } else {
                val intent = Intent(this, CreateSenseRemote::class.java)
                intent.putExtra("REMOTE_DATA", Gson().toJson(remoteListModel))
                intent.putExtra(Constant.DEVICE_TYPE, "create")
                startActivity(intent)
                this.finish()
            }
        } else {
            if (model_number.text.isNullOrEmpty()) {
                model_number.requestFocus()
                model_number.error = getString(R.string.please_enter_model)
            }
            if (brand_name.text.isNullOrEmpty()) {
                brand_name.requestFocus()
                brand_name.error = getString(R.string.please_enter_brand)
            }
            if(etRemoteName.text.isNullOrEmpty()){
                etRemoteName.requestFocus()
                etRemoteName.error = getString(R.string.please_give_name_to_remote)
            }
        }
    }

    fun setSpan(text: String): SpannableString {
        val spannableString = SpannableString(text)
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.Red)), 0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        return spannableString
    }

    private fun validate(): Boolean {
        if (model_number.text.isNullOrEmpty()) {
            return false
        }
        if (brand_name.text.isNullOrEmpty()) {
            return false
        }
        if (etRemoteName.text.isNullOrEmpty()) {
            return false
        }
        return true
    }

}