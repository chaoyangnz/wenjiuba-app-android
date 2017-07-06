package com.wenjiuba.wenjiu.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.wenjiuba.wenjiu.*
import com.wenjiuba.wenjiu.net.post
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

//        App.instance!!.getPreferences().edit().clear().commit()
        if (App.instance!!.getPreferences().getToken().isNotEmpty()) {
            gotoMain()
            return
        }


        login_button.setOnClickListener { view ->

            val userName = username_input.text.toString()
            val password = password_input.text.toString()

            // validate
            if (userName.isEmpty() || password.isEmpty()) {
                Snackbar.make(view, "用户名或密码不允许为空", Snackbar.LENGTH_LONG)
            }

            // API request
            val user = mapOf<String, String>("userName" to userName, "password" to password)
            post("login", user, { gson, json ->
                val res = gson.fromJson(json, Map::class.java)
                App.instance!!.getPreferences().putToken(res.get("token").toString())
                val userJson = gson.toJson(res.get("user"))
                App.instance!!.getPreferences().putUser(gson.fromJson(userJson, User::class.java))

                gotoMain()
            }, { error ->
                Snackbar.make(view, "用户名或密码不正确", Snackbar.LENGTH_LONG)
            })
        }
    }

    fun gotoMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // prevent user to go back: killed off the stack
    }
}

