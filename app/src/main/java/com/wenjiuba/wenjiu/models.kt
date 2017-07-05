package com.wenjiuba.wenjiu

import android.os.Parcelable
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.*

/**
 * Created by Charvis on 12/06/2017.
 */

data class Question (
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

data class Case (
    val id: Int,
//        val title: String,
    val content: String,
    val createdAt: Long,
    val statComment: Int,
    val creator: User
)

data class Comment (
        val id: Int,
        val content: String,
        val createdAt: Long,
        val refId: Int,
        val refType: String,
        val creator: User,
        val sequence: String,
        val children: List<Comment>
)

class EnoterReport {
    var id: Long? = null

    var fullName: String? = null

    var gender: String? = null

    var age: Int? = null

    var menses: String? = null

    var height: BigDecimal? = null

    var weight: BigDecimal? = null

    var bpHigh: BigDecimal? = null

    var bpLow: BigDecimal? = null

    var llu: BigDecimal? = null

    var lpc: BigDecimal? = null

    var lht: BigDecimal? = null

    var lsi: BigDecimal? = null

    var lte: BigDecimal? = null

    var lli: BigDecimal? = null

    var lsp: BigDecimal? = null

    var llr: BigDecimal? = null

    var lki: BigDecimal? = null

    var lbl: BigDecimal? = null

    var lgb: BigDecimal? = null

    var lst: BigDecimal? = null

    var rlu: BigDecimal? = null

    var rpc: BigDecimal? = null

    var rht: BigDecimal? = null

    var rsi: BigDecimal? = null

    var rte: BigDecimal? = null

    var rli: BigDecimal? = null

    var rsp: BigDecimal? = null

    var rlr: BigDecimal? = null

    var rki: BigDecimal? = null

    var rbl: BigDecimal? = null

    var rgb: BigDecimal? = null

    var rst: BigDecimal? = null

    var average: BigDecimal? = null

    var yinyang: BigDecimal? = null

    var limb: BigDecimal? = null

    var side: BigDecimal? = null

    var extremum: BigDecimal? = null

    var enoterPackageId: Long? = null

    var expert1: Long? = null

    var expert2: Long? = null

    var robotReport: String? = null

    var expert1Report: String? = null

    var expert2Report: String? = null

    var robotReviewInd: String? = null

    var expert1ReviewInd: String? = null

    var expert2ReviewInd: String? = null


    // 套餐选项，机器人／专家
    var requestPackageInd: String? = null

    // 专家选项：自己指定／平台分配
    var requestExpertInd: String? = null

    var paymentInd: String? = null

    var createdAt: Long? = null

    var createdBy: Long? = null
}

class Expert {
    var id: Long? = null
    var userId: Long? = null
    var fullName: String? = null

}

class AlipayTrade {
    var businessType: String? = null
    var businessId: Long? = null
    var subject: String? = null
    var totalFee: BigDecimal? = null
    var body: String? = null
}