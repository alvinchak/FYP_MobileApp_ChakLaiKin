package com.example.fyp_kotlin


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

data class Nutrition(val id : String, val score : String, val grade : String)
class Rating : AppCompatActivity() {
    val nutritions = mutableListOf<Nutrition>()
    val client = OkHttpClient()

    private lateinit var postFoodType: String
    private lateinit var postTotalfat: String
    private lateinit var postSugars: String
    private lateinit var postSodium: String

    fun fetchNutritions() {
        var foodType : String = postFoodType.toString()
        var totalFat : String = postTotalfat.toString()
        var sugars : String = postSugars.toString()
        var sodium : String = postSodium.toString()
        var param = FormBody.Builder()
            .add("foodType", foodType)
            .add("totalFat", totalFat)
            .add("sugars", sugars)
            .add("sodium", sodium)
            .build()
        val request = Request.Builder()
            .url("https://bsccom-fyp.chakalvin.repl.co/check")
            .post(param)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { }
            override fun onResponse(call: Call, response: Response) {
                val string = response.body?.string() ?: "[]"
                runOnUiThread {
                    Toast.makeText(this@Rating, string, Toast.LENGTH_SHORT).show()
                    processJSON(string)
                }
            }
        })
    }

    fun processJSON(jsonString : String) {
        nutritions.clear()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            var id = "1"
            var score = jsonObject.getString("score")
            var grade = jsonObject.getString("grade")
            val nutrition = Nutrition(id, score, grade)
            nutritions.add(nutrition)
        }
        val adapter = NutritionAdapter(this, android.R.layout.simple_expandable_list_item_2,
            nutritions)
        val listView = findViewById<ListView>(R.id.listView)
        listView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        fetchNutritions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)
        postFoodType = intent.getStringExtra("FOOD_TYPE_KEY") ?: ""
        postTotalfat = intent.getStringExtra("TOTAL_FATS_KEY") ?: ""
        postSugars = intent.getStringExtra("SUGARS_KEY") ?: ""
        postSodium = intent.getStringExtra("SODIUM_KEY") ?: ""
    }

}