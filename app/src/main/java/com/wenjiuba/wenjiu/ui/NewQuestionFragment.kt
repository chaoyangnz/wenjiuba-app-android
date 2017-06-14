package com.wenjiuba.wenjiu.ui

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.view.View
import com.wenjiuba.wenjiu.R
import kotlinx.android.synthetic.main.fragment_new_question.view.*

class NewQuestionFragment : BottomSheetDialogFragment() {
    private var behaviour: BottomSheetBehavior<View>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.fragment_new_question, null)
        dialog.setContentView(view)
        behaviour = BottomSheetBehavior.from(view.getParent() as View)

        view.ask_question_close_button.setOnClickListener {
            dialog.dismiss()
        }
        view.ask_question_submit_button.setOnClickListener {
            //TODO
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        //默认全屏展开
        behaviour!!.state = BottomSheetBehavior.STATE_EXPANDED
    }
}