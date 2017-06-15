package com.wenjiuba.wenjiu.ui

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.gson.Gson
import com.wenjiuba.wenjiu.Question
import com.wenjiuba.wenjiu.R
import com.wenjiuba.wenjiu.util.StringUtil
import kotlinx.android.synthetic.main.fragment_question_detail.view.*


/**
 * Created by Charvis on 14/06/2017.
 */

class QuestionDetailFragment : BottomSheetDialogFragment() {
    private var behavior: BottomSheetBehavior<View>? = null
    var question: Question? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        question = Gson().fromJson(arguments.getString("question"), Question::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.fragment_question_detail, null)
        dialog.setContentView(view)
        behavior = BottomSheetBehavior.from(view.getParent() as View)

        // init the bottom sheet behavior
        view.answers_recycler.adapter = answersRecyclerAdapter
        view.answers_recycler.setLayoutManager(LinearLayoutManager(context));


        view.question_title.text = question!!.title
        view.question_statAnswer.text = """‧ ‧ ‧ ${question!!.statAnswer} ANSWERS ‧ ‧ ‧"""

        view.question_content.text = StringUtil.html2text(question!!.content)
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

        val answersSwipeRefreshLayout = view.answersSwipeRefreshLayout
        answersSwipeRefreshLayout.setProgressViewEndTarget(true, 120)
        answersSwipeRefreshLayout.setDistanceToTriggerSync(300)
        answersSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        answersSwipeRefreshLayout.post { answersSwipeRefreshLayout.setRefreshing(true) }
        answersRecyclerAdapter.refresh("""questions/${question!!.id}""") {
            answersSwipeRefreshLayout.setRefreshing(false)
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        //默认全屏展开
        behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

}