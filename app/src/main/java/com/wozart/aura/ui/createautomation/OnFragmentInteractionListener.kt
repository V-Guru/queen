package com.wozart.aura.ui.createautomation

import androidx.fragment.app.Fragment

/**
 * Created by Niranjan P on 3/15/2018.
 */
interface OnFragmentInteractionListener {
    fun onHomeBtnClicked()
    fun onRoomBtnClicked()
    fun navigateToFragment(fragment: Fragment)
}