package com.wenjiuba.wenjiu.ui

import android.app.Activity
import android.net.Uri
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.RecyclerView
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.github.vipulasri.timelineview.TimelineView
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.wenjiuba.wenjiu.*
import com.wenjiuba.wenjiu.net.get
import com.wenjiuba.wenjiu.util.DateUtil
import com.wenjiuba.wenjiu.util.StringUtil
import com.zzhoujay.richtext.RichText
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.answer_item.view.*
import kotlinx.android.synthetic.main.fragment_question_detail.view.*
import kotlinx.android.synthetic.main.question_item.view.*
import kotlinx.android.synthetic.main.stream_item.view.*
import rx.subjects.PublishSubject
import java.lang.reflect.Type
import java.util.*


val questionListType = object : TypeToken<List<Question>>() {}.type
val answerListType = object : TypeToken<List<Answer>>() {}.type
val streamListType = object : TypeToken<List<Stream>>() {}.type

val questionsRecyclerAdapter = ListRecyclerAdapter<Question>(R.layout.question_item, { question, view, _, _ ->
    view.question_item_title.text = question.title
    view.question_item_summary.text = StringUtil.trim(StringUtil.html2text(question.content), 80)

    view.question_item_creator_displayName.text = question.creator.displayName
    view.question_item_creator_avatar.load(question.creator.avatar)

    view.question_item_date_answers.text = """Asked in ${DateUtil.formatDate(Date(question.createdAt))} · ${question.statAnswer} answers"""
}, "questions", null, questionListType, false, false)


val answersRecyclerAdapter = ListRecyclerAdapter<Answer>(R.layout.answer_item, { answer, view, position, adaptor ->

    RichText.fromHtml(answer.content).into(view.answer_content)

    view.answer_creator_displayName.text = answer.creator.displayName
    view.answer_creator_avatar.load(answer.creator.avatar)
    view.answer_date.text = """Answered in ${DateUtil.formatDate(Date(answer.createdAt))}"""

    val currentUser = App.instance!!.getPreferences().getUser()
    if (answer.creator.id == currentUser.id) { // my own answer, don't upvote/downvote
        view.upvoteButton.visibility = View.GONE
        view.downvoteButton.visibility = View.GONE
        return@ListRecyclerAdapter
    }
    val answerVote = findAnswerVote(answer)
    when (answerVote?.vote ?: 0) {
        0 -> {
            view.upvoteButton.text = "↑ Upvote"
            view.upvoteButton.setBackgroundResource(R.drawable.button_upvote_ripple)
            view.downvoteButton.text = "↓ Downvote"
        }
        1 -> {
            view.upvoteButton.text = "↑ Upvoted"
            view.upvoteButton.setBackgroundResource(R.drawable.button_upvoted_ripple)
            view.downvoteButton.text = "↓ Downvote"
        }
        -1 -> {
            view.upvoteButton.text = "↑ Upvote"
            view.upvoteButton.setBackgroundResource(R.drawable.button_upvote_ripple)
            view.downvoteButton.text = "↓ Downvoted"
        }
    }
    view.upvoteButton.setOnClickListener {
        view.upvoteButton.isEnabled = false
        var toVote = 1
        if ((answerVote?.vote ?: 0) == 1) {
            toVote = 0
        }
        get("""questions/${answer.question.id}/answers/${answer.id}/vote""", mapOf("to" to toVote.toString()), { gson, json ->
            val newAnswer = gson.fromJson<Answer>(json, Answer::class.java)
            answer.answerVotes = newAnswer.answerVotes

            adaptor.notifyItemChanged(position)
            view.upvoteButton.isEnabled = true
        }, {
            view.upvoteButton.isEnabled = true
        })
    }

    view.downvoteButton.setOnClickListener {
        view.downvoteButton.isEnabled = false
        var toVote = -1
        if ((answerVote?.vote ?: 0) == -1) {
            toVote = 0
        }
        get("""questions/${answer.question.id}/answers/${answer.id}/vote""", mapOf("to" to toVote.toString()), { gson, json ->
            val newAnswer = gson.fromJson<Answer>(json, Answer::class.java)
            answer.answerVotes = newAnswer.answerVotes
            adaptor.notifyItemChanged(position)
            view.downvoteButton.isEnabled = true
        }, {
            view.downvoteButton.isEnabled = true
        })
    }

}, "", "answers", answerListType, true, false)

fun findAnswerVote(answer: Answer): AnswerVote? {
    for (answerVote in answer.answerVotes) {
        val currentUser = App.instance!!.getPreferences().getUser()
        if (answerVote.userId == currentUser.id) {
            return answerVote
        }
    }
    return null
}

open class DefaultViewHolder(view: View) : RecyclerView.ViewHolder(view) {}

class TimeLineViewHolder(itemView: View, viewType: Int) : DefaultViewHolder(itemView) {
    var mTimelineView: TimelineView

    init {
        mTimelineView = itemView.findViewById(R.id.stream_timeline) as TimelineView
        mTimelineView.initLine(viewType)
    }
}

val streamRecyclerAdapter = ListRecyclerAdapter<Stream>(R.layout.stream_item, { stream, view, _, _ ->
    view.stream_description.text = stream.title ?: stream.questionTitle
    view.stream_date.text = DateUtil.formatDate(Date(stream.happenedAt))

}, "me/stream", null, streamListType, false, true)

class ListRecyclerAdapter<T>(
        val itemLayoutResId: Int,
        val binder: (T, View, Int, ListRecyclerAdapter<T>) -> Unit,
        val refreshUri: String,
        val resultNode: String? = null,
        val type: Type,
        val clearOnRefresh: Boolean,
        val timeline: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = mutableListOf<T>()

    val itemClicked = PublishSubject.create<T>()

    fun add(item: T) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    fun refresh(refreshUriOverride: String? = null, refreshCompleteCallback: (() -> Unit)? = null) {
        val uri = refreshUriOverride ?: refreshUri

        // clear list
        if (clearOnRefresh) {
            this.items = mutableListOf()
            this.notifyDataSetChanged()
        }

        // request
        get(uri, mapOf(), {gson, json ->
            if (resultNode != null) {
                val jsonObj = gson.fromJson(json, JsonObject::class.java)
                this.items = gson.fromJson(jsonObj.getAsJsonArray(resultNode), type)
            } else {
                this.items = gson.fromJson(json,  type)
            }

            this.notifyDataSetChanged()
            refreshCompleteCallback?.invoke()
        }, { error -> })
    }

    override fun getItemViewType(position: Int): Int {
        return if (timeline) TimelineView.getTimeLineViewType(position,getItemCount()) else super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(this.itemLayoutResId, parent, false)
        return if (timeline) TimeLineViewHolder(view, viewType) else DefaultViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = this.items.get(position)
        var view = holder.itemView

        binder.invoke(item, view, position, this)

        view.setOnClickListener({
            itemClicked.onNext(item)
        })

    }

    override fun getItemCount(): Int {
        return items.size
    }
}

fun ImageView.load(imageUri: String) {
    val context = this.context as Activity
    if (!context.isDestroyed) {
        val transform = CropCircleTransformation(context)
        if (imageUri.startsWith("data:")) {
            var index = imageUri.indexOfLast { it == ',' }
            val imageBytes = Base64.decode(imageUri.substring(index+1), Base64.DEFAULT);
            Glide.with(context)
                    .load(imageBytes)
                    .bitmapTransform(transform)
                    .into(this);
        } else {
            Glide.with(context)
                    .load(Uri.parse(imageUri))
                    .bitmapTransform(transform)
                    .into(this)
        }
    }
}


class MainPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    val fragments = mutableListOf<Fragment>()

    fun addFragment(fragment: Fragment) {
        fragments.add(fragment)
    }

    override fun getItem(index: Int): Fragment {
        return fragments.get(index)
    }

    override fun getCount(): Int {
        return fragments.size
    }

}


