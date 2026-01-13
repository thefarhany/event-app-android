package com.thefarhany.eventapp.ui.auth.register

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.thefarhany.eventapp.data.remote.RetrofitClient
import com.thefarhany.eventapp.data.repository.AuthRepository
import com.thefarhany.eventapp.databinding.ActivityRegisterBinding
import com.thefarhany.eventapp.ui.auth.viewmodel.AuthViewModel
import com.thefarhany.eventapp.ui.auth.viewmodel.AuthViewModelFactory
import com.thefarhany.eventapp.utils.Resource

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.etFullName.doOnTextChanged { text, _, _, _ ->
            validateFullName(text.toString())
        }

        binding.etEmail.doOnTextChanged { text, _, _, _ ->
            validateEmail(text.toString())
        }

        binding.etPhone.doOnTextChanged { text, _, _, _ ->
            validatePhone(text.toString())
        }

        binding.etPassword.doOnTextChanged { text, _, _, _ ->
            validatePassword(text.toString())
            if (binding.etConfirmPassword.text.toString().isNotEmpty()) {
                validateConfirmPassword(
                    binding.etPassword.text.toString(),
                    binding.etConfirmPassword.text.toString()
                )
            }
        }

        binding.etConfirmPassword.doOnTextChanged { text, _, _, _ ->
            validateConfirmPassword(
                binding.etPassword.text.toString(),
                text.toString()
            )
        }
    }

    private fun validateFullName(fullName: String): Boolean {
        return when {
            fullName.isEmpty() -> {
                binding.tilFullName.error = "This field is required"
                false
            }
            fullName.length < 3 -> {
                binding.tilFullName.error = "Name must be at least 3 characters"
                false
            }
            else -> {
                binding.tilFullName.error = null
                true
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.tilEmail.error = "This field is required"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Please enter a valid email address"
                false
            }
            !email.contains("@") -> {
                binding.tilEmail.error = "Email must contain @"
                false
            }
            else -> {
                binding.tilEmail.error = null
                true
            }
        }
    }

    private fun validatePhone(phone: String): Boolean {
        return when {
            phone.isEmpty() -> {
                binding.tilPhone.error = "This field is required"
                false
            }
            !phone.matches(Regex("^[0-9]+$")) -> {
                binding.tilPhone.error = "Phone number must contain numbers only"
                false
            }
            phone.length < 10 -> {
                binding.tilPhone.error = "Phone number must be at least 10 digits"
                false
            }
            else -> {
                binding.tilPhone.error = null
                true
            }
        }
    }

    private fun validatePassword(password: String): Boolean {
        val hasLetter = password.matches(Regex(".*[A-Za-z].*"))
        val hasNumber = password.matches(Regex(".*[0-9].*"))

        return when {
            password.isEmpty() -> {
                binding.tilPassword.error = "This field is required"
                false
            }
            password.length < 8 -> {
                binding.tilPassword.error = "Password must be at least 8 characters"
                false
            }
            !hasLetter || !hasNumber -> {
                binding.tilPassword.error = "Password must be at least 8 characters and contain both letters and numbers"
                false
            }
            else -> {
                binding.tilPassword.error = null
                true
            }
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        return when {
            confirmPassword.isEmpty() -> {
                binding.tilConfirmPassword.error = "This field is required"
                false
            }
            password != confirmPassword -> {
                binding.tilConfirmPassword.error = "Passwords do not match"
                false
            }
            else -> {
                binding.tilConfirmPassword.error = null
                true
            }
        }
    }

    private fun validateAllFields(): Boolean {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        val isFullNameValid = validateFullName(fullName)
        val isEmailValid = validateEmail(email)
        val isPhoneValid = validatePhone(phone)
        val isPasswordValid = validatePassword(password)
        val isConfirmPasswordValid = validateConfirmPassword(password, confirmPassword)

        return isFullNameValid && isEmailValid && isPhoneValid &&
                isPasswordValid && isConfirmPasswordValid
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            if (validateAllFields()) {
                val fullName = binding.etFullName.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val phone = binding.etPhone.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                val confirmPassword = binding.etConfirmPassword.text.toString().trim()

                val nameParts = fullName.split(" ", limit = 2)
                val firstName = nameParts[0]
                val lastName = if (nameParts.size > 1) nameParts[1] else ""
                val userName = fullName.replace(" ", "").lowercase()

                viewModel.register(
                    firstName = firstName,
                    lastName = lastName,
                    userName = userName,
                    email = email,
                    phoneNumber = phone,
                    password = password,
                    confirmPassword = confirmPassword
                )
            }
        }

        binding.tvSignIn.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.registerResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Registration successful! Please login.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                is Resource.Error -> {
                    showLoading(false)

                    Log.e("RegisterActivity", "Error: ${resource.message}")

                    binding.tilFullName.error = null
                    binding.tilEmail.error = null
                    binding.tilPhone.error = null
                    binding.tilPassword.error = null
                    binding.tilConfirmPassword.error = null

                    val errorMsg = resource.message ?: "Registration failed"

                    when {
                        errorMsg.contains("Email already exists", ignoreCase = true) -> {
                            binding.tilEmail.error = errorMsg
                            binding.etEmail.requestFocus()
                        }
                        errorMsg.contains("Phone number already exists", ignoreCase = true) -> {
                            binding.tilPhone.error = errorMsg
                            binding.etPhone.requestFocus()
                        }
                        errorMsg.contains("Passwords do not match", ignoreCase = true) -> {
                            binding.tilConfirmPassword.error = errorMsg
                            binding.etConfirmPassword.requestFocus()
                        }
                        errorMsg.contains("Password must be at least", ignoreCase = true) -> {
                            binding.tilPassword.error = errorMsg
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
            }
        }
    }


    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignUp.isEnabled = !isLoading

        binding.etFullName.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPhone.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.etConfirmPassword.isEnabled = !isLoading
    }
}
