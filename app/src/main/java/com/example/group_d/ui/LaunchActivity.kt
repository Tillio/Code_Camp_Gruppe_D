package com.example.group_d.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.group_d.R
import com.example.group_d.ui.login.LoginActivity
import com.example.group_d.ui.main.MainScreenActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
    }

    override fun onStart() {
        super.onStart()
        val targetActivity = if (Firebase.auth.currentUser != null) {
            // There is already a user signed in -> go to main screen
            MainScreenActivity::class.java
        } else {
            // There is no user signed in -> go to login
            LoginActivity::class.java
        }
        // Go to target
        val intent = Intent(this, targetActivity)
        startActivity(intent)
        setResult(Activity.RESULT_OK)
        finish()
    }
}