package com.example.group_d.ui.login

import com.google.firebase.auth.FirebaseUser

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val displayName: String,
    val firebaseUser: FirebaseUser
    //... other data fields that may be accessible to the UI
)