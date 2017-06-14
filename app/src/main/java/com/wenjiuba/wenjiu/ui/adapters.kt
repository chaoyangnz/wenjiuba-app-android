package com.wenjiuba.wenjiu.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.wenjiuba.wenjiu.*
import com.wenjiuba.wenjiu.net.get
import com.wenjiuba.wenjiu.util.DateUtil
import com.wenjiuba.wenjiu.util.StringUtil
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.answer_item.view.*
import kotlinx.android.synthetic.main.question_item.view.*
import rx.subjects.PublishSubject
import java.lang.reflect.Type
import java.util.*


val questionListType = object : TypeToken<List<Question>>() {}.type
val answerListType = object : TypeToken<List<Answer>>() {}.type

val questionsRecyclerAdapter = ListRecyclerAdapter<Question>(R.layout.question_item, { question, view ->
    view.question_item_title.text = question.title
    view.question_item_summary.text = StringUtil.trim(StringUtil.html2text(question.content), 80)

    view.question_item_creator_displayName.text = question.creator.displayName
    view.question_item_creator_avatar.load(question.creator.avatar)

    view.question_item_date_answers.text = """问于 ${DateUtil.formatDate(Date(question.createdAt))} · ${question.statAnswer}个回答"""
}, "questions", null, questionListType, false)


val answersRecyclerAdapter = ListRecyclerAdapter<Answer>(R.layout.answer_item, { answer, view ->

    view.answer_content.text = StringUtil.html2text(answer.content)

    view.answer_creator_displayName.text = answer.creator.displayName
    view.answer_creator_avatar.load(answer.creator.avatar)
    view.answer_date.text = """回答于 ${DateUtil.formatDate(Date(answer.createdAt))}"""

    val currentUser = App.instance!!.getPreferences().getUser()
    if (answer.creator.id == currentUser.id) {
        view.upvoteButton.visibility = View.GONE
        view.downvoteButton.visibility = View.GONE
    }
    val answerVote = findAnswerVote(answer)
    when (answerVote?.vote ?: 0) {
        0 -> {
            view.upvoteButton.text = "↑ Upvote"
            view.upvoteButton.setBackgroundResource(R.color.buttonActive)
//            view.upvoteButton.visibility = View.VISIBLE
            view.downvoteButton.text = "↓ Downvote"
//            view.downvoteButton.visibility = View.VISIBLE
        }
        1 -> {
            view.upvoteButton.text = "↑ Upvoted"
            view.upvoteButton.setBackgroundResource(R.color.buttonToggled)
//            view.upvoteButton.visibility = View.VISIBLE
            view.downvoteButton.text = "↓ Downvote"
//            view.downvoteButton.visibility = View.VISIBLE
        }
        -1 -> {
            view.upvoteButton.text = "↑ Upvote"
            view.upvoteButton.setBackgroundResource(R.color.buttonActive)
//            view.upvoteButton.visibility = View.VISIBLE
            view.downvoteButton.text = "↓ Downvoted"
//            view.downvoteButton.visibility = View.VISIBLE
        }
    }

}, "", "answers", answerListType, true)

fun findAnswerVote(answer: Answer): AnswerVote? {
    for (answerVote in answer.answerVotes) {
        val currentUser = App.instance!!.getPreferences().getUser()
        if (answerVote.userId == currentUser.id) {
            return answerVote
        }
    }
    return null
}

class DefaultViewHolder(view: View) : RecyclerView.ViewHolder(view) {}


class ListRecyclerAdapter<T>(
        val itemLayoutResId: Int,
        val binder: (T, View) -> Unit,
        val refreshUri: String,
        val resultNode: String? = null,
        val type: Type,
        val clearOnRefresh: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = listOf<T>()

    val itemClicked = PublishSubject.create<T>()

    fun refresh(refreshUriOverride: String? = null, refreshCompleteCallback: (() -> Unit)? = null) {
        val uri = refreshUriOverride ?: refreshUri

        // clear list
        if (clearOnRefresh) {
            this.items = listOf()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(this.itemLayoutResId, parent, false)
        return DefaultViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = this.items.get(position)
        var view = holder.itemView

        binder.invoke(item, view)

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