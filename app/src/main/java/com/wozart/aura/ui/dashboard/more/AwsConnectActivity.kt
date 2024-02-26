package com.wozart.aura.aura.ui.dashboard.more

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.wozart.aura.R
import com.wozart.aura.aura.data.model.AwsSwitch
import com.wozart.aura.aura.ui.adapter.AwsConnectAdapter
import com.wozart.aura.aura.utilities.Utils
import kotlinx.android.synthetic.main.activity_aws_connect.*

class AwsConnectActivity : AppCompatActivity(){



    private var adapter: AwsConnectAdapter = AwsConnectAdapter()
    private var auraSwitches: MutableList<AwsSwitch> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aws_connect)
        init()
    }
    fun init() {

        adapter.init(auraSwitches)
        listSwitches.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        listSwitches.adapter = adapter
        for (i in 0..20) {
            var switch = AwsSwitch()
            switch.switchName = "Aura Switch $i"
            if(i>10 && i<14)
                switch.isEnabled=true
            auraSwitches.add(switch)
        }
        adapter.update(auraSwitches)
    }
}
