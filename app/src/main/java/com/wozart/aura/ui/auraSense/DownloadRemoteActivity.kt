package com.wozart.aura.ui.auraSense

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.R
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.JsonHelper
import com.wozart.aura.utilities.dialog.DoubleBtnDialog
import kotlinx.android.synthetic.main.activity_download_remote.*
import kotlinx.android.synthetic.main.activity_download_remote.back_btn
import kotlinx.android.synthetic.main.activity_download_remote.rlBrand
import kotlinx.android.synthetic.main.activity_type_selection.*
import kotlinx.android.synthetic.main.dialog_remote_name.*
import kotlinx.android.synthetic.main.dialog_remote_name.etRemoteName
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class DownloadRemoteActivity : AppCompatActivity(), SearchView.OnCloseListener, EspHandler.OnEspHandlerMessage {


    var appliancesTypeSelected: String? = null
    var brandSelected: String? = null
    var modelSelected: String? = null
    lateinit var spinnerModelAdapter: ArrayAdapter<String>
    lateinit var spinnerBrandAdapter: ArrayAdapter<String>
    var remoteData: String? = null
    var remote_device = RemoteListModel()
    var remoteIconList: MutableList<RemoteIconModel> = ArrayList()
    lateinit var runnable: Runnable
    var handler: Handler = Handler()
    var modelHandler: Handler = Handler()
    lateinit var modelRunnable: Runnable
    var searchText = ""
    var modelSearchText = ""
    private var selectedBrandlist: ArrayList<String> = arrayListOf()
    var selectedModelList: ArrayList<String> = arrayListOf()
    private var brandList: ArrayList<String> = arrayListOf()
    private var foundModelList: ArrayList<String> = arrayListOf()
    var espHandler: EspHandler? = null
    var IP = IpHandler()
    private lateinit var spinnerType: Spinner
    var applianceList = arrayOf("TV", "AC", "Projector", "Home Theatres", "Set-top box", "Joysticks", "Camera")

    companion object {
        var listOfTypes: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_remote)
        val info = intent.getStringExtra("REMOTE_DATA")
        spinnerType = findViewById(R.id.type_tv)
        remote_device = Gson().fromJson(info, RemoteListModel::class.java)
        for (l in IP.getIpDevices()) {
            if (l.name == remote_device.auraSenseName) {
                remote_device.senseIp = l.ip ?: ""
                break
            }
        }
        val spinnerAdapter = ArrayAdapter(this, R.layout.spinner_item_list, applianceList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = spinnerAdapter
        appliancesTypeSelected = applianceList[0]
        getRemoteModel()
        initialize()
        setListner()
        back_btn.setOnClickListener {
            onBackPressed()
        }
    }

    fun initialize() {
        runnable = Runnable {
            searchData()
        }
        modelRunnable = Runnable {
            searchModelData()
        }

    }

    private fun searchModelData() {
        selectedModelList.clear()
        val data = JSONObject(listOfTypes!!)
        val bList = data.getJSONObject(appliancesTypeSelected?:"TV")
        val models = bList.getJSONArray(brandSelected!!)
        foundModelList.clear()
        for (i in 0 until models.length()) {
            foundModelList.add(models[i].toString())
        }
        if (modelSearchText.isEmpty()) {
            selectedModelList = foundModelList
        } else {
            for (name in foundModelList.filter { it.contains(modelSearchText, ignoreCase = true) }) {
                selectedModelList.add(name)
            }
        }
        spinnerModelAdapter = ArrayAdapter(this@DownloadRemoteActivity, R.layout.spinner_item_list, selectedModelList)
        modelList.adapter = spinnerModelAdapter
        modelList.setOnItemClickListener { parent, view, position, id ->
            modelSelected = selectedModelList.get(position)
            searchModel.setQuery(modelSelected, true)
            modelHandler.removeCallbacks(modelRunnable)
            selectedModelList.clear()
            modelList.visibility = View.GONE
            remoteList.visibility = View.GONE
            DoubleBtnDialog.with(this).hideHeading()
                    .setMessage(getString(R.string.download_ask, modelSelected))
                    .setOptionPositive(getString(R.string.txt_yes))
                    .setOptionNegative(getString(R.string.txt_no))
                    .setCallback(object : DoubleBtnDialog.OnActionPerformed {
                        override fun negative() {

                        }

                        override fun positive() {
                            progress.visibility = View.VISIBLE
                            Toast.makeText(this@DownloadRemoteActivity, getString(R.string.download_started), Toast.LENGTH_SHORT).show()
                            downloadRemoteData()
                        }
                    }).show()

        }
        if (modelSearchText.isNotEmpty() && selectedModelList.size == 0) {
            ivNotFound.visibility = View.VISIBLE
            text_info.text = getString(R.string.not_found_model)
            text_info.visibility = View.VISIBLE
            btnCreate.visibility = View.VISIBLE
        }
    }

    private fun searchData() {
        selectedBrandlist.clear()
        val data = JSONObject(listOfTypes!!)
        brandList.clear()
        val brandListData = data.getJSONObject(appliancesTypeSelected)
        for (k in brandListData.keys()) {
            brandList.add(k)
        }
        if (searchText.isEmpty()) {
            selectedBrandlist = brandList
        } else {
            for (name in brandList.filter { it.startsWith(searchText, ignoreCase = true) }) {
                selectedBrandlist.add(name)
            }
        }
        spinnerBrandAdapter = ArrayAdapter(this@DownloadRemoteActivity, R.layout.spinner_item_list, selectedBrandlist)
        remoteList.adapter = spinnerBrandAdapter
        remoteList.setOnItemClickListener { parent, view, position, id ->
            brandSelected = selectedBrandlist.get(position)
            search.setQuery(brandSelected, true)
            handler.removeCallbacks(runnable, null)
            searchModelData()
            remoteList.visibility = View.GONE
            rlBrand.visibility = View.VISIBLE

        }
        if (searchText.isNotEmpty() && selectedBrandlist.size == 0) {
            ivNotFound.visibility = View.VISIBLE
            remoteList.visibility = View.GONE
            text_info.text = getString(R.string.not_found)
            text_info.visibility = View.VISIBLE
            btnCreate.visibility = View.VISIBLE

        }

    }

    private fun setListner() {
        search.setOnCloseListener {
            tvSearch.visibility = View.VISIBLE
            remoteList.visibility = View.GONE
            text_info.visibility = View.VISIBLE
            btnCreate.visibility = View.VISIBLE
            search.onActionViewCollapsed()
            true
        }
        searchModel.setOnCloseListener {
            tvSearchModel.visibility = View.VISIBLE
            remoteList.visibility = View.GONE
            modelList.visibility = View.GONE
            searchModel.onActionViewCollapsed()
            true
        }
        search.setOnSearchClickListener {
            //searchData()
            tvSearch.visibility = View.GONE
            ivNotFound.visibility = View.GONE
            text_info.visibility = View.GONE
            btnCreate.visibility = View.GONE
        }
        searchModel.setOnSearchClickListener {
            //searchModelData()
            tvSearchModel.visibility = View.GONE
            ivNotFound.visibility = View.GONE
            text_info.visibility = View.GONE
            btnCreate.visibility = View.GONE
        }
        tvSearch.setOnClickListener {
            search.isIconified = false
        }
        tvSearchModel.setOnClickListener {
            searchModel.isIconified = false
        }
        searchModel.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0.isNullOrEmpty()) {
                    modelSearchText = ""
                    modelList.visibility = View.VISIBLE
                    ivNotFound.visibility = View.GONE
                    text_info.visibility = View.GONE
                    btnCreate.visibility = View.GONE
                } else {
                    modelList.visibility = View.VISIBLE
                    ivNotFound.visibility = View.GONE
                    text_info.visibility = View.GONE
                    btnCreate.visibility = View.GONE
                    modelSearchText = p0.toString()
                    modelHandler.removeCallbacks(modelRunnable)
                    modelHandler.postDelayed(modelRunnable, 200)
                }
                return true
            }

        })
        search.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    searchText = ""
                    remoteList.visibility = View.VISIBLE
                    selectedModelList.clear()
                    rlBrand.visibility = View.GONE
                    ivNotFound.visibility = View.GONE
                    text_info.visibility = View.GONE
                    btnCreate.visibility = View.GONE
                } else {
                    remoteList.visibility = View.VISIBLE
                    ivNotFound.visibility = View.GONE
                    text_info.visibility = View.GONE
                    btnCreate.visibility = View.GONE
                    selectedModelList.clear()
                    rlBrand.visibility = View.GONE
                    searchText = newText.toString()
                    handler.removeCallbacks(runnable)
                    handler.postDelayed(runnable, 500)
                }

                return true
            }

        })

        btnCreate.setOnClickListener {
            val intent = Intent(this@DownloadRemoteActivity, TypeSelectionActivity::class.java)
            intent.putExtra("REMOTE_DATA", Gson().toJson(remote_device))
            intent.putExtra(Constant.DEVICE_TYPE, "create")
            startActivity(intent)
            this@DownloadRemoteActivity.finish()
        }

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                appliancesTypeSelected = (p0!!.selectedItem as String).toString()
            }

        }

    }

    //To get remote Entire data
    //TODO MOVE THIS TO RemoteApiCall class

    private fun downloadRemoteData() {
        thread {
            var connection: HttpURLConnection? = null
            var result: String? = null
            var reader: BufferedReader? = null
            try {
                val url = URL("https://536oh8ac6e.execute-api.ap-south-1.amazonaws.com/test")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("type", appliancesTypeSelected)
                connection.setRequestProperty("brand", brandSelected)
                connection.setRequestProperty("model", modelSelected)
                connection.connectTimeout = 30000

                val inputStream = connection.inputStream
                val buffer = StringBuffer()
                reader = BufferedReader(InputStreamReader(inputStream!!))
                var line: String? = ""
                while (line != null) {
                    line = reader.readLine()
                    buffer.append(line + "\n")
                    result += line
                }
                result = buffer.toString()
                remoteData = result
                callActivity()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                connection?.disconnect()
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (e: Exception) {

                    }
                }

            }
        }

    }

    private fun callActivity() {
        if (remoteData != null) {
            val data = JSONObject(remoteData!!)
            val model = RemoteListModel()
            remoteIconList.clear()
            val brand_name = data.getString("brandName")
            val model_number = data.getString("modelNumber")
            val type = appliancesTypeSelected
            val senseDeviceIp = remote_device.senseIp
            val list = data.getJSONArray("buttons")
            for (jsonIndex in 0 until list.length()) {
                val data_list = list.getJSONObject(jsonIndex)
                val remoteIcon = RemoteIconModel()
                remoteIcon.remoteButtonName = data_list.getString("remoteButtonName")
                remoteIcon.dType = data_list.getString("dType")
                val rArrList = data_list.getJSONArray("rArr").toString()
                val typeToken = object : TypeToken<Array<Int>>() {}.type
                remoteIcon.rArr = Gson().fromJson(rArrList, typeToken)
                remoteIcon.sz = data_list.getInt("sz")
                remoteIcon.fz = data_list.getInt("fz")
                remoteIcon.proto = data_list.getInt("proto")
                remoteIcon.state = Gson().fromJson(data_list.getJSONArray("state").toString(), typeToken)
                remoteIcon.value = data_list.getLong("value")
                remoteIcon.remoteModel = model_number
                remoteIcon.isSelected = true
                remoteIcon.name = remote_device.auraSenseName
                remoteIconList.add(remoteIcon)
                sendDataToDevice(remoteIcon, model_number, remote_device.senseUiud)
                Thread.sleep(1000)
            }
            model.auraSenseName = remote_device.auraSenseName
            model.dynamicRemoteIconList = remoteIconList
            model.brandName = brand_name
            model.modelNumber = model_number
            model.senseIp = senseDeviceIp
            model.senseUiud = remote_device.senseUiud
            model.senseThing = remote_device.senseThing
            model.home = remote_device.home
            model.remoteLocation = remote_device.remoteLocation
            model.dataType = "rOld"
            model.isDownloadedRemote = true
            model.typeAppliances = type ?: "TV"
            runOnUiThread {
                progress.visibility = View.GONE
                openDialog(model)
            }
        }
    }

    private fun sendDataToDevice(remoteButton: RemoteIconModel, modelNumber: String, senseUiud: String?) {
        val dataSend = JsonHelper().confirmRemoteData(remoteButton, senseUiud, modelNumber)
        sendEspHandler(dataSend, remote_device.senseIp, remote_device.auraSenseName!!, remoteButton.remoteButtonName!!)
    }

    private fun sendEspHandler(data: String, ip: String, name: String, type: String) {
        AppExecutors().diskIO().execute {
            try {
                if (espHandler == null) {
                    espHandler = EspHandler(this)
                }
                espHandler?.getResponseData(data, ip, name, type)
            } catch (e: Exception) {
                Log.d("error", "Error in ESP_Handler_Connection")
            }
        }
    }

    //To get type,brand,model
    //TODO MOVE THIS TO RemoteApiCall class
    private fun getRemoteModel() {
        thread {
            var connection: HttpURLConnection? = null
            var result: String? = null
            var reader: BufferedReader? = null
            try {
                val url = URL("https://536oh8ac6e.execute-api.ap-south-1.amazonaws.com/test/get-method")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 30000

                val inputStream = connection.inputStream
                val buffer = StringBuffer()
                reader = BufferedReader(InputStreamReader(inputStream!!))
                var line: String? = ""
                while (line != null) {
                    line = reader.readLine()
                    buffer.append(line + "\n")
                    result += line
                }

                result = buffer.toString()
                runOnUiThread {
                    listOfTypes = result
                    progress.visibility = View.GONE
                    text_info.visibility = View.VISIBLE
                    btnCreate.visibility = View.VISIBLE

                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                connection?.disconnect()
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (e: Exception) {
                        Log.e("ERROR", "Error closing stream", e);
                    }
                }

            }
        }
    }

    override fun onClose(): Boolean {
        remoteList.visibility = View.GONE
        text_info.visibility = View.VISIBLE
        btnCreate.visibility = View.VISIBLE
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onEspHandlerMessage(decryptedData: String, name: String) {
        runOnUiThread {

        }
    }

    private fun openDialog(model: RemoteListModel) {
        val dailog = Dialog(this)
        dailog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dailog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dailog.setContentView(R.layout.dialog_remote_name)
        dailog.setCanceledOnTouchOutside(false)
        dailog.btnContinue.setOnClickListener {
            if (dailog.etRemoteName.text!!.trim().toString().isNotEmpty()) {
                val intent = Intent(this@DownloadRemoteActivity, if (appliancesTypeSelected == "AC") SenseRemoteActivity::class.java else  RemoteCreateActivity::class.java)
                intent.putExtra("remote", Gson().toJson(model))
                intent.putExtra(Constant.REMOTE, "Remote Control")
                startActivity(intent)
                dailog.dismiss()
            } else {
                dailog.etRemoteName.requestFocus()
                dailog.etRemoteName.error = getString(R.string.please_give_name_to_remote)
            }

        }
        dailog.etRemoteName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0?.toString()!!.isNotEmpty()) {
                    model.remoteName = p0.toString()
                }
            }

        })
        dailog.show()
    }
}