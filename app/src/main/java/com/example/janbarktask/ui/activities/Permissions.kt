package com.example.janbarktask.ui.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.janbarktask.ui.dialogs.createPermissionsDialog

const val PERMISSION_REQUEST_CODE = 1240
const val PERMISSION_REQUEST_CODE_NOTIFICATION = 1241

open class Permissions : AppCompatActivity() {

    private var initApp: (() -> Unit)? = null
    private var declineApp: (() -> Unit)? = null
    private var forOnce = true
    var myAppPermissions: List<String>? = null


    fun checkAndRequestPermissions(
        appPermissions: List<String>, action: (() -> Unit)?, declineAction: (() -> Unit)?
    ) {
        myAppPermissions = appPermissions
        forOnce = true
        initApp = action
        declineApp = declineAction
        // check which permission are granted
        val listOfPermissionNeeded = ArrayList<String>()
        appPermissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listOfPermissionNeeded.add(permission)
            }
        }

        // Ask for the non-granted permissions
        if (listOfPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, listOfPermissionNeeded.toTypedArray(), PERMISSION_REQUEST_CODE
            )
        } else initApp?.invoke()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                val permissionResult = HashMap<String, Int>()
                var deniedCount = 0

                // gather permission grant results
                grantResults.forEachIndexed { index, grantResult ->
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        permissionResult[permissions[index]] = grantResult
                        deniedCount++
                    }
                }

                // check if all permissions are granted
                if (deniedCount == 0) initApp?.invoke()
                else {
                    permissionResult.entries.forEach {
                        val permName = it.key
                        // permission is denied (this is the first time, when "Never Ask Again" is not checked)
                        // so ask again explaining the usage of the permission
                        // showShouldRequestPermissionRationale will return true
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {
                            // show dialog of permission
                            if (!this.isDestroyed && !this.isFinishing) {
                                if (forOnce) {
                                    forOnce = false

                                    createPermissionsDialog(" Permission?",
                                        "Allow to access phone\n" + "storage to save Photos ",
                                        "Allow",
                                        "Deny",
                                        acceptAction = {
                                            myAppPermissions?.let { it1 ->
                                                checkAndRequestPermissions(
                                                    it1,
                                                    action = initApp,
                                                    declineAction = declineApp
                                                )
                                            }
                                        },
                                        declineAction = {
                                            declineApp?.invoke()
                                        }).show()
                                }
                            }
                        } else {
                            if (!this.isDestroyed && !this.isFinishing) {
                                if (forOnce) {
                                    forOnce = false
                                    createPermissionsDialog("Storage Permission?",
                                        "Allow to access phone\n" + "storage to save Photos ",
                                        "Allow",
                                        "Deny",
                                        acceptAction = {
                                            startActivity(
                                                Intent(
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", packageName, null)
                                                )
                                            )
                                        },
                                        declineAction = {
                                            declineApp?.invoke()
                                        }).show()
                                }
                            }
                        }
                    }
                }
            }

            PERMISSION_REQUEST_CODE_NOTIFICATION -> {}
        }
    }
}