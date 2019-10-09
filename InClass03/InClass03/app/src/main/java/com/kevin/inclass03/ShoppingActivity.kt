package com.kevin.inclass03

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_shopping.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class ShoppingActivity : AppCompatActivity() {

    val itemList = ArrayList<Item>()
    var cart = ArrayList<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping)

        val jsonObj = JSONObject(readJSONFromAsset())["results"]
        val shoppingArr = JSONArray(jsonObj.toString())

        for (i in 0 until (shoppingArr.length())) {
            val jsonItem = shoppingArr.getJSONObject(i)
            val item = Item(
                jsonItem.getInt("discount"),
                jsonItem.getString("name"),
                jsonItem.getString("photo"),
                jsonItem.getDouble("price"),
                jsonItem.getString("region"),
                1
            )

            itemList.add(item)
        }

        val adapter = ItemCellAdapter(this, R.layout.itemcell, itemList, this)
        listView.adapter = adapter


        btnCart.setOnClickListener {
            val intent = Intent(this, CartActiviy::class.java)
            intent.putParcelableArrayListExtra("cart", cart)
            startActivity(intent)
        }
    }

    fun readJSONFromAsset(): String? {
        var json: String?
        try {
            val  inputStream: InputStream = assets.open("discount.json")
            json = inputStream.bufferedReader().use{it.readText()}
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun addToCart(item: Item) {
        this.cart.add(item)
    }

    fun removeItemFromCart(item: Item) {
        this.cart.remove(item)
    }

    fun updateCart(newCart: ArrayList<Item>) {
        this.cart = newCart
    }
}
