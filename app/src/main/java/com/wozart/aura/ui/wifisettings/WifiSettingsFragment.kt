package com.wozart.aura.ui.wifisettings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.aura.ui.wifisettings.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_wifi_settings.*


class WifiSettingsFragment : androidx.fragment.app.Fragment() {


    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wifi_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvSkip.setOnClickListener {
            mListener?.onSkipClick()
        }
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
