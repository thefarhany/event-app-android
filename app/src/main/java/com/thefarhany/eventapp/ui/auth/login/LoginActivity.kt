package com.thefarhany.eventapp.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.thefarhany.eventapp.data.remote.RetrofitClient
import com.thefarhany.eventapp.data.repository.AuthRepository
import com.thefarhany.eventapp.databinding.ActivityLoginBinding
import com.thefarhany.eventapp.ui.auth.forgot_password.ForgotPasswordActivity
import com.thefarhany.eventapp.ui.auth.register.RegisterActivity
import com.thefarhany.eventapp.ui.auth.viewmodel.AuthViewModel
import com.thefarhany.eventapp.ui.auth.viewmodel.AuthViewModelFactory
import com.thefarhany.eventapp.ui.home.HomeActivity
import com.thefarhany.eventapp.utils.Resource
import com.thefarhany.eventapp.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        RetrofitClient.init(this)

        setupViewModel()
        setupLiveValidation()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViewModel() {
        val repository = AuthRepository(RetrofitClient.instance)
        val factory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }

    private fun setupLiveValidation() {
        binding.etEmail.doOnTextChanged { text, _, _, _ ->
            validateEmail(text.toString())
        }

        binding.etPassword.doOnTextChanged { text, _, _, _ ->
            validatePassword(text.toString())
        }
    }

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.tilEmail.error = "Please fill out this field"
                false
            }
            !email.contains("@") -> {
                binding.tilEmail.error = "Email must contain @"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Please enter a valid email address"
                false
            }
            else -> {
                binding.tilEmail.error = null
                true
            }
        }
    }

    private fun validatePassword(password: String): Boolean {
        val hasLetter = password.matches(Regex(".*[A-Za-z].*"))
        val hasNumber = password.matches(Regex(".*[0-9].*"))

        return when {
            password.isEmpty() -> {
                binding.tilPassword.error = "Please fill out this field"
                false
            }
            password.length < 8 -> {
                binding.tilPassword.error = "Password must be at least 8 characters"
                false
            }
            !hasLetter || !hasNumber -> {
                binding.tilPassword.error = "Password must contain both letters and numbers"
                false
            }
            else -> {
                binding.tilPassword.error = null
                true
            }
        }
    }

    private fun validateAllFields(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        val isEmailValid = validateEmail(email)
        val isPasswordValid = validatePassword(password)

        return isEmailValid && isPasswordValid
    }

    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener {
            if (validateAllFields()) {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                viewModel.login(email, password)
            }
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }

                is Resource.Success -> {
                    val token = resource.data

                    if (token != null) {
                        sessionManager.saveAuthToken(token)

                        Log.d(TAG, "✅ Token saved: ${token.take(20)}...")

                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                        navigateToHome()
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }

                is Resource.Error -> {
                    showLoading(false)
                    Log.e(TAG, "Login error: ${resource.message}")
                    handleLoginError(resource.message)
                }
            }
        }
    }

    private fun handleLoginError(errorMessage: String?) {
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        val errorMsg = errorMessage ?: "Login failed"

        when {
            errorMsg.contains("Email not registered", ignoreCase = true) ||
                    errorMsg.contains("Email is not registered", ignoreCase = true) -> {
                binding.tilEmail.error = "Email is not registered"
                binding.etEmail.requestFocus()
            }
            errorMsg.contains("Incorrect password", ignoreCase = true) -> {
                binding.tilPassword.error = "Incorrect password"
                binding.etPassword.requestFocus()
            }
            errorMsg.contains("Connection failed", ignoreCase = true) -> {
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    errorMsg,
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).show()
            }
            else -> {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignIn.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }
}
