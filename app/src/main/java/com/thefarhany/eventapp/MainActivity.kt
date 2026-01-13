package com.thefarhany.eventapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.thefarhany.eventapp.data.local.UserPreferences
import com.thefarhany.eventapp.ui.auth.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var userPreferences: UserPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userPreferences = UserPreferences(this)

        // Display user info
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        tvWelcome.text = "Welcome, ${userPreferences.getUserName()}!"

        btnLogout.setOnClickListener {
            userPreferences.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}