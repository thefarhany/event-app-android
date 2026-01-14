package com.thefarhany.eventapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thefarhany.eventapp.data.remote.RetrofitClient
import com.thefarhany.eventapp.ui.auth.login.LoginActivity
import com.thefarhany.eventapp.ui.home.HomeActivity
import com.thefarhany.eventapp.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.init(this)
        sessionManager = SessionManager(this)
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        val isLoggedIn = sessionManager.isLoggedIn()
        val token = sessionManager.getAuthToken()

        if (isLoggedIn && token != null) {
            navigateToHome()
        } else {
            navigateToLogin()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
