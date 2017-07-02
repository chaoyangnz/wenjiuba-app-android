package com.wenjiuba.wenjiu.ui

import com.wenjiuba.wenjiu.R
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.view.ViewPager
import android.view.MenuItem


class MainActivity : android.support.v7.app.AppCompatActivity() {

    var prevMenuItem: MenuItem? = null

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
        adapter.addFragment(CasesFragment())
        adapter.addFragment(StreamFragment())
        viewpager.setAdapter(adapter)
        viewpager.setOffscreenPageLimit(2)

        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_questions -> viewpager.setCurrentItem(0, true)
                R.id.action_cases -> viewpager.setCurrentItem(1, true)
                R.id.action_enoter -> viewpager.setCurrentItem(2, true)
            }
            return@setOnNavigationItemSelectedListener true
        }

        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem!!.setChecked(false)
                } else {
                    navigation.getMenu().getItem(0).setChecked(false)
                }

                navigation.getMenu().getItem(position).setChecked(true)
                prevMenuItem = navigation.getMenu().getItem(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }




}



