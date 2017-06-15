package com.wenjiuba.wenjiu

/**
 * Created by Charvis on 12/06/2017.
 */

data class Question(
    val id: Int,
    val title: String,
    val content: String,
    val createdAt: Long,
    val statAnswer: Int,
    val creator: User,
    val answers: List<Answer>
)

data class Answer(
    val id: Int,
    val content: String,
    val createdAt: Long,
    val creator: User,
    val statUpvote: Int,
    val statDownvote: Int,
    var answerVotes: List<AnswerVote>,
    val question: Question
)

data class AnswerVote(
    val id: Int,
    val userId: Int,
    val vote: Int
)

data class User(
    val id: Int,
    val userName: String,
    val email: String,
    val realName: String,
    val avatar: String,
    val displayName: String
)

data class Stream(
    val id: Int,
    val happenedAt: Long,
    val questionId: Int,
    val questionTitle: String,
    val title: String,
    val type: String
)