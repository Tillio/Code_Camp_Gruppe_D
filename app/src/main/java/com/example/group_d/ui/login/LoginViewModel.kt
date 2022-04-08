package com.example.group_d.ui.login

import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : StartViewModel() {

    fun login(auth: FirebaseAuth, email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            _authTask.value = task
        }
    }
}