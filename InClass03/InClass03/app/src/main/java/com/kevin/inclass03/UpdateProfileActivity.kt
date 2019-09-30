package com.kevin.inclass03

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_update_profile.*
import okhttp3.*
import java.io.IOException

class UpdateProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)

        val profileName = intent.getStringExtra("name")
        val profileAge = intent.getStringExtra("age")
        val profileWeight = intent.getStringExtra("weight")
        val profileAddress = intent.getStringExtra("address")

        //set the text of the editTexts to the data already set for the user.
        txtName.setText(profileName)
        txtAge.setText(profileAge)
        txtWeight.setText(profileWeight)
        txtAddress.setText(profileAddress)

        val client = OkHttpClient()
        val jwtToken = getJwt()

        btnCancel.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        btnUpdate.setOnClickListener {
            val name = txtName.text.toString()
            val age = txtAge.text.toString()
            val weight = txtWeight.text.toString()
            val address = txtAddress.text.toString()

            val reqJson =
                """
                {
                    "token": "$jwtToken",
                    "name": "$name",
                    "age": "$age",
                    "weight": "$weight",
                    "address": "$address"
                }
                """.trimIndent()

            val url = "http://10.0.2.2:3000/update_profile"
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
                        //user is not authenticated, force back to login
                        Handler(Looper.getMainLooper()).post(Runnable {
                            Toast.makeText(
                                applicationContext,
                                "JWT not validated, unauthorized",
                                Toast.LENGTH_SHORT
                            ).show()
                        })

                        toLoginPage()
                    } else if (response?.code() == 400) {
                        Handler(Looper.getMainLooper()).post(Runnable {
                            Toast.makeText(
                                applicationContext,
                                "There was an error updating the user information",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    } else if (response?.code() == 200) {
                        //user data has been updated. the user will see changes when they are returned to profile
                        toProfilePage()
                    }
                }
            })
        }
    }

    fun toLoginPage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun toProfilePage() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    fun getJwt(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        return prefs.getString("jwtToken", null).toString()
    }
}
