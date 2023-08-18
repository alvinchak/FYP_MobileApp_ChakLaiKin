package com.example.fyp_kotlin

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.usb.UsbDevice.getDeviceId
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import android.provider.Settings

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
                    put("dateTime", ratingData.dateTime)
                    put("productName", ratingData.productName)
                    put("manufacturer", ratingData.manufacturer)
                    put("productBarcode", ratingData.productBarcode)
                    put("foodType", ratingData.foodType)
                    put("energy", ratingData.energy)
                    put("satuFat", ratingData.satuFat)
                    put("sugars", ratingData.sugars)
                    put("sodium", ratingData.sodium)
                    put("fruitVeget", ratingData.fruitVeget)
                    put("fibre", ratingData.fibre)
                    put("protein", ratingData.protein)
                    put("score", ratingData.score)
                    put("grade", ratingData.grade)
                }
                products.add(productJson)
            }
        }

        /*
        AlertDialog.Builder(this)
            .setTitle("Product Details")
            .setMessage(products.toString())
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

         */


        // Sort products list by unique_id in descending order
        //products.sortByDescending { it.optString("unique_id", "0").toIntOrNull() ?: 0 }

        val adapter = RatingResultAdapter(this, products)
        listView.adapter = adapter

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
        val sharedPreferences = _context.getSharedPreferences("rating", Context.MODE_PRIVATE)
        val jsonRatingResultList = sharedPreferences.getString("result", null)
        dataSource.removeAt(position)

        val tempRatingList = mutableListOf<RatingData>()

        if (tempRatingList != null) {
            val gson = Gson()
            val ratingListType = object : TypeToken<List<RatingData>>() {}.type
            val existingRatingList: List<RatingData>? = gson.fromJson(jsonRatingResultList, ratingListType)
            if (existingRatingList != null) {
                tempRatingList.addAll(existingRatingList)
            }
        }

        tempRatingList.removeAt(position)

        val editor = sharedPreferences.edit()
        val gson = Gson()
        val updatedJsonRatingList = gson.toJson(tempRatingList)

        editor.putString("result", updatedJsonRatingList)
        editor.apply()

        /*
        AlertDialog.Builder(_context)
            .setTitle("Product Details")
            .setMessage(updatedJsonRatingList)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

         */


        notifyDataSetChanged()
    }

    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = convertView ?: inflater.inflate(R.layout.list_item_rating_result, parent, false)

        val productData = getItem(position)
        val productNameTextView = rowView.findViewById<TextView>(R.id.text_product_name)
        val manufacturerTextView = rowView.findViewById<TextView>(R.id.text_manufacturer)
        val scoreTextView = rowView.findViewById<TextView>(R.id.text_score)
        val gradeImageView = rowView.findViewById<ImageView>(R.id.image_grade)

        productNameTextView.text = productData.getString("productName")
        manufacturerTextView.text = productData.getString("manufacturer")
        scoreTextView.text = productData.getString("score")

        val grade = productData.getString("grade")
        val gradeDrawable = when (grade) {
            "A" -> R.drawable.mini_grade_a
            "B" -> R.drawable.mini_grade_b
            "C" -> R.drawable.mini_grade_c
            "D" -> R.drawable.mini_grade_d
            "E" -> R.drawable.mini_grade_e
            else -> R.drawable.mini_grade_e
        }
        gradeImageView.setImageResource(gradeDrawable)

        rowView.setOnClickListener {
            val productData = getItem(position)
            val details = """
                Product Name: ${productData.getString("productName")}
                Barcode: ${productData.getString("productBarcode")}
                ------------------
                Manufacturer: ${productData.getString("manufacturer")}
                ------------------
                Food Type: ${productData.getString("foodType")}
                ------------------
                Energy: ${productData.getString("energy")}kcal
                Saturated Fat: ${productData.getString("satuFat")}g
                Sugars: ${productData.getString("sugars")}g
                Sodium: ${productData.getString("sodium")}mg
                Fruit and vegetables: ${productData.getString("fruitVeget")}%
                Fibre: ${productData.getString("fibre")}g
                Protein: ${productData.getString("protein")}g
                ------------------
                Grade: ${productData.getString("grade")}
                Score: ${productData.getString("score")}
            """.trimIndent()

            AlertDialog.Builder(_context)
                .setTitle("Product Details")
                .setMessage(details)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setNegativeButton("Share") { dialog, _ ->
                    AlertDialog.Builder(_context)
                        .setTitle("Confirm")
                        .setMessage("Confirm to share?")
                        .setPositiveButton("Yes") { confirmDialog, _ ->
                            confirmDialog.dismiss()
                            dialog.dismiss()
                            // Show loading animation and disable back button
                            val progressDialog = ProgressDialog(_context).apply {
                                setMessage("Sharing...")
                                setCancelable(false)
                                show()
                            }

                            val tempRequestBody = productData
                            val androidId = getAndroidId(_context)
                            tempRequestBody.remove("dateTime")
                            tempRequestBody.put("platformId", androidId)
                            val requestBody = tempRequestBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

                            // Send POST request
                            val request = Request.Builder()
                                .url("https://6001cem-fyp.chakalvin.repl.co/api/v1/products/share")
                                .post(requestBody)
                                .build()

                            val client = OkHttpClient()

                            client.newCall(request).enqueue(object : Callback {
                                override fun onResponse(call: Call, response: Response) {
                                    // Close loading animation and enable back button
                                    progressDialog.dismiss()
                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(_context, "Shared successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call, e: IOException) {
                                    // Close loading animation and enable back button
                                    progressDialog.dismiss()
                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(_context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            })
                        }
                        .setNegativeButton("No") { confirmDialog, _ ->
                            confirmDialog.dismiss()
                        }
                        .show()
                }
                .show()
        }
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