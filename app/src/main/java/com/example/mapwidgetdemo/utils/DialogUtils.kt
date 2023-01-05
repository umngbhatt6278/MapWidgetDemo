package com.example.mapwidgetdemo.utils

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.LinearLayout
import com.example.mapwidgetdemo.databinding.ProgressDialogBinding


/**
 * Created by Priyanka.
 */
object DialogUtils {

    var progressBar: AlertDialog? = null

    fun showProgressBar(activity: Activity) {
        val dialogBuilder = AlertDialog.Builder(activity)
        val bindingDialog: ProgressDialogBinding = ProgressDialogBinding.inflate(activity.layoutInflater)
        dialogBuilder.setView(bindingDialog.root)
        dialogBuilder.setCancelable(false)
        val alertDialog = dialogBuilder.create()
        progressBar = alertDialog
        val window = alertDialog.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        alertDialog.show()
    }


    fun hideProgressBar() {
        if (progressBar != null && progressBar?.isShowing!!) {
            progressBar?.dismiss()
        }
    }
}