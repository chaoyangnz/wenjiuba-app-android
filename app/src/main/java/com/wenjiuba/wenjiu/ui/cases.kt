package com.wenjiuba.wenjiu.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wenjiuba.wenjiu.Case
import com.wenjiuba.wenjiu.Comment
import com.wenjiuba.wenjiu.R
import com.wenjiuba.wenjiu.net.post
import com.wenjiuba.wenjiu.util.DateUtil
import com.wenjiuba.wenjiu.util.StringUtil
import com.zzhoujay.richtext.RichText
import kotlinx.android.synthetic.main.fragment_case_detail.view.*
import kotlinx.android.synthetic.main.fragment_cases.view.*
import kotlinx.android.synthetic.main.fragment_new_case.view.*
import kotlinx.android.synthetic.main.fragment_new_comment.view.*
import kotlinx.android.synthetic.main.item_case.view.*
import kotlinx.android.synthetic.main.item_comment.view.*
import rx.subjects.PublishSubject
import java.util.*


class CasesFragment : Fragment() {

    var casesRecyclerAdapter: ListRecyclerAdapter<Case>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        casesRecyclerAdapter = ListRecyclerAdapter<Case>(R.layout.item_case, { case, view, _, _ ->
            //    view.case_item_title.text = case.title
            view.case_item_summary.text = StringUtil.trim(StringUtil.html2text(case.content), 80)

            view.case_item_creator_displayName.text = case.creator.displayName
            view.case_item_creator_avatar.load(case.creator.avatar)

            view.case_item_misc.text = """分享于 ${DateUtil.formatDate(Date(case.createdAt))} · ${case.statComment} 评论"""
        }, "cases", null, object : TypeToken<List<Case>>() {}.type, false, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_cases, container, false)

        //recycler view
        view.cases_recycler.adapter = casesRecyclerAdapter
        view.cases_recycler.setLayoutManager(LinearLayoutManager(context));


        casesRecyclerAdapter!!.itemClicked.subscribe { case ->
            if (activity == null) return@subscribe

            val args = Bundle()
            args.putString("case", Gson().toJson(case))

            val dialog = CaseDetailFragment()
            dialog.arguments = args
            dialog.show(activity.getSupportFragmentManager(), "Case Detail")
        }

        // pull to refresh
        val swipeRefreshLayout = view.cases_swipeRefreshLayout
        swipeRefreshLayout.setProgressViewEndTarget(true, 120)
        swipeRefreshLayout.setDistanceToTriggerSync(300)
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        swipeRefreshLayout.setOnRefreshListener {
            view.cases_refreshing_zone.visibility = View.VISIBLE

            casesRecyclerAdapter!!.refresh(null) {
                swipeRefreshLayout.isRefreshing = false
                view.cases_refreshing_zone.visibility = View.GONE
            }
        }

        // load data for the first time
        swipeRefreshLayout.post { swipeRefreshLayout.setRefreshing(true) }
        view.cases_refreshing_zone.visibility = View.VISIBLE
        casesRecyclerAdapter!!.refresh(null, {
            swipeRefreshLayout.setRefreshing(false)
            view.cases_refreshing_zone.visibility = View.GONE
        })

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater!!.inflate(com.wenjiuba.wenjiu.R.menu.cases_menu, menu)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_new_case) {
            val dialog = NewCaseFragment()
            dialog.caseAdded.subscribe { case ->
                casesRecyclerAdapter!!.add(case)
            }
            dialog.show(activity.supportFragmentManager, "New Case")
        }

        return super.onOptionsItemSelected(item)
    }

}

class NewCaseFragment : FullScreenDialogFragment() {
    val caseAdded = PublishSubject.create<Case>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.fragment_new_case, null)
        dialog.setContentView(view)

        view.new_case_close_button.setOnClickListener {
            dialog.dismiss()
        }
        view.new_case_submit_button.setOnClickListener {
            view.new_case_submit_button.isEnabled = false

            val content = view.new_case_content.text.toString()

            // validate
            if (content.isEmpty()) {
                view.new_case_submit_button.isEnabled = true
                Toast.makeText(context, "案例内容不能为空", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            post("cases", mapOf<String, String>("content" to content), { gson, json ->
                val case = gson.fromJson<Case>(json, Case::class.java)
                Toast.makeText(context, "保存案例成功", Toast.LENGTH_LONG).show()
                caseAdded.onNext(case)
                view.new_case_submit_button.isEnabled = true
                dialog.dismiss()
            }, {
                view.new_case_submit_button.isEnabled = true
            })
        }

        return dialog
    }
}


class CaseDetailFragment : FullScreenDialogFragment() {
    var case: Case? = null
    var commentsRecyclerAdapter: ListRecyclerAdapter<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        case = Gson().fromJson(arguments.getString("case"), Case::class.java)

        commentsRecyclerAdapter  = ListRecyclerAdapter<Comment>(R.layout.item_comment, { comment, view, _, _ ->

            RichText.fromHtml(comment.content).into(view.comment_content)

            view.comment_creator_displayName.text = comment.creator.displayName
            view.comment_creator_avatar.load(comment.creator.avatar)

            view.comment_misc.text = """评论于 ${DateUtil.formatDate(Date(comment.createdAt))}"""
        }, """cases/${case!!.id}/comments""", null, object : TypeToken<List<Comment>>() {}.type, false, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.fragment_case_detail, null)
        dialog.setContentView(view)

        // init the bottom sheet behavior
        view.comments_recycler.adapter = commentsRecyclerAdapter
        view.comments_recycler.setLayoutManager(LinearLayoutManager(context))

        view.case_statComment.text = """‧ ‧ ‧ ${case!!.statComment} 个评论 ‧ ‧ ‧"""


        RichText.fromHtml(case!!.content).into(view.case_content)
        view.case_content.setMovementMethod(ScrollingMovementMethod())

        view.case_detail_comment_button.setOnClickListener {
            val dialog = NewCommentFragment()
            dialog.arguments = arguments
            dialog.commentAdded.subscribe { comment ->
                commentsRecyclerAdapter!!.add(comment)
            }
            dialog.show(activity.getSupportFragmentManager(), "New Comment")
        }
        view.case_detail_close_button.setOnClickListener {
            dialog.dismiss()
        }

        view.case_toggle_collapse.setOnClickListener {
            if (view.case_content.visibility == View.VISIBLE){
                view.case_content.visibility = View.GONE
                view.case_toggle_collapse.setImageResource(R.drawable.ic_expand_more_black_24dp)
            } else {
                view.case_content.visibility = View.VISIBLE
                view.case_toggle_collapse.setImageResource(R.drawable.ic_expand_less_black_24dp)
            }
        }

        val commentsSwipeRefreshLayout = view.commentsSwipeRefreshLayout
        commentsSwipeRefreshLayout.isEnabled = false // we don't implement pull to refresh right now
        commentsSwipeRefreshLayout.setProgressViewEndTarget(true, 120)
        commentsSwipeRefreshLayout.setDistanceToTriggerSync(300)
        commentsSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        commentsSwipeRefreshLayout.post { commentsSwipeRefreshLayout.setRefreshing(true) }
        commentsRecyclerAdapter!!.refresh("""cases/${case!!.id}/comments""") {
            commentsSwipeRefreshLayout.setRefreshing(false)
        }

        return dialog
    }
}

class NewCommentFragment : FullScreenDialogFragment() {
    val commentAdded = PublishSubject.create<Comment>()

    var case: Case? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        case = Gson().fromJson(arguments.getString("case"), Case::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.fragment_new_comment, null)
        dialog.setContentView(view)

        view.new_comment_case_content.text = StringUtil.trim(StringUtil.html2text(case!!.content), 80)
        view.new_comment_close_button.setOnClickListener {
            dialog.dismiss()
        }
        view.new_comment_submit_button.setOnClickListener {
            view.new_comment_submit_button.isEnabled = false

            val content = view.new_comment_case_content.text.toString()

            // validate
            if (content.isEmpty()) {
                view.new_comment_submit_button.isEnabled = true
                Toast.makeText(context, "评论不能为空", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            post("""cases/${case!!.id}/comments""", mapOf<String, String>("content" to content), { gson, json ->
                val comment = gson.fromJson<Comment>(json, Comment::class.java)
                Toast.makeText(context, "保存评论成功", Toast.LENGTH_LONG).show()
                commentAdded.onNext(comment)
                view.new_comment_submit_button.isEnabled = true
                dialog.dismiss()
            }, {
                view.new_comment_submit_button.isEnabled = true
            })
        }

        return dialog
    }
}