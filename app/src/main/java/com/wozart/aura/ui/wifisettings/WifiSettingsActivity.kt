package com.wozart.aura.ui.wifisettings

import android.content.Intent
import android.os.Bundle

import com.wozart.aura.R
import com.wozart.aura.ui.auraswitchlist.AuraListActivity
import com.wozart.aura.aura.ui.wifisettings.OnFragmentInteractionListener
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity

class WifiSettingsActivity : BaseAbstractActivity(), OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        val themeId: Int = Utils.getThemeId(applicationContext)
        if (themeId != 0)
            setTheme(themeId)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_automation)
        navigateToFragment(WifiConfigurationFragment(), getString(R.string.tag_wifi_settings))
      // navigateToFragment(WifiSettingsFragment(), getString(R.string.tag_wifi_settings), true, true)
    }

    override fun onWifiSettingsButtonClick() {
        navigateToFragment(WifiConfigurationFragment(), getString(R.string.tag_wifi_settings))
    }

    override fun onSkipClick() {
        val intent = Intent(this, AuraListActivity::class.java)
        startActivity(intent)
    }
}
