package com.kevin.inclass03

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import okhttp3.*
import java.io.IOException
import android.util.Base64
import android.view.View
import kotlinx.android.synthetic.main.activity_profile.*
import org.json.JSONObject


class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val client = OkHttpClient()

        val jwtToken = intent.getStringExtra("jwtToken")
        val username = intent.getStringExtra("username")
        val reqJson =
            """
                {
                    "token": "$jwtToken"
                }
                """.trimIndent()

        //validate JWT token with API
        val url = "http:/10.0.2.2:3000/profile"
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

                if (body == "success") {
                    //Decode JWT payload (stuff.payload.stuff -> splitToken[1]) and make it a JSON Object
                    val splitToken = jwtToken.split(".")
                    val jwtPayload = String(Base64.decode(splitToken[1], Base64.URL_SAFE))
                    val userData = JSONObject(jwtPayload)

                    //Put userData values onto the screen
                    lblName.text = "Name: " + userData.getString("name")
                    lblAge.text = "Age: " + userData.getString("age")
                    lblWeight.text = "Weight: " + userData.getString("weight")
                    lblAddress.text = "Address: " + userData.getString("address")

                } else {
                    //This should never happen
                    Handler(Looper.getMainLooper()).post(Runnable {
                        Toast.makeText(
                            applicationContext,
                            "JWT not validated, force back to Login screen.",
                            Toast.LENGTH_SHORT
                        ).show()
                        toLoginPage()
                    })
                }
            }
        })

        btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnUpdateProfile.setOnClickListener {
            val intent = Intent(this, UpdateProfileActivity::class.java)
            println(jwtToken)
            intent.putExtra("jwtToken", jwtToken)
            intent.putExtra("username", username)
            startActivity(intent)
        }
    }

    fun toLoginPage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
