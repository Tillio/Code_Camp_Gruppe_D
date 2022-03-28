package com.example.group_d.ui.main.ingame

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.group_d.R

class LocationRequestDialogFragment(
    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>>
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val builder = AlertDialog.Builder(it, R.style.AlertDialogTheme)
            builder.setTitle(R.string.compass_location_request_dialog_title)
                .setMessage(R.string.compass_location_request_dialog_msg)
                .setPositiveButton(R.string.dialog_got_it) { _, _ ->
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    )
                }
            builder.create()
        }
    }
}

class LocationDeniedDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val builder = AlertDialog.Builder(it, R.style.AlertDialogTheme)
            builder.setTitle(R.string.compass_location_denied_dialog_title)
                .setMessage(R.string.compass_location_denied_dialog_msg)
                .setPositiveButton(R.string.dialog_got_it) { _, _ ->
                    // If location permission is denied we want to go back to last fragment because
                    // the compass game doesn't make any sense without location.
                    findNavController().popBackStack()
                }
            builder.create()
        }
    }
}