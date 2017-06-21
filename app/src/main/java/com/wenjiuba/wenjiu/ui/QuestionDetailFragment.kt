package com.wenjiuba.wenjiu.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.gson.Gson
import com.wenjiuba.wenjiu.Question
import com.wenjiuba.wenjiu.R
import com.zzhoujay.richtext.RichText
import kotlinx.android.synthetic.main.fragment_question_detail.view.*


/**
 * Created by Charvis on 14/06/2017.
 */

class QuestionDetailFragment : FullScreenDialogFragment() {
    var question: Question? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        question = Gson().fromJson(arguments.getString("question"), Question::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.fragment_question_detail, null)
        dialog.setContentView(view)

        // init the bottom sheet behavior
        view.answers_recycler.adapter = answersRecyclerAdapter
        view.answers_recycler.setLayoutManager(LinearLayoutManager(context))

        view.question_title.text = question!!.title
        view.question_statAnswer.text = """‧ ‧ ‧ ${question!!.statAnswer} ANSWERS ‧ ‧ ‧"""

        RichText.fromHtml(question!!.content).into(view.question_content)

        view.question_detail_answer_button.setOnClickListener {
            val dialog = NewAnswerFragment()
            dialog.arguments = arguments
            dialog.answerAdded.subscribe { answer ->
                answersRecyclerAdapter.add(answer)
            }
            dialog.show(activity.getSupportFragmentManager(), "New Answer")
        }
        view.question_detail_close_button.setOnClickListener {
            dialog.dismiss()
        }

        view.toggle_question_collapse.setOnClickListener {
            if (view.question_content.visibility == View.VISIBLE){
                view.question_content.visibility = View.GONE
                view.toggle_question_collapse.setImageResource(R.drawable.ic_expand_more_black_24dp)
            } else {
                view.question_content.visibility = View.VISIBLE
                view.toggle_question_collapse.setImageResource(R.drawable.ic_expand_less_black_24dp)
            }


        }

        val answersSwipeRefreshLayout = view.answersSwipeRefreshLayout
        answersSwipeRefreshLayout.isEnabled = false // we don't implement pull to refresh right now
        answersSwipeRefreshLayout.setProgressViewEndTarget(true, 120)
        answersSwipeRefreshLayout.setDistanceToTriggerSync(300)
        answersSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        answersSwipeRefreshLayout.post { answersSwipeRefreshLayout.setRefreshing(true) }
        answersRecyclerAdapter.refresh("""questions/${question!!.id}""") {
            answersSwipeRefreshLayout.setRefreshing(false)
        }

        return dialog
    }
}