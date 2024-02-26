package com.wozart.aura.aura.ui.auraswitchlist

import com.wozart.aura.entity.model.aura.AuraSwitch


interface OnAdapterInteractionListener {
    fun onSelectAuraDevice(switch: AuraSwitch)
}