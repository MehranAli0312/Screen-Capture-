package com.example.janbarktask.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.ads.admobs.utils.setOnSingleClickListener
import com.example.ads.myExtensios.show
import com.example.janbarktask.R
import com.example.janbarktask.databinding.FragmentPermissionBinding
import com.example.janbarktask.enums.Actions
import com.example.janbarktask.helper.isAccessibilityServiceEnabled
import com.example.janbarktask.helper.toastMessageAccessibility
import com.example.janbarktask.services.MyForegroundService
import com.example.janbarktask.ui.activities.Permissions
import com.example.janbarktask.ui.dialogs.createPermissionsDialog
import com.example.janbarktask.utills.ScreenCaptureUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class PermissionFragment : Fragment() {

    private val binding by lazy {
        FragmentPermissionBinding.inflate(layoutInflater)
    }
    private lateinit var navController: NavController

    private val screenCaptureUtil by lazy {
        ScreenCaptureUtil(mContext)
    }
    private lateinit var screenCaptureLauncher: ActivityResultLauncher<Intent>


    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        initializeLaunchers()
    }

    private fun initializeLaunchers() {
        screenCaptureLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    result.apply {
                        data?.let {
                            screenCaptureUtil.setScreenCapture(resultCode, it)
                            startServiceAndNavigate(resultCode, it)
                        }
                    }
                } else {
                    callDialog()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermissions()
        binding.clickListen()
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            if (mContext.isAccessibilityServiceEnabled()) {
                permissionDescTv.text = resources.getString(R.string.screenCaptureDetail)
            } else {
                permissionDescTv.text = resources.getString(R.string.accessibilityDetail)
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (mActivity as Permissions).checkAndRequestPermissions(
                listOf(Manifest.permission.POST_NOTIFICATIONS),
                action = {
                    checkPermissionCache()
                },
                declineAction = {
                    callDialog()
                })
        } else {
            checkPermissionCache()
        }
    }

    private fun FragmentPermissionBinding.clickListen() {
        acceptBtn.setOnSingleClickListener {
            if (mContext.isAccessibilityServiceEnabled()) {
                startScreenCapture()
            } else {
                openAccessibilitySettings(mContext)
            }
        }
    }


    private fun checkPermissionCache() {
        if (mContext.isAccessibilityServiceEnabled()) {
            startScreenCapture()
        } else {
            binding.mainRoot.show()
        }
    }

    private fun startScreenCapture() {
        // Initialize the ActivityResultLauncher
        val mediaProjectionManager =
            mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        // Create the screen capture intent and launch it
        val screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(screenCaptureIntent)
    }

    private fun openAccessibilitySettings(context: Context) {
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            if (resolveActivity(context.packageManager) != null) {
                context.toastMessageAccessibility()
                context.startActivity(this)
                Handler(Looper.getMainLooper()).postDelayed({
                    context.toastMessageAccessibility()
                }, 2000)
                Handler(Looper.getMainLooper()).postDelayed({
                    context.toastMessageAccessibility()
                }, 4000)
            }
        }
    }

    private fun callDialog() {
        mActivity.createPermissionsDialog(resources.getString(R.string.screenCapture),
            resources.getString(R.string.screenCaptureDetail),
            "Allow",
            "Close",
            acceptAction = {
                checkPermissions()
            },
            declineAction = {
                mActivity.finishAffinity()
            }).show()
    }

    private fun startServiceAndNavigate(resultCode: Int, data: Intent) {
        // Start the foreground service with the result data
        val startServiceIntent =
            Intent(mContext, MyForegroundService::class.java).apply {
                action = Actions.START.name
                putExtra("resultCode", resultCode)
                putExtra("data", data)
            }
        mContext.startService(startServiceIntent)

        navController.navigate(PermissionFragmentDirections.actionPermissionFragmentToMyGalleryFragment())
    }

}