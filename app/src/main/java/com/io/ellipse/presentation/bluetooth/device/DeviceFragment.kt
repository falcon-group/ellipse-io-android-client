package com.io.ellipse.presentation.bluetooth.device

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.io.ellipse.R
import com.io.ellipse.common.android.list.OnItemClickListener
import com.io.ellipse.common.android.list.decorations.GridItemMarginDecoration
import com.io.ellipse.data.bluetooth.state.requestBluetooth
import com.io.ellipse.presentation.base.BaseFragment
import com.io.ellipse.presentation.bluetooth.device.action.*
import com.io.ellipse.presentation.bluetooth.device.utils.adapter.DeviceVM
import com.io.ellipse.presentation.bluetooth.device.utils.adapter.DevicesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_devices.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeviceFragment : BaseFragment<DeviceViewModel>(), OnItemClickListener<DeviceVM> {

    override val viewModel: DeviceViewModel by viewModels()

    override val layoutResId: Int = R.layout.fragment_devices

    private var scannerLiveData: LiveData<DeviceVM>? = null
    private val adapter = DevicesAdapter(this)

    private lateinit var requestMultiplePermissions: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestMultiplePermissions = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            viewModel
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val margin = requireContext().resources.getDimensionPixelSize(R.dimen.margin_small)
        val dividerDecoration = DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.VERTICAL
        )
        devicesRecyclerView.adapter = adapter
        devicesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        devicesRecyclerView.addItemDecoration(GridItemMarginDecoration(margin, margin, 1))
        devicesRecyclerView.addItemDecoration(dividerDecoration)
        viewModel.userActionState
            .asLiveData(Dispatchers.IO)
            .observe(viewLifecycleOwner, Observer(::observeBluetoothAction))
    }

    override fun onResume() {
        super.onResume()
        scannerLiveData = viewModel.startScan().also {
            it.observe(viewLifecycleOwner, Observer(::upsertDevice))
        }
    }

    override fun onPause() {
        scannerLiveData?.removeObservers(viewLifecycleOwner)
        super.onPause()
    }

    override fun onDestroy() {
        requestMultiplePermissions.unregister()
        super.onDestroy()
    }

    override fun onItemClick(item: DeviceVM, position: Int) {
        viewModel.viewModelScope.launch(Dispatchers.IO) { viewModel.connect(item) }
    }

    private fun observeBluetoothAction(action: UserAction) {
        when (action) {
            is DialogDescriptionAction -> showDescriptionDialog()
            is DialogPermissionsAction -> showPermissionDialogs()
            is DialogBluetoothAction -> showBluetoothDialog()
            is DialogLocationAction -> showLocationEnablingScreen()
        }
    }

    private fun showDescriptionDialog() {
        MaterialDialog(requireActivity()).show {
            title(res = R.string.title_dialog_enable_bluetooth)
            message(res = R.string.content_dialog_enable_bluetooth)
            cancelable(false)
            cancelOnTouchOutside(false)
            positiveButton(res = android.R.string.yes) {
                viewModel.showPermissionsDialog()
                it.dismiss()
            }
            negativeButton(res = android.R.string.no) {
                it.dismiss()
            }
        }
    }

    private fun showPermissionDialogs() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH
        )
        requestMultiplePermissions.launch(permissions)
    }

    private fun showBluetoothDialog() = requestBluetooth()

    private fun showLocationEnablingScreen() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    private fun upsertDevice(deviceVM: DeviceVM) {
        val index = adapter.items.indexOfFirst { it.address == deviceVM.address }
        if (index == -1) {
            adapter.addItems(items = *arrayOf(deviceVM))
        } else {
            adapter.changeItem(index, deviceVM)
        }
    }
}