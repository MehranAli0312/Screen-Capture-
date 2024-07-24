package com.example.janbarktask.ui.fragments

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ads.admobs.utils.setOnSingleClickListener
import com.example.ads.admobs.utils.showInterstitial
import com.example.ads.myExtensios.hide
import com.example.ads.myExtensios.show
import com.example.ads.myExtensios.showToast
import com.example.janbarktask.R
import com.example.janbarktask.adapters.MyGalleryAdapter
import com.example.janbarktask.databinding.FragmentMyGalleryBinding
import com.example.janbarktask.helper.shareImage
import com.example.janbarktask.ui.activities.Permissions
import com.example.janbarktask.utills.AppUtil
import com.example.janbarktask.viewModels.GalleryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.File


class MyGalleryFragment : Fragment() {

    private val binding by lazy {
        FragmentMyGalleryBinding.inflate(layoutInflater)
    }

    private val viewModel: GalleryViewModel by inject()


    private lateinit var navController: NavController

    private var deletedImageUri: Uri? = null


    private var myGalleryAdapter: MyGalleryAdapter? = null

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissions()
        binding.initRecyclerViews()
        binding.initObservers()
        binding.swipeRefreshListener()
        binding.clickListener()
    }

    private fun checkPermissions() {
        (mActivity as Permissions).checkAndRequestPermissions(
            AppUtil.getPermissions(),
            action = {
                binding.allowPermissionView.hide()
                binding.swipeRefreshLayout.show()
                binding.emptyDataPlaceHolder.show()
                viewModel.getImages()
            },
            declineAction = {
                binding.allowPermissionView.show()
                binding.emptyDataPlaceHolder.hide()
                binding.swipeRefreshLayout.hide()
            })
    }

    private fun FragmentMyGalleryBinding.clickListener() {
        allowButton.setOnSingleClickListener {
            checkPermissions()
            binding.allowPermissionView.hide()
            binding.swipeRefreshLayout.show()
            binding.emptyDataPlaceHolder.show()
        }
    }

    private fun FragmentMyGalleryBinding.initRecyclerViews() {
        myGalleryAdapter = MyGalleryAdapter(mActivity, emptyList(), { uri, v ->
            showPopup(uri, v)
        }, {
            mActivity.showInterstitial({
                navController.navigate(
                    MyGalleryFragmentDirections.actionMyGalleryFragmentToImagePreviewFragment(
                        it
                    )
                )
            }, {
                navController.navigate(
                    MyGalleryFragmentDirections.actionMyGalleryFragmentToImagePreviewFragment(
                        it
                    )
                )
            })

        })
        myGalleryAdapter?.let {
            val mLayoutManager = GridLayoutManager(mContext, 3)
            mLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (it.getItemViewType(position)) {
                        it.AD_VIEW -> 3
                        else -> 1
                    }
                }
            }
            myRecyclerView.layoutManager = mLayoutManager
            myRecyclerView.adapter = it
        }
    }

    private fun FragmentMyGalleryBinding.swipeRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            viewModel.getImages()
        }
    }

    private fun FragmentMyGalleryBinding.initObservers() {
        viewModel.imageList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                emptyDataPlaceHolder.hide()
                myRecyclerView.show()
                if (it.size > 5) {
                    myGalleryAdapter?.updateListWithAd(it)
                } else myGalleryAdapter?.updateList(it)
            } else {
                emptyDataPlaceHolder.show()
                myRecyclerView.hide()
            }
        }

        viewModel.savedImageUriLatest.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.getImages()
            }
        }
    }

    private fun showPopup(uri: Uri, view: View) {
        val popup = PopupMenu(mContext, view)
        popup.inflate(R.menu.image_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item?.itemId) {
                R.id.open -> {
                    navController.navigate(
                        MyGalleryFragmentDirections.actionMyGalleryFragmentToImagePreviewFragment(
                            uri
                        )
                    )
                }

                R.id.share -> {
                    mActivity.shareImage(uri)
                }

                R.id.delete -> {
                    deletedImageUri = uri
                    lifecycleScope.launch {
                        deleteUriFromExternalStorage(uri)
                    }
                }
            }

            true
        }

        popup.show()
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
                viewModel.getImages()
                mContext.showToast("Photo deleted successfully")
            } else {
                mContext.showToast("Photo couldn't be deleted")
            }
        }

    private suspend fun deleteUriFromExternalStorage(fileUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                mActivity.contentResolver.delete(fileUri, null, null)
                viewModel.getImages()
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

    private fun deleteFile(path: String?) {
        val file = path?.let { File(it) }
        if (file?.exists() == true) {
            if (file.delete()) {
                MediaScannerConnection.scanFile(
                    context, arrayOf(path), null
                ) { _, _ -> viewModel.getImages() }
                mContext.showToast("Photo deleted successfully")
            } else {
                mContext.showToast("Photo couldn't be deleted")
            }
        }
    }

}


