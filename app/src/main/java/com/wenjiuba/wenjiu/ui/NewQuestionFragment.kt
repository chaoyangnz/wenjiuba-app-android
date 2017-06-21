package com.wenjiuba.wenjiu.ui

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast
import com.wenjiuba.wenjiu.Question
import com.wenjiuba.wenjiu.R
import com.wenjiuba.wenjiu.net.post
import kotlinx.android.synthetic.main.fragment_new_question.view.*
import kotlinx.android.synthetic.main.fragment_questions.*
import rx.subjects.PublishSubject

class NewQuestionFragment : FullScreenDialogFragment() {
    val questionAdded = PublishSubject.create<Question>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.fragment_new_question, null)
        dialog.setContentView(view)

        view.ask_question_close_button.setOnClickListener {
            dialog.dismiss()
        }
        view.ask_question_submit_button.setOnClickListener {
            view.ask_question_submit_button.isEnabled = false

            val title = view.ask_question_title.text.toString()
            val content = view.ask_question_content.text.toString()

            // validation
            if (title.isEmpty() || content.isEmpty()) {
                view.ask_question_submit_button.isEnabled = true
                Toast.makeText(context, "Title and description should be not empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            post("questions", mapOf<String, String>("title" to title, "content" to content), { gson, json ->
                val question = gson.fromJson<Question>(json, Question::class.java)
                Toast.makeText(context, "Save new qustion successfully", Toast.LENGTH_LONG).show()
                questionAdded.onNext(question)
                view.ask_question_submit_button.isEnabled = true
                dialog.dismiss()
            }, {
                view.ask_question_submit_button.isEnabled = true
            })
        }

        return dialog
    }
}