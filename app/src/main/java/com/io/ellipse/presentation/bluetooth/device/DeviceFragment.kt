package com.io.ellipse.presentation.bluetooth.device

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.io.ellipse.R
import com.io.ellipse.common.android.list.OnItemClickListener
import com.io.ellipse.common.android.list.decorations.GridItemMarginDecoration
import com.io.ellipse.data.bluetooth.connection.ConnectionState
import com.io.ellipse.data.bluetooth.state.requestBluetooth
import com.io.ellipse.presentation.base.BaseFragment
import com.io.ellipse.presentation.bluetooth.device.action.DialogBluetoothAction
import com.io.ellipse.presentation.bluetooth.device.action.DialogDescriptionAction
import com.io.ellipse.presentation.bluetooth.device.action.DialogPermissionsAction
import com.io.ellipse.presentation.bluetooth.device.action.UserAction
import com.io.ellipse.presentation.bluetooth.device.utils.ActionUpsertItem
import com.io.ellipse.presentation.bluetooth.device.utils.ListAction
import com.io.ellipse.presentation.bluetooth.device.utils.adapter.DeviceVM
import com.io.ellipse.presentation.bluetooth.device.utils.adapter.DevicesAdapter
import com.io.ellipse.presentation.bluetooth.device.utils.checkBluetoothPermissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_devices.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class DeviceFragment : BaseFragment<DeviceViewModel>(), OnItemClickListener<DeviceVM> {

    override val viewModel: DeviceViewModel by viewModels()

    override val layoutResId: Int = R.layout.fragment_devices

    private val adapter = DevicesAdapter(this)
    private lateinit var requestMultiplePermissions: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestMultiplePermissions = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            viewModel
        )
        var isFlowEnabled = checkBluetoothPermissions()
        if (!isFlowEnabled) {
            viewModel.showDescriptionDialog()
            return
        }
        isFlowEnabled = viewModel.isBluetoothEnabled
        if (!isFlowEnabled) {
            viewModel.showBluetoothDialog()
            return
        }
        viewModel.startScan()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val margin = requireContext().resources.getDimensionPixelSize(R.dimen.margin_small)
        devicesRecyclerView.adapter = adapter
        devicesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        devicesRecyclerView.addItemDecoration(GridItemMarginDecoration(margin, margin, 1))
        viewModel.connectionState
            .asLiveData(Dispatchers.IO)
            .observe(viewLifecycleOwner, Observer(::observeConnectionState))
        viewModel.deviceAction
            .asLiveData(Dispatchers.IO)
            .observe(viewLifecycleOwner, Observer(::observeDeviceState))
        viewModel.userActionState
            .asLiveData(Dispatchers.IO)
            .observe(viewLifecycleOwner, Observer(::observeBluetoothAction))
        viewModel.bluetoothState
            .asLiveData(Dispatchers.IO)
            .observe(viewLifecycleOwner, Observer(::observeBluetoothState))
    }

    override fun onDestroy() {
        requestMultiplePermissions.unregister()
        super.onDestroy()
    }

    override fun onItemClick(item: DeviceVM, position: Int) {
        viewModel.viewModelScope.launch(Dispatchers.IO) { viewModel.connect(item) }
    }

    private fun observeConnectionState(state: ConnectionState) {
        Timber.e(state.toString())
    }

    private fun observeDeviceState(state: ListAction) {
        when (state) {
            is ActionUpsertItem -> upsertDevice(state.device)
        }
    }

    private fun observeBluetoothState(state: Boolean) {
        if (state) {
            viewModel.startScan()
        } else if (checkBluetoothPermissions()) {
            viewModel.stopScan()
        }
    }

    private fun observeBluetoothAction(action: UserAction) {
        when (action) {
            is DialogDescriptionAction -> showDescriptionDialog()
            is DialogPermissionsAction -> showPermissionDialogs()
            is DialogBluetoothAction -> showBluetoothDialog()
        }
    }

    private fun showDescriptionDialog() {
        MaterialDialog(requireActivity()).show {
            title(text = "Enable bluetooth")
            message(text = "Enable bluetooth")
            cancelable(false)
            cancelOnTouchOutside(false)
            positiveButton(text = "Yes") {
                viewModel.showPermissionsDialog()
                it.dismiss()
            }
            negativeButton(text = "No") {
                it.dismiss()
            }
        }
    }

    private fun showPermissionDialogs() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
        requestMultiplePermissions.launch(permissions)
    }

    private fun showBluetoothDialog() = requestBluetooth()

    private fun upsertDevice(deviceVM: DeviceVM) {
        val index = adapter.items.indexOfFirst { it.address == deviceVM.address }
        if (index == -1) {
            adapter.addItems(items = *arrayOf(deviceVM))
        } else {
            adapter.changeItem(index, deviceVM)
        }
    }
}