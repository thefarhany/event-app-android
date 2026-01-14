package com.thefarhany.eventapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.thefarhany.eventapp.R
import com.thefarhany.eventapp.data.model.request.UpdateUserRequest
import com.thefarhany.eventapp.data.model.response.UserProfile
import com.thefarhany.eventapp.data.remote.RetrofitClient
import com.thefarhany.eventapp.data.repository.UserRepository
import com.thefarhany.eventapp.databinding.FragmentProfileBinding
import com.thefarhany.eventapp.ui.auth.login.LoginActivity
import com.thefarhany.eventapp.utils.Resource
import com.thefarhany.eventapp.utils.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var sessionManager: SessionManager

    private var originalProfile: UserProfile? = null
    private var hasChanges = false

    companion object {
        private const val TAG = "ProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupViewModel()
        setupTextWatchers()
        setupClickListeners()
        observeUserProfile()
        observeUpdateResult()

        viewModel.loadUserProfile()
    }

    private fun setupViewModel() {
        val repository = UserRepository(RetrofitClient.instance)
        val factory = ProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun setupTextWatchers() {
        val textWatcher = {
            checkForChanges()
        }

        binding.apply {
            etFirstName.addTextChangedListener { textWatcher() }
            etLastName.addTextChangedListener { textWatcher() }
            etUserName.addTextChangedListener { textWatcher() }
            etEmail.addTextChangedListener { textWatcher() }
            etPhoneNumber.addTextChangedListener { textWatcher() }
        }
    }

    private fun checkForChanges() {
        originalProfile?.let { original ->
            binding.apply {
                hasChanges = etFirstName.text.toString() != original.firstName ||
                        etLastName.text.toString() != original.lastName ||
                        etUserName.text.toString() != original.userName ||
                        etEmail.text.toString() != original.email ||
                        etPhoneNumber.text.toString() != original.phoneNumber

                btnUpdateProfile.isEnabled = hasChanges
                btnUpdateProfile.alpha = if (hasChanges) 1.0f else 0.5f
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnUpdateProfile.setOnClickListener {
            if (validateInput()) {
                updateProfile()
            }
        }

        binding.btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun validateInput(): Boolean {
        binding.apply {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val userName = etUserName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phoneNumber = etPhoneNumber.text.toString().trim()

            when {
                firstName.isEmpty() -> {
                    etFirstName.error = "First name is required"
                    etFirstName.requestFocus()
                    return false
                }
                lastName.isEmpty() -> {
                    etLastName.error = "Last name is required"
                    etLastName.requestFocus()
                    return false
                }
                userName.isEmpty() -> {
                    etUserName.error = "Username is required"
                    etUserName.requestFocus()
                    return false
                }
                email.isEmpty() -> {
                    etEmail.error = "Email is required"
                    etEmail.requestFocus()
                    return false
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etEmail.error = "Invalid email format"
                    etEmail.requestFocus()
                    return false
                }
                phoneNumber.isEmpty() -> {
                    etPhoneNumber.error = "Phone number is required"
                    etPhoneNumber.requestFocus()
                    return false
                }
            }
            return true
        }
    }

    private fun updateProfile() {
        binding.apply {
            val request = UpdateUserRequest(
                firstName = etFirstName.text.toString().trim(),
                lastName = etLastName.text.toString().trim(),
                userName = etUserName.text.toString().trim(),
                email = etEmail.text.toString().trim(),
                phoneNumber = etPhoneNumber.text.toString().trim()
            )

            viewModel.updateProfile(request)
        }
    }

    private fun observeUserProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }

                is Resource.Success -> {
                    showLoading(false)
                    resource.data?.let { user ->
                        originalProfile = user
                        displayUserProfile(user)
                    }
                }

                is Resource.Error -> {
                    showLoading(false)
                    val errorMessage = resource.message ?: "Failed to load profile"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()

                    if (errorMessage.contains("Unauthorized", ignoreCase = true)) {
                        performLogout()
                    }
                }
            }
        }
    }

    private fun observeUpdateResult() {
        viewModel.updateResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnUpdateProfile.isEnabled = false
                    binding.btnUpdateProfile.text = "Updating..."
                }

                is Resource.Success -> {
                    binding.btnUpdateProfile.isEnabled = true
                    binding.btnUpdateProfile.text = "Update Profile"

                    resource.data?.let { updatedProfile ->
                        sessionManager.updateUserProfile(updatedProfile)
                        originalProfile = updatedProfile
                    }

                    Toast.makeText(
                        requireContext(),
                        "Profile updated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    hasChanges = false
                    binding.btnUpdateProfile.alpha = 0.5f

                    viewModel.clearUpdateResult()
                }

                is Resource.Error -> {
                    binding.btnUpdateProfile.isEnabled = true
                    binding.btnUpdateProfile.text = "Update Profile"

                    val errorMessage = resource.message ?: "Failed to update profile"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()

                    viewModel.clearUpdateResult()
                }

                else -> {
                }
            }
        }
    }

    private fun displayUserProfile(user: UserProfile) {
        binding.apply {
            // ✅ Display full name (firstName + lastName)
            val fullName = "${user.firstName} ${user.lastName}"
            tvUserName.text = fullName

            // Fill form fields
            etFirstName.setText(user.firstName)
            etLastName.setText(user.lastName)
            etUserName.setText(user.userName)
            etEmail.setText(user.email)
            etPhoneNumber.setText(user.phoneNumber)

            btnUpdateProfile.isEnabled = false
            btnUpdateProfile.alpha = 0.5f

            if (!user.profilePicture.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(user.profilePicture)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .centerCrop()
                    .into(ivProfilePicture)
            } else {
                ivProfilePicture.setImageResource(R.drawable.ic_person)
            }

            sessionManager.saveLoginSession(
                email = user.email,
                userName = user.userName,
                firstName = user.firstName,
                lastName = user.lastName,
                phoneNumber = user.phoneNumber,
                profilePicture = user.profilePicture
            )
        }
    }


    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.layoutProfileContent.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun performLogout() {
        sessionManager.clearSession()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
