package com.example.mapwidgetdemo.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.databinding.AlertDialogEditTextWtihButtonBinding
import com.example.mapwidgetdemo.databinding.AlertDialogWithTitleSubtitleAndTwoButtonBinding
import com.example.mapwidgetdemo.databinding.ProgressDialogBinding


/**
 * Created by Priyanka.
 */
object DialogUtils {

    var progressBar: AlertDialog? = null

    fun showProgressBar(activity: Activity) {
        val dialogBuilder = AlertDialog.Builder(activity)
        val bindingDialog: ProgressDialogBinding =
            ProgressDialogBinding.inflate(activity.layoutInflater)
        dialogBuilder.setView(bindingDialog.root)
        dialogBuilder.setCancelable(false)
        val alertDialog = dialogBuilder.create()
        progressBar = alertDialog
        val window = alertDialog.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        alertDialog.show()
    }


    fun hideProgressBar() {
        if (progressBar != null && progressBar?.isShowing!!) {
            progressBar?.dismiss()
        }
    }

    fun alertDialogSignOut(context: Activity, title: String, subTitle: String, code: Int, dialogClickInterface: DialogClickInterface) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        val bindingDialog: AlertDialogWithTitleSubtitleAndTwoButtonBinding =
            AlertDialogWithTitleSubtitleAndTwoButtonBinding.inflate(context.layoutInflater)
        dialog.setContentView(bindingDialog.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        bindingDialog.apply {
            txtTitle.text = title
            txtSubTitle.text = subTitle
            btnNo.setOnClickListener {
                dialog.dismiss()
                dialogClickInterface.onClick(code, "Cancel")
            }

            btnYes.setOnClickListener {
                dialog.dismiss()
                dialogClickInterface.onClick(code, "")
            }
        }
        dialog.show()
    }

    fun dialogChildNameOrRewardMsg(context: Activity, title: String, subTitle: String, code: Int, dialogClickInterface: DialogClickInterface) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val bindingDialog: AlertDialogEditTextWtihButtonBinding =
            AlertDialogEditTextWtihButtonBinding.inflate(context.layoutInflater)
        dialog.setContentView(bindingDialog.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        bindingDialog.apply {
            txtTitle.text = title
            txtSubTitle.text = subTitle

            btnNo.setOnClickListener {
                dialog.dismiss()
            }
            btnYes.setOnClickListener {
                if (!bindingDialog.edtName.text.isNullOrEmpty()) {
                    dialogClickInterface.onClick(code, bindingDialog.edtName.text.toString())
                } else {
                    Toast.makeText(context, "Please enter Video Name", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
       /* val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)*/
    }
}