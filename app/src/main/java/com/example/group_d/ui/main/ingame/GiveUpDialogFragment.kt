package com.example.group_d.ui.main.ingame

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.group_d.R

class GiveUpDialogFragment(
    private val receiver: GiveUpReceiver
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val builder = AlertDialog.Builder(it, R.style.AlertDialogTheme)
            builder.setTitle(R.string.dialog_give_up_msg)
                .setPositiveButton(R.string.dialog_yes) { _, _ ->
                    receiver.onGiveUp()
                }
                .setNegativeButton(R.string.dialog_no) { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        }
    }
}