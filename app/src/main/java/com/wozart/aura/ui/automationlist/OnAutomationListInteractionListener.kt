package com.wozart.aura.aura.ui.automationlist

import com.wozart.aura.data.model.AutomationModel

/**
 * Created by Niranjan P on 3/15/2018.
 */
interface OnAutomationListInteractionListener {
    fun onCreateAutomationBtnClicked()
    fun onDetailsBtnClicked(automation: AutomationModel)
    fun onDeleteBtnClicked(automation: AutomationModel)
}