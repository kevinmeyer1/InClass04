package com.kevin.inclass03

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.View
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
    var totalQuantity: Int = 0
    val decimalFormat = DecimalFormat("#,###.00")
    var cart = ArrayList<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_activiy)

        val client = OkHttpClient()

        cart = intent.getParcelableArrayListExtra<Item>("cart")

        val adapter = CartCellAdapter(this, R.layout.cartcell, cart, this, ShoppingActivity())
        listView.adapter = adapter

        for (i in 0 until (cart.size)) {
            val costOfQuantity = cart.get(i).price * cart.get(i).amount

            totalAmount += costOfQuantity
        }

        for (i in 0 until (cart.size)) {
            val itemQuantity = cart.get(i).amount

            totalQuantity += itemQuantity
        }

        lblTotalQuantity.text = "Total Quantity: " + totalQuantity


        val formattedAmount = decimalFormat.format(totalAmount)
        lblAmount.text = "Total Cost: $" + formattedAmount

        btnCheckout.setOnClickListener {
            toCardPage()
        }
    }

    fun toCardPage() {
        val intent = Intent(this, CardActivity::class.java)

        val formattedAmount = decimalFormat.format(totalAmount)

        println("cart activity: " + formattedAmount)

        intent.putExtra("chargeAmount", formattedAmount)
        startActivity(intent)
    }

    fun refreshTotalPrice() {
        totalAmount = 0.00

        for (i in 0 until (cart.size)) {
            val costOfQuantity = cart.get(i).price * cart.get(i).amount

            totalAmount += costOfQuantity
        }

        val formattedAmount = decimalFormat.format(totalAmount)
        lblAmount.text = "Total Cost: $" + formattedAmount
    }

    fun refreshTotalQuantity() {
        totalQuantity = 0

        for (i in 0 until (cart.size)) {
            val itemQuantity = cart.get(i).amount

            totalQuantity += itemQuantity
        }

        lblTotalQuantity.text = "Total Quantity: " + totalQuantity
    }
}
