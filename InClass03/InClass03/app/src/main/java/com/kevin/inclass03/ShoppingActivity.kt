package com.kevin.inclass03

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_shopping.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class ShoppingActivity : AppCompatActivity() {

    val itemList = ArrayList<Item>()

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
                jsonItem.getString("region")
            )

            itemList.add(item)
        }

        //println(itemList.size)

        val adapter = ItemCellAdapter(this, R.layout.itemcell, itemList)

        listView.adapter = adapter
    }

    fun readJSONFromAsset(): String? {
        var json: String? = null
        try {
            val  inputStream: InputStream = assets.open("discount.json")
            json = inputStream.bufferedReader().use{it.readText()}
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }
}
