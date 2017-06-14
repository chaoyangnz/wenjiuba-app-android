package com.wenjiuba.wenjiu.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wenjiuba.wenjiu.R
import kotlinx.android.synthetic.main.fragment_stream.view.*

class StreamFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_stream, container, false)

        view.stream_recycler.adapter = streamRecyclerAdapter
        view.stream_recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        streamRecyclerAdapter.refresh {  }

        return view
    }

}
