package com.example.group_d.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

abstract class StartViewModel : ViewModel() {
    protected val auth: FirebaseAuth = Firebase.auth

    protected val _formState = MutableLiveData<FormState>()
    val formState: LiveData<FormState> = _formState

    protected val _authTask = MutableLiveData<Task<AuthResult>>()
    val authTask: LiveData<Task<AuthResult>> = _authTask

    // email validation check
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    fun formDataChanged(email: String, password: String, username: String? = null) {
        if (!isEmailValid(email)) {
            _formState.value = FormState(emailError = R.string.invalid_email)
        } else if (username != null && username.contains('@')) {
            // Ensure that the username does not contain to prevent confusing it with an email
            _formState.value = FormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _formState.value = FormState(passwordError = R.string.invalid_password)
        } else {
            _formState.value = FormState(isDataValid = true)
        }
    }
}