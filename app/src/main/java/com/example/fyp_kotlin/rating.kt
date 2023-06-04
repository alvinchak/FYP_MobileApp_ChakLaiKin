package com.example.fyp_kotlin


import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

data class Nutrition(val score : String, val grade : String)
data class RatingData(val date_time: String,
                      val product_name: String,
                      val food_type: String,
                      val total_fats: String,
                      val sugars: String,
                      val sodium: String,
                      val score: String,
                      val grade: String)

class Rating : AppCompatActivity() {
    val nutritions = mutableListOf<Nutrition>()
    val client = OkHttpClient()

    private lateinit var postFoodType: String
    private lateinit var postTotalfat: String
    private lateinit var postSugars: String
    private lateinit var postSodium: String

    private val SHARED_PREFS_NAME = "rating"

    fun getGradeDrawable(grade: String): Int {
        return when (grade) {
            "A++" -> R.drawable.grade_a_plus_plus
            "A+" -> R.drawable.grade_a_plus
            "A" -> R.drawable.grade_a
            "B" -> R.drawable.grade_b
            "C" -> R.drawable.grade_c
            "D" -> R.drawable.grade_d
            "E" -> R.drawable.grade_e
            "E-" -> R.drawable.grade_e_minus
            "E--" -> R.drawable.grade_e_minus_minus
            else -> R.drawable.grade_e_minus_minus
        }
    }

    fun fetchNutritions() {
        setLoadingIndicator(true)
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

    fun setLoadingIndicator(isLoading: Boolean) {
        val progressBar = findViewById<ProgressBar>(R.id.loading_progress_bar)
        val gradeImageView = findViewById<ImageView>(R.id.grade_image_view)
        val scoreTextView = findViewById<TextView>(R.id.score_text_view)
        val saveFavouriteLayout = findViewById<LinearLayout>(R.id.save_to_favourite)

        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            gradeImageView.visibility = View.GONE
            scoreTextView.visibility = View.GONE
            saveFavouriteLayout.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            gradeImageView.visibility = View.VISIBLE
            scoreTextView.visibility = View.VISIBLE
            saveFavouriteLayout.visibility = View.VISIBLE
        }
    }

    fun processJSON(jsonString : String) {
        setLoadingIndicator(false)
        nutritions.clear()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            var score = jsonObject.getString("score")
            var grade = jsonObject.getString("grade")
            val nutrition = Nutrition(score, grade)
            nutritions.add(nutrition)
        }
        val nutrition = nutritions[0]
        // 更新 grade ImageView
        val gradeImageView = findViewById<ImageView>(R.id.grade_image_view)
        gradeImageView.setImageResource(getGradeDrawable(nutrition.grade))

        // 更新 score TextView
        val scoreTextView = findViewById<TextView>(R.id.score_text_view)
        scoreTextView.text = "Score: ".plus(nutrition.score)
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


        val buttonSaveToFavourite = findViewById<Button>(R.id.button_save_to_favourite)
        buttonSaveToFavourite.setOnClickListener {
            saveToFavouritesAndNavigate()
        }
    }

    private fun saveToFavouritesAndNavigate() {
        val productNameEditText = findViewById<EditText>(R.id.text_save_to_favourite)
        val productName = productNameEditText.text.toString()
        if (productName.isBlank()) {
            Toast.makeText(this, "Please enter the product name", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val jsonRatingResultList = sharedPreferences.getString("result", null)

        val tempRatingList = mutableListOf<RatingData>()

        if (tempRatingList != null) {
            val gson = Gson()
            val ratingListType = object : TypeToken<List<RatingData>>() {}.type
            val existingRatingList: List<RatingData>? = gson.fromJson(jsonRatingResultList, ratingListType)
            if (existingRatingList != null) {
                tempRatingList.addAll(existingRatingList)
            }
        }

        val newRating = RatingData(
            System.currentTimeMillis().toString(),
            productName,
            postFoodType,
            postTotalfat,
            postSugars,
            postSodium,
            nutritions[0].score,
            nutritions[0].grade
        )
        tempRatingList.add(newRating)

        val editor = sharedPreferences.edit()

        val gson = Gson()
        val updatedJsonRatingList = gson.toJson(tempRatingList)

        editor.putString("result", updatedJsonRatingList)
        editor.apply()

        /*
        AlertDialog.Builder(this)
            .setTitle("Product Details")
            .setMessage(updatedJsonRatingList)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

         */

        val intent = Intent(this, RatingResult::class.java)
        startActivity(intent)

    }



}