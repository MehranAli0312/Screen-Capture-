package com.example.janbarktask.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.example.janbarktask.databinding.DialogPermissionsLayoutBinding
import com.example.janbarktask.helper.setSingleClickListener

fun Activity.createPermissionsDialog(
    permTitle: String,
    permDesc: String,
    acceptBtnText: String,
    declineBtnText: String,
    acceptAction: () -> Unit,
    declineAction: () -> Unit,
    permImage: Int? = null
) = Dialog(this).apply {
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    setCancelable(false)
    with(DialogPermissionsLayoutBinding.inflate(LayoutInflater.from(this@createPermissionsDialog))) {
        setContentView(root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.70).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        permissionTitle.text = permTitle
        permissionDescTv.text = permDesc
        acceptBtn.text = acceptBtnText
        declineBtn.text = declineBtnText
        permImage?.let {
            permissionIllustrationIv.visibility = View.VISIBLE
            permissionIllustrationIv.setImageResource(it)
        }
        acceptBtn.setSingleClickListener {
            dismiss()
            acceptAction.invoke()
        }
        declineBtn.setSingleClickListener {
            dismiss()
            declineAction.invoke()
        }
    }
}