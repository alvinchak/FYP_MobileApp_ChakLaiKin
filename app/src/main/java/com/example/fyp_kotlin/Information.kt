package com.example.fyp_kotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class Information : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)

        val button = findViewById<Button>(R.id.backButton)
        button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val arrowRight = findViewById<ImageView>(R.id.arrow_right)
        val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.horizontal_scroll_view)
        horizontalScrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollX = horizontalScrollView.scrollX
            if (scrollX > 0) {
                arrowRight.visibility = View.GONE
            } else {
                arrowRight.visibility = View.VISIBLE
            }
        }
    }
}