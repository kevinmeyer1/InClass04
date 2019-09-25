package com.kevin.inclass03

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val client = OkHttpClient()

        btnLogIn.setOnClickListener {
            val username = txtUsername.text.toString()
            val password = txtPassword.text.toString()

            if (username.equals("") || password.equals("")) {
                Handler(Looper.getMainLooper()).post(Runnable {
                    Toast.makeText(
                        applicationContext,
                        "Username and/or password cannot be blank.",
                        Toast.LENGTH_SHORT
                    ).show()
                })
                return@setOnClickListener
            }

            val reqJson =
                """
                {
                    "username": "$username",
                    "password": "$password"
                }
                """.trimIndent()

            val url = "https://inclass03.herokuapp.com/login"
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), reqJson)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    println("${e?.message}")
                }

                override fun onResponse(call: Call?, response: Response?) {
                    val body = response?.body()?.string()
                    val jwtToken = body.toString()

                    if (response?.code() == 401) {
                        Handler(Looper.getMainLooper()).post(Runnable {
                            Toast.makeText(
                                applicationContext,
                                "Authorization failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    } else if (response?.code() == 200) {
                        toProfilePage(jwtToken, username)
                    }
                }
            })
        }

        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    fun toProfilePage(jwtToken: String, username: String) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("jwtToken", jwtToken)
        intent.putExtra("username", username)
        startActivity(intent)
    }
}
