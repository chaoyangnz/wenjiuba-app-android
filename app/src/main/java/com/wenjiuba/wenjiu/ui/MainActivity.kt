package com.wenjiuba.wenjiu.ui

import android.support.design.widget.Snackbar
import com.wenjiuba.wenjiu.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_questions.*


class MainActivity : android.support.v7.app.AppCompatActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

//        fab.setOnClickListener { view ->
//            android.support.design.widget.Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }

        val adapter = MainPagerAdapter(supportFragmentManager)

        adapter.addFragment(QuestionsFragment())
        adapter.addFragment(StreamFragment())
        adapter.addFragment(ProfileFragment())
        viewpager.setAdapter(adapter)

        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_questions -> viewpager.setCurrentItem(0, true)
                R.id.action_stream -> viewpager.setCurrentItem(1, true)
                R.id.action_you -> viewpager.setCurrentItem(2, true)
            }
            return@setOnNavigationItemSelectedListener true
        }
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
            val dialog = NewQuestionFragment()
            dialog.questionAdded.subscribe { question ->
                questionsRecyclerAdapter.add(question)
            }
            dialog.show(getSupportFragmentManager(), "New Question")
        }

        return super.onOptionsItemSelected(item)
    }


}



