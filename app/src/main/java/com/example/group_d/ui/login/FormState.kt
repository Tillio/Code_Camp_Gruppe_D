package com.example.group_d.ui.login

/**
 * Data validation state of the login and register form.
 */
data class FormState(
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val usernameError: Int? = null,
    val isDataValid: Boolean = false
)