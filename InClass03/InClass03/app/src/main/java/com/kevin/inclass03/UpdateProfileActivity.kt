package com.kevin.inclass03

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_update_profile.*
import okhttp3.*
import java.io.IOException

class UpdateProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)

        val jwtToken = intent.getStringExtra("jwtToken")
        val username = intent.getStringExtra("username")

        val client = OkHttpClient()

        btnCancel.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("jwtToken", jwtToken);
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
                    "username": "$username",
                    "token": "$jwtToken",
                    "name": "$name",
                    "age": "$age",
                    "weight": "$weight",
                    "address": "$address"
                }
                """.trimIndent()

            val url = "https://inclass03-api-only.herokuapp.com/update_profile"
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
                        //the returned value is the new JWT token with new user info
                        val newJwtToken = body.toString()
                        toProfilePage(newJwtToken)
                    }
                }
            })
        }
    }

    fun toLoginPage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun toProfilePage(newJwtToken: String) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("jwtToken", newJwtToken);
        startActivity(intent)
    }
}
