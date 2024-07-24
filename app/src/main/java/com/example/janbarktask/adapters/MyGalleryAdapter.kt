package com.example.janbarktask.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.example.ads.admobs.utils.loadAndShowNative
import com.example.ads.admobs.utils.setOnSingleClickListener
import com.example.ads.admobs.utils.showNative
import com.example.ads.databinding.MediumNativeLayoutBinding
import com.example.ads.databinding.SmallNativeLayoutBinding
import com.example.ads.myExtensios.hide
import com.example.ads.myExtensios.invisible
import com.example.ads.myExtensios.show
import com.example.janbarktask.databinding.MyGalleryItemBinding
import com.example.janbarktask.databinding.RecyclerviewItemNativeLayoutBinding
import com.example.janbarktask.helper.adAdsIntoList
import com.example.janbarktask.utills.UnifiedNativeAd

class MyGalleryAdapter(
    private val activity: Activity,
    private var dataList: List<Any> = emptyList(),
    private val callback: (uri: Uri, v: View) -> Unit,
    private val itemClick: (uri: Uri) -> Unit
) :
    RecyclerView.Adapter<ViewHolder>() {

    val AD_VIEW = 0
    val CONTENT = 1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        when (viewType) {
            AD_VIEW -> {
                TemplateAdViewHolder(
                    RecyclerviewItemNativeLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                MyWorkViewHolder(
                    MyGalleryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
        }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            AD_VIEW -> {
                with((holder as TemplateAdViewHolder).binding) {
                    activity.loadAndShowNative(
                        com.example.ads.R.layout.small_native_ad,
                        loadedAction = {
                            nativeContainer.show()
                            smallNativeLayout.adContainer.show()
                            smallNativeLayout.adContainer.removeAllViews()
                            if (it?.parent != null) {
                                (it.parent as ViewGroup).removeView(it)
                            }
                            smallNativeLayout.adContainer.addView(it)
                            smallNativeLayout.shimmerViewContainer.invisible()

                        },
                        failedAction = {
                            nativeContainer.hide()
                            smallNativeLayout.shimmerViewContainer.hide()
                            smallNativeLayout.adContainer.hide()

                        })
                }
            }

            CONTENT -> {
                with((holder as MyWorkViewHolder).binding) {
                    dataList[position].let { uri ->
                        if (uri is Uri) {
                            galleryImage.load(uri)
                            menuButton.setOnClickListener {
                                callback(uri, it)
                            }
                            root.setOnSingleClickListener {
                                itemClick(uri)
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateListWithAd(dataList: List<Any>) {
        this.dataList = adAdsIntoList(dataList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(dataList: List<Uri>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        dataList.let { it ->
            if (it.size > position && position >= 0) {
                it[position].let {
                    return when (it) {
                        is UnifiedNativeAd -> AD_VIEW
                        else -> CONTENT
                    }
                }
            }
        }
        return 0
    }

    override fun getItemCount(): Int = dataList.size

    inner class MyWorkViewHolder(val binding: MyGalleryItemBinding) :
        ViewHolder(binding.root)

    inner class TemplateAdViewHolder(val binding: RecyclerviewItemNativeLayoutBinding) :
        ViewHolder(binding.root)
}