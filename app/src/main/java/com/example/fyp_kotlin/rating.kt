package com.example.fyp_kotlin


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import okhttp3.*
import java.io.IOException

class Rating : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)
    }

    private lateinit var editFoodType: EditText

    fun onAddClicked( v : View ){
        var foodType : String = findViewById<EditText>(R.id.editFoodType).text.toString()
        var totalFat : String = findViewById<EditText>(R.id.editTotalfat).text.toString()
        var sugars : String = findViewById<EditText>(R.id.editSugars).text.toString()
        var sodium : String = findViewById<EditText>(R.id.editSodium).text.toString()
        var param = FormBody.Builder()
            .add("foodType", foodType)
            .add("totalFat", totalFat)
            .add("sugars", sugars)
            .add("sodium", sodium)
            .build()
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://bsccom-fyp.chakalvin.repl.co/check")
            .post(param)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { }
            override fun onResponse(call: Call, response: Response) {
                val string = response.body?.string() ?: "[]"
                runOnUiThread {
                    this@Rating.finish()
                }
            }
        })
    }

    fun onCancelClicked(v : View){
        this.finish()
    }
}