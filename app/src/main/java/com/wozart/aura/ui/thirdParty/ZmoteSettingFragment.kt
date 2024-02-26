package com.wozart.aura.ui.wifisettings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.aura.ui.wifisettings.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.layout_zmote_setting.*

class ZmoteSettingFragment : androidx.fragment.app.Fragment() {
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater.inflate(R.layout.layout_zmote_setting, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        back.setOnClickListener {
            activity?.finish()
        }

        btnConfigureDevice.setOnClickListener {
            mListener?.onWifiSettingsButtonClick()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnAdapterInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }
}