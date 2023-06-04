package com.example.fyp_kotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject

class RatingResult : AppCompatActivity() {

    private val SHARED_PREFS_NAME = "rating"
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating_result)

        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)


        val listView = findViewById<ListView>(R.id.list_view)
        val products = mutableListOf<JSONObject>()

        val jsonRatingResultList = sharedPreferences.getString("result", null)

        val tempRatingList = mutableListOf<RatingData>()

        if (jsonRatingResultList != null) {
            val gson = Gson()
            val ratingListType = object : TypeToken<List<RatingData>>() {}.type
            val existingRatingList: List<RatingData>? = gson.fromJson(jsonRatingResultList, ratingListType)
            if (existingRatingList != null) {
                tempRatingList.addAll(existingRatingList)
            }

            tempRatingList.forEach { ratingData ->
                val productJson = JSONObject().apply {
                    put("date_time", ratingData.date_time)
                    put("product_name", ratingData.product_name)
                    put("food_type", ratingData.food_type)
                    put("total_fats", ratingData.total_fats)
                    put("sugars", ratingData.sugars)
                    put("sodium", ratingData.sodium)
                    put("score", ratingData.score)
                    put("grade", ratingData.grade)
                }
                products.add(productJson)
            }
        }


        // Sort products list by unique_id in descending order
        //products.sortByDescending { it.optString("unique_id", "0").toIntOrNull() ?: 0 }

        val adapter = RatingResultAdapter(this, products)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val productData = products[position]
            val details = """
                Product Name: ${productData.getString("product_name")}
                Food Type: ${productData.getString("food_type")}
                Total Fats: ${productData.getString("total_fats")}
                Sugars: ${productData.getString("sugars")}
                Sodium: ${productData.getString("sodium")}
                Score: ${productData.getString("score")}
                Grade: ${productData.getString("grade")}
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Product Details")
                .setMessage(details)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        val buttonBackToMain = findViewById<Button>(R.id.button_back_to_main)
        buttonBackToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

}

// 为适配器创建一个新的类
class RatingResultAdapter (
    private val _context: Context,
    private val dataSource: MutableList<JSONObject>
) : ArrayAdapter<JSONObject>(_context, 0, dataSource) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): JSONObject {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun removeItem(position: Int) {
        val sharedPreferences = _context.getSharedPreferences("rating_prefs", Context.MODE_PRIVATE)
        val uniqueId = dataSource[position].optString("unique_id")
        dataSource.removeAt(position)
        sharedPreferences.edit().putString(uniqueId, dataSource.toString())
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = convertView ?: inflater.inflate(R.layout.list_item_rating_result, parent, false)

        val productData = getItem(position)
        val productNameTextView = rowView.findViewById<TextView>(R.id.text_product_name)
        val scoreTextView = rowView.findViewById<TextView>(R.id.text_score)
        val gradeImageView = rowView.findViewById<ImageView>(R.id.image_grade)

        productNameTextView.text = productData.getString("product_name")
        scoreTextView.text = productData.getString("score")

        val grade = productData.getString("grade")
        val gradeDrawable = when (grade) {
            "A++" -> R.drawable.mini_grade_a_plus_plus
            "A+" -> R.drawable.mini_grade_a_plus
            "A" -> R.drawable.mini_grade_a
            "B" -> R.drawable.mini_grade_b
            "C" -> R.drawable.mini_grade_c
            "D" -> R.drawable.mini_grade_d
            "E" -> R.drawable.mini_grade_e
            "E-" -> R.drawable.mini_grade_e_minus
            "E--" -> R.drawable.mini_grade_e_minus_minus
            else -> R.drawable.grade_e
        }
        gradeImageView.setImageResource(gradeDrawable)

        val deleteButton = rowView.findViewById<ImageButton>(R.id.button_delete)
        deleteButton.setOnClickListener {
            AlertDialog.Builder(_context)
                .setTitle("Confirm")
                .setMessage("Confirm to remove this product?")
                .setPositiveButton("Yes") { dialog, _ ->
                    removeItem(position)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        return rowView
    }
}