package com.wenjiuba.wenjiu.net

import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wenjiuba.wenjiu.App
import com.wenjiuba.wenjiu.Question
import com.wenjiuba.wenjiu.getToken


//const val API_BASE = "https://api.wenjiuba.com/"
const val API_BASE = "http://130.217.185.73:8080/"

val StringType = object : TypeToken<String>() {}.type
val VoidType = object : TypeToken<Void>() {}.type
val QuestionListType = object : TypeToken<List<Question>>() {}.type

private fun api(endpoint: String): String {
    return API_BASE + endpoint
}

fun request(method: Int, endpoint: String, data: Any, success: (gson: Gson, json: String)->Unit, failure: (Exception)->Unit) {
    val gson = Gson()
    val request = object : StringRequest(method, api(endpoint), { response ->
        var json = response.toString()
        success(gson, json)
        Log.v("api.wenjiuba.com", json)
    }, { error ->
        error.printStackTrace()
        failure.invoke(error)
    }) {
        override fun getBody(): ByteArray {
            var body: ByteArray = "".toByteArray()
            if (this.method == Request.Method.POST) {
                val paramsJson = gson.toJson(data)
                body = paramsJson.toByteArray()
            }
            return body
        }

        override fun getBodyContentType(): String {
            return "application/json"
        }

        override fun getHeaders(): Map<String, String> {
            val headers = mutableMapOf<String, String>()
            val token = App.instance!!.getPreferences().getToken()
            if (token.isNotEmpty()) {
                headers["Authorization"] = """Bearer ${token}"""
            }
            return headers
        }
    }
    App.addRequest(request, "API")
}

fun post(endpoint: String, data: Any, success: (gson: Gson, json: String) -> Unit, failure: (Exception) -> Unit) {
    request(Request.Method.POST, endpoint, data, success, failure)
}

fun get(endpoint: String, data: Map<String, String?>, success: (gson: Gson, json: String) -> Unit, failure: (Exception) -> Unit) {
    val params_encode = data.entries.map { (k,v) -> """${k}=${v}""" }.joinToString("&")
    var url = endpoint
    if (params_encode.isNotEmpty()) url += "?" + params_encode
    request(Request.Method.GET, url, "", success, failure)
}

