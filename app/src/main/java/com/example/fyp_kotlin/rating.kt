package com.example.fyp_kotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

data class Nutrition(val score : String, val grade : String)
data class RatingData(val dateTime: Int,
                      val productName: String,
                      val productBarcode: String,
                      val foodType: String,
                      val energy: String,
                      val satuFat: String,
                      val sugars: String,
                      val sodium: String,
                      val fruitVeget: String,
                      val fibre: String,
                      val protein: String,
                      val score: String,
                      val grade: String)

class Rating : AppCompatActivity() {
    val nutritions = mutableListOf<Nutrition>()
    val client = OkHttpClient()

    private lateinit var postFoodType: String
    private lateinit var postEnergy: String
    private lateinit var postSatuFat: String
    private lateinit var postSugars: String
    private lateinit var postSodium: String
    private lateinit var postFruitVeget: String
    private lateinit var postFibre: String
    private lateinit var postProtein: String

    private val SHARED_PREFS_NAME = "rating"

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_GALLERY = 2
    }

    fun getGradeDrawable(grade: String): Int {
        return when (grade) {
            "A" -> R.drawable.grade_a
            "B" -> R.drawable.grade_b
            "C" -> R.drawable.grade_c
            "D" -> R.drawable.grade_d
            "E" -> R.drawable.grade_e
            else -> R.drawable.grade_e_minus_minus
        }
    }

    fun fetchNutritions() {
        setLoadingIndicator(true)
        val foodType : String = postFoodType.toString()
        val energy : String = postEnergy.toString()
        val satuFat : String = postSatuFat.toString()
        val sugars : String = postSugars.toString()
        val sodium : String = postSodium.toString()
        val fruitVeget : String = postFruitVeget.toString()
        val fibre : String = postFibre.toString()
        val protein : String = postProtein.toString()
        val param = FormBody.Builder()
            .add("foodType", foodType)
            .add("energy", energy)
            .add("sugars", sugars)
            .add("satuFat", satuFat)
            .add("sodium", sodium)
            .add("fruitVeget", fruitVeget)
            .add("fibre", fibre)
            .add("protein", protein)
            .build()
        val request = Request.Builder()
            .url("https://bsccom-fyp.chakalvin.repl.co/check2")
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
        postEnergy = intent.getStringExtra("ENERGY_KEY") ?: ""
        postSatuFat = intent.getStringExtra("SATU_FAT_KEY") ?: ""
        postSugars = intent.getStringExtra("SUGARS_KEY") ?: ""
        postSodium = intent.getStringExtra("SODIUM_KEY") ?: ""
        postFruitVeget = intent.getStringExtra("FRUIT_VEGET_KEY") ?: ""
        postFibre = intent.getStringExtra("FIBRE_KEY") ?: ""
        postProtein = intent.getStringExtra("PROTEIN_KEY") ?: ""

        val buttonScanBarcode = findViewById<Button>(R.id.button_scan_barcode)
        buttonScanBarcode.setOnClickListener {
            scanBarcode()
        }

        val buttonSaveToFavourite = findViewById<Button>(R.id.button_save_to_favourite)
        buttonSaveToFavourite.setOnClickListener {
            saveToFavouritesAndNavigate()
        }

        val buttonBackToHome = findViewById<Button>(R.id.button_back_to_home)
        buttonBackToHome.setOnClickListener {
            val intent = Intent(this, Scanner::class.java)
            startActivity(intent)
        }
    }

    private fun scanBarcode() {
        //這類接著使用ML Kit的barcode scanning：透過prompt box詢問用戶使要camera拍攝圖片還是透過gallery讀取圖片，然後直接使用ML Kit的barcode scanning，自動讀取圖片的raw value，並填寫到edit text id「text_scan_barcode」上。
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
            .setItems(options) { _, which ->
                when (options[which]) {
                    "Camera" -> takePhotoFromCamera()
                    "Gallery" -> choosePhotoFromGallery()
                }
            }
        builder.create().show()
    }

    private fun takePhotoFromCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun choosePhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    processImage(imageBitmap)
                }
                REQUEST_IMAGE_GALLERY -> {
                    val selectedImage = data?.data
                    val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                    processImage(imageBitmap)
                }
            }
        }
    }

    private fun processImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()

        val scanner = BarcodeScanning.getClient(options)
        val task = scanner.process(image)
        task.addOnSuccessListener { barcodes ->
            if (barcodes.isNotEmpty()) {
                val rawValue = barcodes.first().rawValue
                val editText = findViewById<EditText>(R.id.text_scan_barcode)
                editText.setText(rawValue)
            } else {
                Toast.makeText(this, "No barcode detected", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToFavouritesAndNavigate() {
        val productNameEditText = findViewById<EditText>(R.id.text_save_to_favourite)
        val productName = productNameEditText.text.toString()
        if (productName.isBlank()) {
            Toast.makeText(this, "Please enter the product name", Toast.LENGTH_SHORT).show()
            return
        }

        val productBarcodeEditText = findViewById<EditText>(R.id.text_scan_barcode)
        val productBarcode = productBarcodeEditText.text.toString()
        if (productBarcode.isBlank()) {
            Toast.makeText(this, "Please scan the product barcode", Toast.LENGTH_SHORT).show()
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
            System.currentTimeMillis().toInt(),
            productName,
            productBarcode,
            postFoodType,
            postEnergy,
            postSatuFat,
            postSugars,
            postSodium,
            postFruitVeget,
            postFibre,
            postProtein,
            nutritions[0].score,
            nutritions[0].grade
        )
        tempRatingList.add(newRating)
        tempRatingList.sortByDescending { it.dateTime }

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