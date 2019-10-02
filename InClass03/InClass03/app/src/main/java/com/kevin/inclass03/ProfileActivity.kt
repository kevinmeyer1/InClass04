package com.kevin.inclass03

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.widget.Toast
import okhttp3.*
import java.io.IOException
import kotlinx.android.synthetic.main.activity_profile.*
import org.json.JSONObject
import com.braintreepayments.api.dropin.DropInActivity
import com.braintreepayments.api.dropin.DropInResult





class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val client = OkHttpClient()

        var name: String = ""
        var age: String = ""
        var weight: String = ""
        var address: String = ""

        val jwtToken = getJwt()

        val reqJson =
            """
            {
                "token": "$jwtToken"
            }
            """.trimIndent()

        //validate JWT token with API
        //val url = "http://10.0.2.2:3000/profile"
        val url = "https://inclass03.herokuapp.com/profile"
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
                    //This should never happen
                    Handler(Looper.getMainLooper()).post(Runnable {
                        Toast.makeText(
                            applicationContext,
                            "JWT not validated, unauthorized",
                            Toast.LENGTH_SHORT
                        ).show()

                        toLoginPage()
                    })
                } else if (response?.code() == 200) {
                    val userData = JSONObject(body)

                    name = userData.getString("name")
                    age = userData.getString("age")
                    weight = userData.getString("weight")
                    address = userData.getString("address")

                    //Put userData values onto the screen
                    runOnUiThread {
                        lblName.text = "Name: " + name
                        lblAge.text = "Age: " + age
                        lblWeight.text = "Weight: " + weight
                        lblAddress.text = "Address: " + address
                    }
                } else if (response?.code() == 400) {
                    Handler(Looper.getMainLooper()).post(Runnable {
                        Toast.makeText(
                            applicationContext,
                            "There was an error while getting user data.",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                }
            }
        })

        btnLogout.setOnClickListener {
            removeJwt()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnUpdateProfile.setOnClickListener {
            val intent = Intent(this, UpdateProfileActivity::class.java)

            //pass current user data to fill editTexts on UpdateProfileActivity
            intent.putExtra("name", name)
            intent.putExtra("age", age)
            intent.putExtra("weight", weight)
            intent.putExtra("address", address)

            startActivity(intent)
        }

        btnShopping.setOnClickListener {
            toShoppingPage()
        }
    }

    fun toLoginPage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun getJwt(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        return prefs.getString("jwtToken", null).toString()
    }

    //Delete the JWT saved to the device
    fun removeJwt() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = prefs.edit()
        editor.remove("jwtToken")
        editor.commit()
    }

    fun toShoppingPage() {
        val intent = Intent(this, ShoppingActivity::class.java)
        startActivity(intent)
    }
}
