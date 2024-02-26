package com.wozart.aura.utilities.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.wozart.aura.R
import com.wozart.aura.ui.adapter.NumberPadAdapter
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.dialog_number_pad.*


/**
 * Created by Saif on 21/04/21.
 * mds71964@gmail.com
 */
class NumberPadDialog(context: Context) : Dialog(context, R.style.full_screen_dialog), View.OnClickListener, RecyclerItemClicked {
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ivClose -> {
                dismiss()
            }
        }
    }

    var numpadAdapter: NumberPadAdapter? = null
    var recyclerclicked: RecyclerItemClicked? = null
    var numberButtonList: MutableList<RemoteIconModel> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_number_pad)
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.BOTTOM)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        init()

    }

    fun init() {
        ivClose.setOnClickListener(this)
        numpadAdapter = NumberPadAdapter(recyclerclicked!!)
        rvNumber.adapter = numpadAdapter
        (rvNumber.adapter as NumberPadAdapter).setData(numberButtonList)

    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        when (viewType) {
            "KEY_PAD_CONTROL" -> {
                if ((data is RemoteIconModel)) {
                    if (data.isSelected) {
                        recyclerclicked?.onRecyclerItemClicked(position, data, viewType)
                    } else {
                        SingleBtnDialog.with(context).setHeading(context.getString(R.string.alert)).setMessage(context.getString(R.string.button_not_learn)).show()
                    }
                }
            }
            "KEY_PAD_LEARN" -> {
                recyclerclicked?.onRecyclerItemClicked(position, data, viewType)
            }
        }
    }
}