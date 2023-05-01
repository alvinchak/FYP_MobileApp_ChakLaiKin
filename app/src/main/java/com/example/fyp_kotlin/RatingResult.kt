package com.example.fyp_kotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

//add this into the project
data class Contact(val id : String, val foodType: String, val totalFat : String, val sugars : String, val sodium : String)
class RatingResult : AppCompatActivity() {
    val contacts = mutableListOf<Contact>()
    val client = OkHttpClient()

    fun onCheckRatingClicked(v : View){
        val intent = Intent(this, Rating::class.java)
        startActivity(intent)
    }

    fun fetchContacts() {
        val request = Request.Builder()
            .url("http://www.invivo.me/reg/demo/index_json2.php")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { }
            override fun onResponse(call: Call, response: Response) {
                val string = response.body?.string() ?: "[]"
                runOnUiThread {
                    Toast.makeText(this@RatingResult, string, Toast.LENGTH_SHORT).show()
                    processJSON(string)
                }
            }
        })
    }

    fun processJSON(jsonString : String) {
        contacts.clear()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            var id = jsonObject.getString("id")
            var foodType = jsonObject.getString("foodType")
            var totalFat = jsonObject.getString("totalFat")
            var sugars = jsonObject.getString("sugars")
            var sodium = jsonObject.getString("sodium")
            val contact = Contact(id, foodType, totalFat, sugars, sodium)
            contacts.add(contact)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchContacts()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating_result)
    }
}