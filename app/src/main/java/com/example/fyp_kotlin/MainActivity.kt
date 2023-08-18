package com.example.fyp_kotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var logoImageView: ImageView
    private lateinit var recommendListView: ListView
    private lateinit var startRatingButton: Button
    private lateinit var nutriScoreRatingButton: Button
    private lateinit var myFavouritesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        logoImageView = findViewById(R.id.logoImageView)
        recommendListView = findViewById(R.id.recommendListView)
        startRatingButton = findViewById(R.id.startRatingButton)
        nutriScoreRatingButton = findViewById(R.id.nutriScoreRatingButton)
        myFavouritesButton = findViewById(R.id.myFavouritesButton)

        // Load data from API using OkHttp3
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://6001cem-fyp.chakalvin.repl.co/api/v1/products/recommend")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val jsonData = response.body!!.string()
                    val jsonArray = JSONArray(jsonData)

                    // Process JSON data and update ListView
                    runOnUiThread {

                        val adapter = ProductAdapter(this@MainActivity, R.layout.list_item, mutableListOf())

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val product = Product(
                                productbarcode = jsonObject.getString("productbarcode"),
                                productname = jsonObject.getString("productname"),
                                manufacturer = jsonObject.getString("manufacturer"),
                                score = jsonObject.getString("score"),
                                grade = jsonObject.getString("grade"),
                                foodtype = jsonObject.getString("foodtype"),
                                energy = jsonObject.getDouble("energy"),
                                sugars = jsonObject.getDouble("sugars"),
                                satufat = jsonObject.getDouble("satufat"),
                                sodium = jsonObject.getDouble("sodium"),
                                fruitveget = jsonObject.getDouble("fruitveget"),
                                fibre = jsonObject.getDouble("fibre"),
                                protein = jsonObject.getDouble("protein")
                            )
                            adapter.add(product)
                            loadingProgressBar.visibility = View.GONE
                        }

                        recommendListView.adapter = adapter

                        // TODO: Process JSON data, create Product objects and add them to the adapter

                        // Set onItemClickListener for ListView
                        recommendListView.setOnItemClickListener { _, _, position, _ ->
                            val product = adapter.getItem(position)
                            val message = product?.toDisplayString() ?: "Unknown product"

                            AlertDialog.Builder(this@MainActivity)
                                .setTitle(product?.productname ?: "Unknown")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                }
            }
        })

        // Set onClickListeners for buttons
        startRatingButton.setOnClickListener {
            val intent = Intent(this, Scanner::class.java)
            startActivity(intent)
        }

        nutriScoreRatingButton.setOnClickListener {
            val intent = Intent(this, Information::class.java)
            startActivity(intent)
        }

        myFavouritesButton.setOnClickListener {
            val intent = Intent(this, RatingResult::class.java)
            startActivity(intent)
        }
    }

    data class Product(
        val productbarcode: String,
        val productname: String,
        val manufacturer: String,
        val score: String,
        val grade: String,
        val foodtype: String,
        val energy: Double,
        val sugars: Double,
        val satufat: Double,
        val sodium: Double,
        val fruitveget: Double,
        val fibre: Double,
        val protein: Double
    ) {
        fun toDisplayString(): String {
            return """
                Product Barcode: $productbarcode
                Product Name: $productname
                --------------------
                Manufacturer: $manufacturer
                --------------------
                Score: $score
                Grade: $grade
                --------------------
                Food Type: $foodtype
                --------------------
                [Nutrients]
                Energy: $energy
                Sugars: $sugars
                Saturated Fat: $satufat
                Sodium: $sodium
                Fruit & Vegetables: $fruitveget
                Fibre: $fibre
                Protein: $protein
            """.trimIndent()
        }
    }
}

class ProductAdapter(context: Context, resource: Int, objects: MutableList<MainActivity.Product>) :
    ArrayAdapter<MainActivity.Product>(context, resource, objects) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.list_item, parent, false)
        val product = getItem(position)

        val productNameTextView = view.findViewById<TextView>(R.id.productNameTextView)
        val manufacturerTextView = view.findViewById<TextView>(R.id.manufacturerTextView)
        val gradeImageView = view.findViewById<ImageView>(R.id.gradeImageView)
        val scoreTextView = view.findViewById<TextView>(R.id.scoreTextView)

        productNameTextView.text = product?.productname
        manufacturerTextView.text = product?.manufacturer
        scoreTextView.text = product?.score.toString()

        // Set the grade image based on the grade value
        val gradeResource = when (product?.grade) {
            "A" -> R.drawable.mini_grade_a
            "B" -> R.drawable.mini_grade_b
            "C" -> R.drawable.mini_grade_c
            "D" -> R.drawable.mini_grade_d
            "E" -> R.drawable.mini_grade_e
            else -> R.drawable.mini_grade_e
        }
        gradeImageView.setImageResource(gradeResource)

        return view
    }
}

