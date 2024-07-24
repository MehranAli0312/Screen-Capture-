package com.example.janbarktask.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.ads.admobs.utils.setOnSingleClickListener
import com.example.ads.myExtensios.showToast
import com.example.janbarktask.databinding.FragmentImagePreviewBinding
import com.example.janbarktask.helper.setSingleClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ImagePreviewFragment : Fragment() {

    private val binding by lazy { FragmentImagePreviewBinding.inflate(layoutInflater) }

    private lateinit var navController: NavController

    private var deletedImageUri: Uri? = null

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.getArgData()
        binding.clickListen()
    }

    @SuppressLint("SetTextI18n")
    private fun FragmentImagePreviewBinding.getArgData() {
        // Safely retrieve the arguments and cast them
        val args = arguments?.let {
            ImagePreviewFragmentArgs.fromBundle(it)
        }
        args?.imagePath?.let { uri ->
            deletedImageUri = uri
            userImage.load(uri)
        }
    }

    private fun FragmentImagePreviewBinding.clickListen() {
        homeUpIv.setSingleClickListener {
            navController.navigateUp()
        }
        delete.setOnSingleClickListener {
            lifecycleScope.launch {
                deletedImageUri?.let { deleteUriFromExternalStorage(it) }
            }
        }
    }


    //    delete image
    private var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    lifecycleScope.launch {
                        deleteUriFromExternalStorage(deletedImageUri ?: return@launch)
                    }
                }
                backToScreen()
                mContext.showToast("Photo deleted successfully")
            } else {
                mContext.showToast("Photo couldn't be deleted")
            }
        }

    private suspend fun deleteUriFromExternalStorage(fileUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                mActivity.contentResolver.delete(fileUri, null, null)
                backToScreen()
            } catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(
                            mActivity.contentResolver, listOf(fileUri)
                        ).intentSender
                    }

                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }

                    else -> {
                        deleteFile(fileUri.path)
                        null
                    }
                }
                intentSender?.let { sender ->
                    intentSenderLauncher.launch(
                        IntentSenderRequest.Builder(sender).build()
                    )
                }
            }
        }
    }

    private fun backToScreen() {
        mActivity.runOnUiThread {
            navController.navigateUp()
        }
    }

    private fun deleteFile(path: String?) {
        val file = path?.let { File(it) }
        if (file?.exists() == true) {
            if (file.delete()) {
                MediaScannerConnection.scanFile(
                    context, arrayOf(path), null
                ) { _, _ ->
                    backToScreen()
                }
                mContext.showToast("Photo deleted successfully")
            } else {
                mContext.showToast("Photo couldn't be deleted")
            }
        }
    }
}