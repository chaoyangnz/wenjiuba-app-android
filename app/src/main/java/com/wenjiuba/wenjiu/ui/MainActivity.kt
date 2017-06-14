package com.wenjiuba.wenjiu.ui

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.wenjiuba.wenjiu.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : android.support.v7.app.AppCompatActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

//        fab.setOnClickListener { view ->
//            android.support.design.widget.Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }

        //recycler view
        questions_recycler.adapter = questionsRecyclerAdapter
        questions_recycler.setLayoutManager(LinearLayoutManager(this));


        questionsRecyclerAdapter.itemClicked.subscribe { question ->

            QuestionDetailFragment(question).show(getSupportFragmentManager(), "Question Detail")
//            handleQuestionDetail(question)
        }

        // pull to refresh
        swipeRefreshLayout.setProgressViewEndTarget(true, 120)
        swipeRefreshLayout.setDistanceToTriggerSync(300)
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        swipeRefreshLayout.setOnRefreshListener {
            refreshing_zone.visibility = View.VISIBLE

            questionsRecyclerAdapter.refresh(null) {
                swipeRefreshLayout.isRefreshing = false
                refreshing_zone.visibility = View.GONE
            }
        }

        // load data for the first time
        swipeRefreshLayout.post(Runnable { swipeRefreshLayout.setRefreshing(true) })
        questionsRecyclerAdapter.refresh(null, {
            swipeRefreshLayout.setRefreshing(false)
        })
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(com.wenjiuba.wenjiu.R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_new_question) {
            NewQuestionFragment().show(getSupportFragmentManager(), "New Question")
//            handleNewQuestion()
        }

        return super.onOptionsItemSelected(item)
    }


}



