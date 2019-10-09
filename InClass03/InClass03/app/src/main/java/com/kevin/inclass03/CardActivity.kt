package com.kevin.inclass03

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.widget.Toast
import com.stripe.android.Stripe
import com.stripe.android.ApiResultCallback
import com.stripe.android.model.Token
import com.stripe.android.model.Card
import kotlinx.android.synthetic.main.activity_card.*
import okhttp3.*
import java.io.IOException
import java.lang.Exception

class CardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        val chargeAmountString = intent.getStringExtra("chargeAmount")
        val chargeAmount = chargeAmountString.toDouble()

        println("card activity: " + chargeAmount)

        btnAddCard.setOnClickListener {
            var custCard : Card?
            custCard = card_widget.card

            if (custCard == null) {
                Handler(Looper.getMainLooper()).post(Runnable {
                    Toast.makeText(
                        applicationContext,
                        "Please add all card details",
                        Toast.LENGTH_SHORT
                    ).show()
                })
            } else {
                Handler(Looper.getMainLooper()).post(Runnable {
                    Toast.makeText(
                        applicationContext,
                        "Card added, transaction in progress",
                        Toast.LENGTH_SHORT
                    ).show()

                    tokenizeCard(custCard, chargeAmount)
                })
            }

        }

    }

    fun tokenizeCard(card: Card, chargeAmount: Double) {
        var stripe = Stripe(applicationContext,"pk_test_ylqAzIaIWW0phnBiyBf2izTP00fLjAHCEw")
        var client = OkHttpClient()

        stripe.createToken(
            card,
            object : ApiResultCallback<Token> {
                override fun onSuccess(token: Token) {
                    println("card has been tokenized")
                    println(token.id)

                    val cardJson =
                        """
                        {
                            "cardToken": "${token.id}",
                            "token": "${getJwt()}"
                        }
                        """.trimIndent()

                    val url = "http://10.0.2.2:3000/addCard"
                    //val url = "https://inclass03.herokuapp.com/customer"
                    val cardReqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), cardJson)
                    val request = Request.Builder()
                        .url(url)
                        .post(cardReqBody)
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) {
                            println("${e?.message}")
                        }

                        override fun onResponse(call: Call?, response: Response?) {
                            if (response?.code() == 400) {
                                //This should never happen
                                Handler(Looper.getMainLooper()).post(Runnable {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error while adding card",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                            } else if (response?.code() == 200) {
                                createCharge(chargeAmount)
                            }
                        }
                    })
                }

                override fun onError(error: Exception) {
                    Handler(Looper.getMainLooper()).post(Runnable {
                        Toast.makeText(
                            applicationContext,
                            error.localizedMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                }
            }
        )
    }

    fun createCharge(chargeAmount: Double) {
        val client = OkHttpClient()

        val chargeJson =
            """
            {
                "token": "${getJwt()}",
                "chargeAmount": "${chargeAmount}"
            }
            """.trimIndent()

        val url = "http://10.0.2.2:3000/charge"
        //val url = "https://inclass03.herokuapp.com/customer"
        val chargeReqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), chargeJson)
        val request = Request.Builder()
            .url(url)
            .post(chargeReqBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                println("${e?.message}")
            }

            override fun onResponse(call: Call?, response: Response?) {
                //charge has gone through
                if (response?.code() == 400) {
                    Handler(Looper.getMainLooper()).post(Runnable {
                        Toast.makeText(
                            applicationContext,
                            "Error while adding card",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                } else if (response?.code() == 200) {
                    Handler(Looper.getMainLooper()).post(Runnable {
                        Toast.makeText(
                            applicationContext,
                            "Transaction complete.",
                            Toast.LENGTH_SHORT
                        ).show()

                        toProfilePage()
                    })
                }
            }
        })
    }

    fun getJwt(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        return prefs.getString("jwtToken", null).toString()
    }

    fun toProfilePage() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }
}
