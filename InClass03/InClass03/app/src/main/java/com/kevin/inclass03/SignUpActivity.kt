package com.kevin.inclass03

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_sign_up.*
import okhttp3.*
import java.io.IOException

class SignUpActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val client = OkHttpClient()

        btnSignUp.setOnClickListener {
            val username = txtUsername.text.toString()
            val password = txtPassword.text.toString()
            val confirmPassword = txtConfirmPassword.text.toString()
            val name = txtName.text.toString()
            val age = txtAge.text.toString()
            val weight = txtWeight.text.toString()
            val address = txtAddress.text.toString()

            //Only username and password cannot be empty
            if (username.equals("") || password.equals("") || confirmPassword.equals("")) {
                Handler(Looper.getMainLooper()).post(Runnable {
                    Toast.makeText(
                        applicationContext,
                        "Username, password, and confirm password are required.",
                        Toast.LENGTH_SHORT
                    ).show()
                })

                return@setOnClickListener
            }

            //passwords must match
            if (!password.equals(confirmPassword)) {
                Handler(Looper.getMainLooper()).post(Runnable {
                    Toast.makeText(
                        applicationContext,
                        "Passwords must match.",
                        Toast.LENGTH_SHORT
                    ).show()
                })

                return@setOnClickListener
            }

            val reqJson =
                """
                {
                    "username": "$username",
                    "password": "$password",
                    "name": "$name",
                    "age": "$age",
                    "weight": "$weight",
                    "address": "$address"
                }
                """.trimIndent()

            val url = "https://inclass03.herokuapp.com/signup"
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

                    if (response?.code() == 400) {
                        //this should never happen
                        Handler(Looper.getMainLooper()).post(Runnable {
                            Toast.makeText(
                                applicationContext,
                                "There was an error while creating the account",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    } else if (response?.code() == 1062) {
                        Handler(Looper.getMainLooper()).post(Runnable {
                            Toast.makeText(
                                applicationContext,
                                "An account with this username already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    } else if (response?.code() == 201) {
                        Handler(Looper.getMainLooper()).post(Runnable {
                            Toast.makeText(
                                applicationContext,
                                "Account created successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        })

                        backToMain()
                    }
                }
            })
        }
    }

    fun backToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
