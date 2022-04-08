package com.example.group_d.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.group_d.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterViewModel : StartViewModel() {
    private val _setupTask = MutableLiveData<Task<List<Void>>>()
    val setupTask: LiveData<Task<List<Void>>> = _setupTask

    private lateinit var username: String

    fun registerUser(email: String, password: String, username: String) {
        // Save username for later usage, if it's blank use the email instead
        this.username = username.ifBlank { email }
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            _authTask.value = task
        }
    }

    fun setupDatabase() {
        val db = Firebase.firestore
        val currentUser = auth.currentUser!!
        val user = hashMapOf(
            USER_STATUS to false,
            USER_NAME to currentUser.email,
            USER_DISPLAY_NAME to username,
            USER_SEARCHING to false
        )

        val profileChangeReq = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()
        val userDataCollection =
            db.collection(COL_USER).document(currentUser.uid).collection(
                USER_DATA
            )

        val tasks = listOf(
            currentUser.updateProfile(profileChangeReq),

            db.collection(COL_USER).document(currentUser.uid).set(user),

            userDataCollection.document(USER_FRIENDS).set(
                hashMapOf(
                    USER_FRIENDS to arrayListOf<String>()
                )
            ),

            userDataCollection.document(USER_CHALLENGES).set(
                hashMapOf(
                    USER_CHALLENGES to arrayListOf<String>()
                )
            ),

            userDataCollection.document(USER_FRIEND_REQUESTS).set(
                hashMapOf(
                    USER_FRIEND_REQUESTS to arrayListOf<String>()
                )
            ),

            userDataCollection.document(USER_GAMES).set(
                hashMapOf(
                    USER_GAMES to arrayListOf<String>()
                )
            )
        )

        // Wait for all tasks to complete before going to main activity
        Tasks.whenAllSuccess<Void>(tasks).addOnCompleteListener { task ->
            _setupTask.value = task
        }
    }
}