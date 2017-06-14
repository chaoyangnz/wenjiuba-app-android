package com.wenjiuba.wenjiu

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.wenjiuba.wenjiu.net.OkHttp3Stack


/**
 * Created by richard on 4/10/17.
 */

class App : Application() {
    var volleyRequestQueue: RequestQueue? = null
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // initialize Sugar ORM
        //        SugarContext.init(this);

        // volley
        volleyRequestQueue = Volley.newRequestQueue(this, OkHttp3Stack())
        volleyRequestQueue!!.start()
    }

    override fun onTerminate() {
        super.onTerminate()

        // destroy Sugar ORM
        //        SugarContext.terminate();

        volleyRequestQueue!!.stop()
        volleyRequestQueue = null

    }

    companion object {
        val LOG_TAG = "BoomCast"

        var instance: App? = null

        val application: Application?
            get() = instance

        val context: Context?
            get() = instance

        private fun addRequest(request: Request<*>) {
            instance!!.volleyRequestQueue!!.add(request)
        }

        fun addRequest(request: Request<*>, tag: String) {
            request.tag = tag
            addRequest(request)
        }

        fun cancelAllRequests(tag: String) {
            instance!!.volleyRequestQueue!!.cancelAll(tag)
        }


    }

    fun getPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

}
const val PREFERENCE_NAME = "wenjiu"

const val USER_KEY = "user"
const val TOKEN_KEY = "token"

fun SharedPreferences.putUser(user: User) {
    val edit = this.edit()
    edit.putString(USER_KEY, Gson().toJson(user))
    edit.commit()
}

fun SharedPreferences.getUser(): User {
    return Gson().fromJson(this.getString(USER_KEY, "{}"), User::class.java)
}

fun SharedPreferences.putToken(token: String) {
    val edit = this.edit()
    edit.putString(TOKEN_KEY, token)
    edit.commit()
}

fun SharedPreferences.getToken(): String {
    return this.getString(TOKEN_KEY, "")
}





