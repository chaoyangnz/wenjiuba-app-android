package com.wenjiuba.wenjiu.ui

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.wenjiuba.wenjiu.Answer
import com.wenjiuba.wenjiu.Question
import com.wenjiuba.wenjiu.R
import com.wenjiuba.wenjiu.net.post
import kotlinx.android.synthetic.main.fragment_new_answer.view.*
import rx.subjects.PublishSubject

class NewAnswerFragment : BottomSheetDialogFragment() {
    private var behaviour: BottomSheetBehavior<View>? = null
    val answerAdded = PublishSubject.create<Answer>()

    var question: Question? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        question = Gson().fromJson(arguments.getString("question"), Question::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.fragment_new_answer, null)
        dialog.setContentView(view)
        behaviour = BottomSheetBehavior.from(view.getParent() as View)

        view.answer_question_title.text = question!!.title
        view.answer_question_close_button.setOnClickListener {
            dialog.dismiss()
        }
        view.answer_question_submit_button.setOnClickListener {
            view.answer_question_submit_button.isEnabled = false

            val content = view.answer_question_content.text.toString()

            // validation
            if (content.isEmpty()) {
                view.answer_question_submit_button.isEnabled = true
                Toast.makeText(context, "Answer should be not empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            post("""questions/${question!!.id}/answers""", mapOf<String, String>("content" to content), { gson, json ->
                val answer = gson.fromJson<Answer>(json, Answer::class.java)
                Toast.makeText(context, "Save new answer successfully", Toast.LENGTH_LONG).show()
                answerAdded.onNext(answer)
                view.answer_question_submit_button.isEnabled = true
                dialog.dismiss()
            }, {
                view.answer_question_submit_button.isEnabled = true
            })
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        //默认全屏展开
        behaviour!!.state = BottomSheetBehavior.STATE_EXPANDED
    }
}