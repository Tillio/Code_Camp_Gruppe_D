package com.example.group_d.ui.login

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.group_d.R
import com.example.group_d.databinding.ActivityRegisterBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerViewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.regEmail
        val username = binding.regUsername
        val password = binding.regPassword
        val confirmPassword = binding.regConfirmPassword
        val register = binding.register
        val loading = binding.regLoading

        registerViewModel.formState.observe(this) {
            val loginState = it ?: return@observe

            // disable register button unless both username / password is valid
            register.isEnabled = loginState.isDataValid

            if (loginState.emailError != null) {
                email.error = getString(loginState.emailError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
        }

        email.afterTextChanged {
            registerViewModel.formDataChanged(
                email.text.toString(),
                password.text.toString(),
                username.text.toString()
            )
        }

        password.afterTextChanged {
            registerViewModel.formDataChanged(
                email.text.toString(),
                password.text.toString(),
                username.text.toString()
            )
        }

        username.afterTextChanged {
            registerViewModel.formDataChanged(
                email.text.toString(),
                password.text.toString(),
                username.text.toString()
            )
        }

        register.setOnClickListener {
            // Check if the entered passwords differ
            if (password.text.toString() != confirmPassword.text.toString()) {
                confirmPassword.error = getString(R.string.password_not_match)
                return@setOnClickListener
            }
            loading.visibility = View.VISIBLE
            registerViewModel.registerUser(
                Firebase.auth,
                email.text.toString(),
                password.text.toString(),
                username.text.toString()
            )
        }
    }
}