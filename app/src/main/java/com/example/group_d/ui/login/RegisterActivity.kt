package com.example.group_d.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.group_d.R
import com.example.group_d.databinding.ActivityRegisterBinding
import com.example.group_d.ui.main.MainScreenActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var register: Button
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerViewModel = ViewModelProvider(this)[RegisterViewModel::class.java]
        // Save the deep link
        registerViewModel.deepLink = intent?.data

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.regEmail
        val username = binding.regUsername
        val password = binding.regPassword
        val confirmPassword = binding.regConfirmPassword
        register = binding.register
        loading = binding.regLoading

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

        registerViewModel.authTask.observe(this) { task ->
            if (!task.isSuccessful) {
                creatingNewAccountFailed()
                return@observe
            }
            registerViewModel.setupDatabase()
        }

        registerViewModel.setupTask.observe(this) { task ->
            if (!task.isSuccessful) {
                creatingNewAccountFailed()
            }
            val intent = Intent(this, MainScreenActivity::class.java)
            // Forward the deep link
            intent.data = registerViewModel.deepLink
            startActivity(intent)
            setResult(Activity.RESULT_OK)
            finish()
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
            register.visibility = View.INVISIBLE
            loading.visibility = View.VISIBLE
            registerViewModel.registerUser(
                email.text.toString(),
                password.text.toString(),
                username.text.toString()
            )
        }
    }

    private fun creatingNewAccountFailed() {
        Toast.makeText(applicationContext, R.string.register_failed, Toast.LENGTH_SHORT).show()
        loading.visibility = View.INVISIBLE
        register.visibility = View.VISIBLE
    }
}