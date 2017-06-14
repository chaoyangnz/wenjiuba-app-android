package com.wenjiuba.wenjiu.ui

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.view.View
import com.wenjiuba.wenjiu.Question
import com.wenjiuba.wenjiu.R
import kotlinx.android.synthetic.main.fragment_new_answer.view.*

class NewAnswerFragment(val question: Question) : BottomSheetDialogFragment() {
    private var behaviour: BottomSheetBehavior<View>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.fragment_new_answer, null)
        dialog.setContentView(view)
        behaviour = BottomSheetBehavior.from(view.getParent() as View)

        view.answer_question_title.text = question.title
        view.answer_question_close_button.setOnClickListener {
            dialog.dismiss()
        }
        view.answer_question_submit_button.setOnClickListener {
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