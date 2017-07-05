package com.wenjiuba.wenjiu.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import com.alipay.sdk.app.PayTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wenjiuba.wenjiu.AlipayTrade
import com.wenjiuba.wenjiu.R
import com.wenjiuba.wenjiu.net.get
import com.wenjiuba.wenjiu.net.post
import kotlinx.android.synthetic.main.fragment_payment.view.*
import java.util.*


private val SDK_PAY_FLAG = 1

class PaymentFragment : FullScreenDialogFragment() {
    var alipayTrade: AlipayTrade? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alipayTrade = Gson().fromJson(arguments.getString("alipayTrade"), AlipayTrade::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.fragment_payment, null)
        dialog.setContentView(view)

        view.payment_subject.text = alipayTrade!!.subject
        view.payment_totalFee.text = alipayTrade!!.totalFee.toString()


        view.payment_close_button.setOnClickListener {
            dialog.dismiss()
        }

        view.payment_submit_button.setOnClickListener { v ->
            var paramMap = mapOf<String, String?>(
                    "businessType" to alipayTrade!!.businessType!!,
                    "businessId" to alipayTrade!!.businessId.toString(),
                    "subject" to alipayTrade!!.subject,
                    "totalFee" to alipayTrade!!.totalFee.toString(),
                    "body" to alipayTrade!!.body
            )
            val getParams = paramMap.entries.map { (k,v) -> """${k}=${v}""" }.joinToString("&")

            post("alipay/submit?" + getParams, mapOf<String, String>(), { gson, json ->
                var res: Map<String, String> = gson.fromJson(json, object : TypeToken<Map<String, String>>(){}.type)
                val orderInfo = res.get("orderInfo")

                AlertDialog.Builder(this.activity)
                        .setTitle("支付结果")
                        .setMessage("您是否在支付宝App中完成支付？")
                        .setCancelable(false)
                        .setPositiveButton("完成", {dialoginterface, i ->
                            this.dismiss() // 关闭dialog，让其回到列表页面
                        })
                        .setNegativeButton("遇到问题", {dialoginterface, i ->
                            this.dismiss()
                        }).show()

                // 调起支付宝App进行支付
                val payRunnable = Runnable {
                    val alipay = PayTask(this.activity)
                    val result = alipay.payV2(orderInfo, true)
                }
                // 必须异步调用
                val payThread = Thread(payRunnable)
                payThread.start()
            }, {})


        }


        return dialog
    }
}

class PayResult(rawResult: Map<String, String>) {
    /**
     * @return the resultStatus
     */
    var resultStatus: String? = null
        private set
    /**
     * @return the result
     */
    var result: String? = null
        private set
    /**
     * @return the memo
     */
    var memo: String? = null
        private set

    init {
        for (key in rawResult!!.keys) {
            if (TextUtils.equals(key, "resultStatus")) {
                resultStatus = rawResult[key]
            } else if (TextUtils.equals(key, "result")) {
                result = rawResult[key]
            } else if (TextUtils.equals(key, "memo")) {
                memo = rawResult[key]
            }
        }
    }

    override fun toString(): String {
        return "resultStatus={$resultStatus};memo={$memo"+"};result={" + result + "}"
    }
}

