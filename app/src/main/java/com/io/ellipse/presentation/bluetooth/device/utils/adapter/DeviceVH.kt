package com.io.ellipse.presentation.bluetooth.device.utils.adapter

import android.view.View
import android.widget.TextView
import com.io.ellipse.R
import com.io.ellipse.common.android.list.BaseViewHolder
import com.io.ellipse.common.android.list.OnItemClickListener

class DeviceVH(view: View) : BaseViewHolder<DeviceVM>(view) {

    private val nameTextView: TextView = view.findViewById(R.id.text_title)

    override fun bindView(item: DeviceVM, itemClickListener: OnItemClickListener<DeviceVM>?) {
        super.bindView(item, itemClickListener)
        nameTextView.text = item.name
    }

    override fun unbindView() {
        super.unbindView()
    }

}