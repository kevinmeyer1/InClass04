package com.kevin.inclass03

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.widget.Toast
import com.braintreepayments.api.dropin.DropInActivity
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import kotlinx.android.synthetic.main.activity_cart_activiy.*
import kotlinx.android.synthetic.main.activity_shopping.*
import kotlinx.android.synthetic.main.activity_shopping.listView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.DecimalFormat

class CartActiviy : AppCompatActivity() {

    var totalAmount: Double = 0.00
    val decimalFormat = DecimalFormat("#,###.00")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_activiy)

        val client = OkHttpClient()

        var cart = ArrayList<Item>()
        cart = intent.getParcelableArrayListExtra<Item>("cart")

        val adapter = CartCellAdapter(this, R.layout.cartcell, cart, this)
        listView.adapter = adapter

        for (i in 0 until (cart.size)) {
            totalAmount += cart.get(i).price
        }


        val formattedAmount = decimalFormat.format(totalAmount)
        lblAmount.text = "Total Cost: $" + formattedAmount

        btnCheckout.setOnClickListener {
            val reqJson =
                """
                {
                    "token": "${getJwt()}"
                }
                """.trimIndent()

            val url = "http://10.0.2.2:3000/customer"
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

                    if (response?.code() == 200) {
                        val clientToken = JSONObject(body.toString())["clientToken"].toString()
                        val dropInRequest = DropInRequest()
                            .clientToken(clientToken)
                        startActivityForResult(dropInRequest.getIntent(applicationContext), 1)
                    } else if (response?.code() == 401) {
                        //This should never happen
                        Handler(Looper.getMainLooper()).post(Runnable {
                            Toast.makeText(
                                applicationContext,
                                "JWT not validated, unauthorized",
                                Toast.LENGTH_SHORT
                            ).show()
                        })

                        toProfilePage()
                    }
                }
            })
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val result =
                    data!!.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                // use the result to update your UI and send the payment method nonce to your server

                val formattedAmount = decimalFormat.format(totalAmount)

                val reqJson =
                    """
                    {
                        "paymentMethodNonce": "${result.paymentMethodNonce?.nonce}",
                        "paymentMethodAmount": "${formattedAmount}"
                    }
                    """.trimIndent()

                val client = OkHttpClient()
                val url = "http://10.0.2.2:3000/transaction"
                val body =
                    RequestBody.create(MediaType.parse("application/json; charset=utf-8"), reqJson)
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

                        if (response?.code() == 200) {
                            Handler(Looper.getMainLooper()).post(Runnable {
                                Toast.makeText(
                                    applicationContext,
                                    "Transaction complete.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            })

                            toProfilePage()
                        } else if (response?.code() == 400) {
                            Handler(Looper.getMainLooper()).post(Runnable {
                                Toast.makeText(
                                    applicationContext,
                                    "There was an error while creating transaction",
                                    Toast.LENGTH_SHORT
                                ).show()
                            })

                            toProfilePage()
                        }
                    }
                })
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // the user canceled
                println("canceled")
            } else {
                // handle errors here, an exception may be available in
                val error = data!!.getSerializableExtra(DropInActivity.EXTRA_ERROR) as Exception
                println(error)
            }
        }
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
