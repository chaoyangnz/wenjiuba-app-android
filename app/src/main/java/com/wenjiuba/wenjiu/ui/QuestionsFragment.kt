package com.wenjiuba.wenjiu.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.wenjiuba.wenjiu.Question
import com.wenjiuba.wenjiu.R
import kotlinx.android.synthetic.main.fragment_questions.view.*

class QuestionsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

}
