package com.wenjiuba.wenjiu.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import com.google.gson.Gson
import com.wenjiuba.wenjiu.Answer
import com.wenjiuba.wenjiu.Question
import com.wenjiuba.wenjiu.R
import com.wenjiuba.wenjiu.net.post
import com.zzhoujay.richtext.RichText
import kotlinx.android.synthetic.main.fragment_new_answer.view.*
import kotlinx.android.synthetic.main.fragment_new_question.view.*
import kotlinx.android.synthetic.main.fragment_question_detail.view.*
import kotlinx.android.synthetic.main.fragment_questions.view.*
import rx.subjects.PublishSubject

class QuestionsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_questions, container, false)

        //recycler view
        view.questions_recycler.adapter = questionsRecyclerAdapter
        view.questions_recycler.setLayoutManager(LinearLayoutManager(context));


        questionsRecyclerAdapter.itemClicked.subscribe { question ->
            if (activity == null) return@subscribe

            val args = Bundle()
            args.putString("question", Gson().toJson(question))

            val dialog = QuestionDetailFragment()
            dialog.arguments = args
            dialog.show(activity.getSupportFragmentManager(), "Question Detail")
        }

        // pull to refresh
        val swipeRefreshLayout = view.swipeRefreshLayout
        swipeRefreshLayout.setProgressViewEndTarget(true, 120)
        swipeRefreshLayout.setDistanceToTriggerSync(300)
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        swipeRefreshLayout.setOnRefreshListener {
            view.refreshing_zone.visibility = View.VISIBLE

            questionsRecyclerAdapter.refresh(null) {
                swipeRefreshLayout.isRefreshing = false
                view.refreshing_zone.visibility = View.GONE
            }
        }

        // load data for the first time
        swipeRefreshLayout.post { swipeRefreshLayout.setRefreshing(true) }
        view.refreshing_zone.visibility = View.VISIBLE
        questionsRecyclerAdapter.refresh(null, {
            swipeRefreshLayout.setRefreshing(false)
            view.refreshing_zone.visibility = View.GONE
        })

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater!!.inflate(com.wenjiuba.wenjiu.R.menu.questions_menu, menu)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_new_question) {
            val dialog = NewQuestionFragment()
            dialog.questionAdded.subscribe { question ->
                questionsRecyclerAdapter.add(question)
            }
            dialog.show(activity.supportFragmentManager, "New Question")
        }

        return super.onOptionsItemSelected(item)
    }

}


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
        view.question_statAnswer.text = """‧ ‧ ‧ ${question!!.statAnswer} 个回答 ‧ ‧ ‧"""

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
                Toast.makeText(context, "标题和描述不能为空", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            post("questions", mapOf<String, String>("title" to title, "content" to content), { gson, json ->
                val question = gson.fromJson<Question>(json, Question::class.java)
                Toast.makeText(context, "保存问题成功", Toast.LENGTH_LONG).show()
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

class NewAnswerFragment : FullScreenDialogFragment() {
    val answerAdded = PublishSubject.create<Answer>()

    var question: Question? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        question = Gson().fromJson(arguments.getString("question"), Question::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.fragment_new_answer, null)
        dialog.setContentView(view)

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
                Toast.makeText(context, "答案不能为空", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            post("""questions/${question!!.id}/answers""", mapOf<String, String>("content" to content), { gson, json ->
                val answer = gson.fromJson<Answer>(json, Answer::class.java)
                Toast.makeText(context, "保存新答案成功", Toast.LENGTH_LONG).show()
                answerAdded.onNext(answer)
                view.answer_question_submit_button.isEnabled = true
                dialog.dismiss()
            }, {
                view.answer_question_submit_button.isEnabled = true
            })
        }

        return dialog
    }
}