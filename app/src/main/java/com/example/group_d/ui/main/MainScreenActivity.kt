package com.example.group_d.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.group_d.COL_USER
import com.example.group_d.R
import com.example.group_d.data.model.UserDataViewModel
import com.example.group_d.databinding.ActivityMainScreenBinding
import com.example.group_d.ui.login.LoginActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class MainScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainScreenBinding
    private  val  userDataViewModel: UserDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main_screen)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_games, R.id.navigation_friends, R.id.navigation_challenges, R.id.recentGamesFragment
            )
        )
        userDataViewModel.setupFireBaseSnapshots()
        userDataViewModel.notificationHandler.createNotificationChannel(this)
        userDataViewModel.applicationContext = this.applicationContext

        // fetches a token and stores it in firestore
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            val msg = getString(R.string.default_message_thing, token)
            Log.d(TAG, msg)
            // updates the "token"-value in the firestor-database
            Firebase.firestore.collection(COL_USER).document(FirebaseAuth.getInstance().uid.toString()).update("token", token)
        })

        // create topic string from userID
        val topic: String = userDataViewModel.getOwnUserID()
        // subscribe to the topic of this user
        FirebaseMessaging.getInstance().subscribeToTopic(topic)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option_sign_out -> {
                SignOutDialogFragment(this).show(supportFragmentManager, "sign_out")
                true
            }
            android.R.id.home -> {
                // Up button was clicked
                findNavController(R.id.nav_host_fragment_activity_main_screen).navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class SignOutDialogFragment(val mainActivity: MainScreenActivity) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity.let {
                val builder = AlertDialog.Builder(it, R.style.AlertDialogTheme)
                builder.setTitle(R.string.dialog_sign_out_title)
                    .setPositiveButton(R.string.dialog_yes) { _, _ ->
                        mainActivity.signOut()
                    }
                    .setNegativeButton(R.string.dialog_no) { dialog, _ ->
                        dialog.cancel()
                    }
                builder.create()
            }
        }
    }

    private fun signOut() {
        // create topic string from userID
        val topic: String = userDataViewModel.getOwnUserID()
        // unsubscribe from user-topic
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
        // Sign out user
        Firebase.auth.signOut()
        // Go back to login screen
        setResult(Activity.RESULT_OK)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseAuth.getInstance().signOut()
    }
}