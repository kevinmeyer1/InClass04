package com.kevin.inclass03

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val client = OkHttpClient()

        //Check if the user has already signed in (jwt is stored on device)
        checkForJwtOnDevice()

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

            val url = "http://10.0.2.2:3000/login"
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

                    if (response?.code() == 401) {
                        Handler(Looper.getMainLooper()).post(Runnable {
                            Toast.makeText(
                                applicationContext,
                                "Authorization failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    } else if (response?.code() == 200) {
                        val tokenJson = JSONObject(body.toString())
                        println(tokenJson["token"].toString())
                        saveJwtToDevice(tokenJson["token"].toString())
                        toProfilePage()
                    }
                }
            })
        }

        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    //sends user to profile page
    fun toProfilePage() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    //saves the JWT to the device
    fun saveJwtToDevice(jwtToken: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = prefs.edit()
        editor.putString("jwtToken", jwtToken)
        editor.commit()
    }

    //checks for a JWT saved on the device
    fun checkForJwtOnDevice() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        if (prefs.getString("jwtToken", null) != null) {
            toProfilePage()
        }
    }
}
