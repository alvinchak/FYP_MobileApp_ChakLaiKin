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

class MainActivity : AppCompatActivity() {

    fun onCheckRatingClicked(v: View) {
        val radioGroup = findViewById<RadioGroup>(R.id.foodType)
        val selectedRadioButtonId = radioGroup.checkedRadioButtonId
        val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
        val foodType = selectedRadioButton.text.toString()

        val energy = findViewById<EditText>(R.id.editEnergy).text.toString()
        val totalFats = findViewById<EditText>(R.id.editTotalfat).text.toString()
        val sugars = findViewById<EditText>(R.id.editSugars).text.toString()
        val sodium = findViewById<EditText>(R.id.editSodium).text.toString()
        val fruitVegetables = findViewById<EditText>(R.id.editFruitVegetables).text.toString()
        val fibre = findViewById<EditText>(R.id.editFibre).text.toString()
        val protein = findViewById<EditText>(R.id.editProtein).text.toString()

        val intent = Intent(this, Rating::class.java)
        intent.putExtra("FOOD_TYPE_KEY", foodType)
        intent.putExtra("ENERGY_KEY", energy)
        intent.putExtra("TOTAL_FATS_KEY", totalFats)
        intent.putExtra("SUGARS_KEY", sugars)
        intent.putExtra("SODIUM_KEY", sodium)
        intent.putExtra("FRUIT_VEGETABLES_KEY", fruitVegetables)
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
    private lateinit var positionTotalfat: String
    private lateinit var positionSugar: String
    private lateinit var positionSodium: String
    private lateinit var positionProtein: String

    private lateinit var valueEnergy: EditText
    private lateinit var valueTotalfat: EditText
    private lateinit var valueSugars: EditText
    private lateinit var valueFruitVegetables: EditText
    private lateinit var valueSodium: EditText
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
        setContentView(R.layout.activity_main)

        //init UI Views
        inputImageBtn = findViewById(R.id.inputImageBtn)
        recognizeTextBtn = findViewById(R.id.recognizeTextBtn)
        imageIv = findViewById(R.id.imageIv)
        //recognizedTextEt = findViewById(R.id.recognizedTextEt)
        valueTotalfat = findViewById(R.id.editTotalfat)
        valueSugars = findViewById(R.id.editSugars)
        valueSodium = findViewById(R.id.editSodium)

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
                     //val word1 = line.text.contains("Sugar");
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
                        if(i.contains("Total fat")) {
                            val index = newText.indexOf(i)
                            positionTotalfat = index.toString()
                        }
                        if(i.contains("Sugar")) {
                            val index = newText.indexOf(i)
                            positionSugar = index.toString()
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


                    val sugarPosition = positionTotalfat.toInt() - positionEnergy.toInt() + positionValueEnergy.toInt()
                    val sugarValue = newText[sugarPosition]

                    val totalfatPosition = positionSugar.toInt() - positionEnergy.toInt() + positionValueEnergy.toInt()
                    val totalfatValue = newText[totalfatPosition]

                    val sodiumPosition = positionSodium.toInt() - positionEnergy.toInt() + positionValueEnergy.toInt()
                    val sodiumValue = newText[sodiumPosition]

                    val energyPosition = positionEnergy.toInt() + positionValueEnergy.toInt()
                    val energyValue = newText[energyPosition]

                    val proteinPosition = positionProtein.toInt() - positionEnergy.toInt() + positionValueEnergy.toInt()
                    val proteinValue = newText[proteinPosition]


                    var output = "Total Fats: "
                        .plus(sugarValue)
                        .plus(", Sugars: ")
                        .plus(totalfatValue)
                        .plus(", Sodium: ")
                        .plus(sodiumValue)

                    //recognizedTextEt.setText(output)

                    valueEnergy.setText(energyValue)
                    valueTotalfat.setText(sugarValue)
                    valueSugars.setText(totalfatValue)
                    valueSodium.setText(sodiumValue)
                    valueProtein.setText(proteinValue)


                    val radioFoodType = findViewById<RadioGroup>(R.id.foodType)
                    val selectedRadioButtonId = radioFoodType.checkedRadioButtonId


                    val ratingButton = findViewById<Button>(R.id.GradingBtn)
                    ratingButton.visibility = View.VISIBLE

                    //val abc = newText.indexOf("Per 100g")
                    //recognizedTextEt.setText(abc)


                    /*

                           for (block in recognizedText) {
                               for (line in block.lines) {
                                   val lineText = line.text
                                   val word = lineText.contains("Sugars");
                                   if (word) {
                                       recognizedTextEt.setText(lineText)
                                   }
                               }
                           }*/

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
                //Gallery is clicked, check if storage permission is granted or not
                if (checkStoragePermission()){
                    //storage permission granted, we can launch the gallery intent
                    pickImageGallery()
                }
                else {
                    //storage permission not granted, request the storage permission
                    requestStoragePermission()
                }
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

    private fun requestStoragePermission() {
        //request storage permission (for gallery image pick)
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE)
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
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    //Check if both permissions are granted or not
                    if (cameraAccepted && storageAccepted) {
                        //both permissions (Camera & Storage) are granted, we can launch camera intent
                        pickImageCamera()
                    }
                    else {
                        //one or both permissions are denied, cannot launch camera intent
                        showToast("Camera & Storage permission are required...")
                    }
                }
            }

            STORAGE_REQUEST_CODE -> {
                //Check if some action from permission dialog performed or not Allow/Deny
                if (grantResults.isNotEmpty()){
                    //Check if Storage permissions granted, contains boolean results either true or false
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    //Check if Storage permission is granted or not
                    if (storageAccepted) {
                        //storage permission granted, we can launch gallery intent
                        pickImageGallery()
                    }
                    else {
                        //storage permission denied, cannot launch gallery intent
                        showToast("Storage permission are required...")
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText( this, message, Toast.LENGTH_SHORT).show()
    }
}