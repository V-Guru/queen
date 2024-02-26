package com.wozart.aura.ui.wifisettings


import android.os.Bundle
import com.wozart.aura.R
import com.wozart.aura.aura.ui.wifisettings.OnFragmentInteractionListener
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity

class ZemoteActivty : BaseAbstractActivity(),OnFragmentInteractionListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_automation)

        navigateToFragment(ZmoteSettingFragment(), getString(R.string.tag_wifi_settings), true, true)
    }

    override fun onWifiSettingsButtonClick() {
        navigateToFragment(ZemoteConfigurationFragment(), getString(R.string.tag_wifi_settings))
    }

    override fun onSkipClick() {

    }
}