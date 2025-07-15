package com.example.task_tacker

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class Screen04 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen04)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get reference to the create account button
        val createAccountButton = findViewById<MaterialButton>(R.id.createAccountButton)

        // Set click listener
        createAccountButton.setOnClickListener {
            // Create an Intent to start Screen05
            val intent = Intent(this, Screen05::class.java)
            startActivity(intent)

            // Optional: add animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}