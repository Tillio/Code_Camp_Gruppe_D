package com.example.group_d.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.group_d.*
import com.example.group_d.databinding.ActivityLoginBinding
import com.example.group_d.ui.main.MainScreenActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.email
        val password = binding.password
        val login = binding.login
        val createNew = binding.createNew
        loading = binding.loading

        loginViewModel =
            ViewModelProvider(this)[LoginViewModel::class.java]

        loginViewModel.formState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.emailError != null) {
                email.error = getString(loginState.emailError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.authTask.observe(this@LoginActivity, Observer { task ->
            loading.visibility = View.INVISIBLE
            if (!task.isSuccessful) {
                showLoginFailed(R.string.login_failed)
                return@Observer
            }
            startMainActivity()
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        email.afterTextChanged {
            loginViewModel.formDataChanged(
                email.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.formDataChanged(
                    email.text.toString(),
                    password.text.toString()
                )
            }

            /*setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }*/


            login.setOnClickListener {
                loading.visibility = View.VISIBLE

                loginViewModel.login(
                    Firebase.auth,
                    email.text.toString(),
                    password.text.toString()
                )
            }

            createNew.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

        }
    }

    private fun registerUser(name: String, password: String, auth: FirebaseAuth) {
        auth.createUserWithEmailAndPassword(name, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    applicationContext,
                    "created new account",
                    Toast.LENGTH_LONG
                ).show()

                val db = Firebase.firestore
                val user = hashMapOf(
                    USER_STATUS to false,
                    USER_NAME to name,
                    USER_SEARCHING to false
                )
                db.collection(COL_USER).document(auth.currentUser?.uid.toString()).set(user)

                val userDataCollection =
                    db.collection(COL_USER).document(auth.currentUser?.uid.toString()).collection(
                        USER_DATA
                    )
                userDataCollection.document(USER_FRIENDS).set(
                    hashMapOf(
                        USER_FRIENDS to arrayListOf<String>()
                    )
                )

                userDataCollection.document(USER_CHALLENGES).set(
                    hashMapOf(
                        USER_CHALLENGES to arrayListOf<String>()
                    )
                )

                userDataCollection.document(USER_FRIEND_REQUESTS).set(
                    hashMapOf(
                        USER_FRIEND_REQUESTS to arrayListOf<String>()
                    )
                )

                userDataCollection.document(USER_GAMES).set(
                    hashMapOf(
                        USER_GAMES to arrayListOf<String>()
                    )
                )

                startMainActivity()
                finish()
            }

        }

    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun startMainActivity() {
        val i = Intent(this, MainScreenActivity::class.java).apply { }
        startActivity(i)
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}