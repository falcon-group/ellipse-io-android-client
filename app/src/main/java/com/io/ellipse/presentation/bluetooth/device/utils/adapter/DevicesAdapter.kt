package com.io.ellipse.presentation.bluetooth.device.utils.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.io.ellipse.R
import com.io.ellipse.common.android.list.OnItemClickListener
import com.io.ellipse.common.android.list.adapter.BaseRecyclerViewAdapter

class DevicesAdapter(
    itemClickListener: OnItemClickListener<DeviceVM>
) : BaseRecyclerViewAdapter<DeviceVM, DeviceVH>(itemClickListener) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_device, parent, false)
        return DeviceVH(view)
    }


}