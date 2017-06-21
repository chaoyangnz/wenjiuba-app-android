package com.wenjiuba.wenjiu.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.WindowManager
import com.wenjiuba.wenjiu.R

/**
 * Created by Chao on 21/06/2017.
 */

abstract class FullScreenDialogFragment : DialogFragment() {
    override fun onResume() {
        // Get existing layout params for the window
        val params = getDialog().getWindow().getAttributes()
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        getDialog().getWindow().setAttributes(params)
        // Call super onResume after sizing
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        this.setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen)
        super.onCreate(savedInstanceState)
    }
}
