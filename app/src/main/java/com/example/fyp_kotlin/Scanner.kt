package com.example.fyp_kotlin

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import okhttp3.*
import java.io.IOException

class Scanner : AppCompatActivity() {

    fun onCheckRatingClicked(v: View) {
        val radioGroup = findViewById<RadioGroup>(R.id.foodType)
        val selectedRadioButtonId = radioGroup.checkedRadioButtonId
        val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
        val foodType = selectedRadioButton.text.toString()

        val energy = findViewById<EditText>(R.id.editEnergy).text.toString()
        val satuFat = findViewById<EditText>(R.id.editSatuFat).text.toString()
        val sugars = findViewById<EditText>(R.id.editSugars).text.toString()
        val sodium = findViewById<EditText>(R.id.editSodium).text.toString()
        var fruitVeget = findViewById<EditText>(R.id.editFruitVeget)?.text?.toString()
        var fibre = findViewById<EditText>(R.id.editFibre)?.text?.toString()
        val protein = findViewById<EditText>(R.id.editProtein).text.toString()
        if (fruitVeget?.toDoubleOrNull() == null) {fruitVeget = "0"}
        if (fibre?.toDoubleOrNull() == null) fibre = "0"

        val intent = Intent(this, Rating::class.java)
        intent.putExtra("FOOD_TYPE_KEY", foodType)
        intent.putExtra("ENERGY_KEY", energy)
        intent.putExtra("SATU_FAT_KEY", satuFat)
        intent.putExtra("SUGARS_KEY", sugars)
        intent.putExtra("SODIUM_KEY", sodium)
        intent.putExtra("FRUIT_VEGET_KEY", fruitVeget)
        intent.putExtra("FIBRE_KEY", fibre)
        intent.putExtra("PROTEIN_KEY", protein)
        startActivity(intent)
    }

    //UI Views
    private lateinit var inputImageBtn: MaterialButton
    private lateinit var recognizeTextBtn: MaterialButton
    private lateinit var imageIv: ImageView
    //private lateinit var recognizedTextEt: EditText
    private lateinit var positionEnergy: String
    private lateinit var positionValueEnergy: String
    private lateinit var positionSatuFat: String
    private lateinit var positionSugars: String
    private lateinit var positionSodium: String
    private lateinit var positionProtein: String

    private lateinit var valueEnergy: EditText
    private lateinit var valueSatuFat: EditText
    private lateinit var valueSugars: EditText
    private lateinit var valueSodium: EditText
    private lateinit var valueFruitVeget: EditText
    private lateinit var valueFibre: EditText
    private lateinit var valueProtein: EditText

    private companion object{
        //to handle the result of Camera/Gallery permissions in onRequestPermissionResults
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101
    }

    //Uri of the image that we will take from Camera/Gallery
    private var imageUri: Uri? = null

    //arrays of permission required to pick image from Camera/Gallery
    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>

    //Progress Dialog
    private lateinit var progressDialog: ProgressDialog

    //TextRecognizer
    private lateinit var textRecognizer: TextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        //init UI Views
        inputImageBtn = findViewById(R.id.inputImageBtn)
        recognizeTextBtn = findViewById(R.id.recognizeTextBtn)
        imageIv = findViewById(R.id.imageIv)
        //recognizedTextEt = findViewById(R.id.recognizedTextEt)
        valueEnergy = findViewById(R.id.editEnergy)
        valueSatuFat = findViewById(R.id.editSatuFat)
        valueSugars = findViewById(R.id.editSugars)
        valueSodium = findViewById(R.id.editSodium)
        valueFruitVeget = findViewById(R.id.editFruitVeget)
        valueFibre = findViewById(R.id.editFibre)
        valueProtein = findViewById(R.id.editProtein)

        //init arrays of permissions required for Camera, Gallery
        cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        //init setup the progress dialog, show while text from image is being recognized
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //init Text Recognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        //handle click, show input image dialog
        inputImageBtn.setOnClickListener {
            showInputImageDialog()
        }

        //handle click, start recognizing text from image we took from Camera/Gallery
        recognizeTextBtn.setOnClickListener {
            //check if image is picked or not, picked if imageUri is not null
            if(imageUri == null){
                //imageUrl is null, which means we haven't picked image yet, cannot recognize text
                showToast("Pick Image First...")
            }
            else {
                //imageUrl is not null, which means we have picked image, we can recognize text
                recognizeTextFromImage()
            }
        }

        /*
        val buttonToResult = findViewById<Button>(R.id.button_to_rating_result)
        buttonToResult.setOnClickListener {
            val intent = Intent(this, RatingResult::class.java)
            startActivity(intent)
        }
        */

        val backButton = findViewById<Button>(R.id.scannerBackButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun recognizeTextFromImage() {
        // set message and show progress dialog
        progressDialog.setMessage("Preparing Image...")
        progressDialog.show()

        try {
            //Prepare InputImage from image Uri
            val inputImage = InputImage.fromFilePath(this, imageUri!!)
            //image prepared, we are about to start text recognition process, change progress message
            progressDialog.setMessage("Recognizing text...")
            //start text recognition process from image
            val textTaskResult = textRecognizer.process(inputImage)
                .addOnSuccessListener { text ->
                    //process completed, dismiss dialog
                    progressDialog.dismiss()
                    //get the recognized text

                    val recognizedText = text.text
                    //recognizedTextEt.setText(recognizedText)

/*
                    val recognizedText = text.text
                    var i = 0
                    */

                    //textArray = arrayListOf(recognizedText)
                    //val textTest = textArray.indexOf("Per 100g")
                    //recognizedTextEt.setText(textTest)
                    //textArray.removeAt(textArray.indexOf("Per 100g"))

                    //for (line in arrayOf(text)) {
                    //    recognizedTextEt.setText(line.text)
                    //}


                    val newText = mutableListOf<String>()

                    for (line in text.textBlocks) {
                        //val word1 = line.text.contains("Sugars");
                        newText += listOf(line.text)
                        //if (word1) {
                        //    recognizedTextEt.setText(line.text)
                        //}
                    }


                    for (i in newText) {
                        if(i.contains("Energy")) {
                            val index = newText.indexOf(i)
                            positionEnergy = index.toString()
                        }
                        if(i.contains("Saturated")) {
                            val index = newText.indexOf(i)
                            positionSatuFat = index.toString()
                        }
                        if(i.contains("Sugar")) {
                            val index = newText.indexOf(i)
                            positionSugars = index.toString()
                        }
                        if(i.contains("Sodium")) {
                            val index = newText.indexOf(i)
                            positionSodium = index.toString()
                        }
                        if(i.contains("Protein")) {
                            val index = newText.indexOf(i)
                            positionProtein = index.toString()
                        }
                        if(i.contains("kcal")) {
                            val index = newText.indexOf(i)
                            positionValueEnergy = index.toString()
                        }
                    }

                    val energyPosition = positionEnergy.toInt() + positionValueEnergy.toInt() -1
                    val energyValue = newText[energyPosition].substringBefore("kcal").replace("[^\\d.]".toRegex(), "")

                    val satuFatPosition = positionSatuFat.toInt() - positionEnergy.toInt() + positionValueEnergy.toInt()
                    val satuFatValue = newText[satuFatPosition].replace("[^\\d.]".toRegex(), "")

                    val sugarsPosition = positionSugars.toInt() - positionEnergy.toInt() + positionValueEnergy.toInt()
                    val sugarsValue = newText[sugarsPosition].replace("[^\\d.]".toRegex(), "")

                    val sodiumPosition = positionSodium.toInt() - positionEnergy.toInt() + positionValueEnergy.toInt()
                    val sodiumValue = newText[sodiumPosition].replace("[^\\d.]".toRegex(), "")

                    val proteinPosition = positionProtein.toInt() - positionEnergy.toInt() + positionValueEnergy.toInt()
                    val proteinValue = newText[proteinPosition].replace("[^\\d.]".toRegex(), "")

                    valueEnergy.setText(energyValue)
                    valueSatuFat.setText(satuFatValue)
                    valueSugars.setText(sugarsValue)
                    valueSodium.setText(sodiumValue)
                    valueProtein.setText(proteinValue)


                    val radioFoodType = findViewById<RadioGroup>(R.id.foodType)
                    val selectedRadioButtonId = radioFoodType.checkedRadioButtonId


                    val ratingButton = findViewById<Button>(R.id.GradingBtn)
                    ratingButton.visibility = View.VISIBLE

                }
                .addOnFailureListener { e->
                    //failed recognizing text from image, dismiss dialog, show reason in Toast
                    progressDialog.dismiss()
                    showToast("Failed to recognize text due to ${e.message}")
                }
        }
        catch (e: Exception){
            //Exception occurred while preparing InputImage, dismiss dialog, show reason in Toast
            progressDialog.dismiss()
            showToast("Failed to prepare image due to ${e.message}")
        }
    }

    private fun showInputImageDialog() {
        //init PopupMenu param 1 is context, param 2 is UI View where you want to show PopupView
        val popupMenu = PopupMenu(this, inputImageBtn)

        //Add items Camera, Gallery to PopupMenu, param 2 is menu id, param 3 is position of this menu item in menu items list, param 4 is title of the menu
        popupMenu.menu.add(Menu.NONE, 1, 1, "CAMERA")
        popupMenu.menu.add(Menu.NONE, 2, 2, "GALLERY")

        //Show PopupMenu
        popupMenu.show()

        //handle PopupMenu item clicks
        popupMenu.setOnMenuItemClickListener {menuItem ->
            //get item id that is clicked from PopupMenu
            val id = menuItem.itemId
            if (id==1){
                //Camera is clicked, check if camera permissions are granted or not
                if (checkCameraPermission()) {
                    //camera permission granted, we can launch the camera intent
                    pickImageCamera()
                }
                else {
                    //camera permission not granted, request the camera permission
                    requestCameraPermission()
                }
            }
            else if (id==2){
                pickImageGallery()
            }

            return@setOnMenuItemClickListener true
        }
    }

    private fun pickImageGallery(){
        //intent to pick image from gallery. will show all resources from where we can pick the image
        val intent = Intent(Intent.ACTION_PICK)
        //set type of file we want to pick i.e. image
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)

    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            //here we will receive the image, if picked
            if (result.resultCode == Activity.RESULT_OK) {
                //image picked
                val data = result.data
                imageUri = data!!.data
                //set to imageView i.e. imageIv
                imageIv.setImageURI(imageUri)
            }
            else {
                //cancelled
                showToast("Cancelled...!")
            }
        }

    private fun pickImageCamera(){
        //get ready the image data to store in MediaStorage
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Sample Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description")
        //image Uri
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //intent to launch camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }


    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            //here we will receive the image, if taken from camera
            if (result.resultCode == Activity.RESULT_OK) {
                //image is taken from camera
                //we already have the image in imageUri using function pickImageCamera
                imageIv.setImageURI(imageUri)
            }
            else {
                //cancelled
                showToast("Cancelled....")
            }
        }

    private fun checkStoragePermission(): Boolean {
        /*check if storage permissions are allowed or not
        return true if allowed, false if not allowed*/
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCameraPermission(): Boolean {
        /*check if camera & storage permissions are allowed or not
        return true if allowed, false if not allowed*/
        val cameraResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        return cameraResult && storageResult
    }

    private fun requestCameraPermission(){
        //request camera permissions (for camera intent)
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //handle permission(s) results
        when(requestCode){
            CAMERA_REQUEST_CODE -> {
                //Check if some action from permission dialog performed or not Allow/Deny
                if (grantResults.isNotEmpty()){
                    //Check if Camera, Storage permissions granted contains boolean results either true or false
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    //Check if both permissions are granted or not
                    if (cameraAccepted) {
                        //both permissions (Camera & Storage) are granted, we can launch camera intent
                        pickImageCamera()
                    }
                    else {
                        //one or both permissions are denied, cannot launch camera intent
                        showToast("Camera & Storage permission are required...")
                    }
                }
            }

        }
    }

    private fun showToast(message: String) {
        Toast.makeText( this, message, Toast.LENGTH_SHORT).show()
    }
}