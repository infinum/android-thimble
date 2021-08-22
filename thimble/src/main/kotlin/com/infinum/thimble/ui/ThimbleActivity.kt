package com.infinum.thimble.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RestrictTo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.infinum.thimble.R
import com.infinum.thimble.databinding.ThimbleActivityThimbleBinding
import com.infinum.thimble.models.configuration.ThimbleConfiguration
import com.infinum.thimble.ui.contracts.OverlayPermissionContract
import com.infinum.thimble.ui.fragments.GridOverlayFragment
import com.infinum.thimble.ui.fragments.MagnifierOverlayFragment
import com.infinum.thimble.ui.fragments.MockupOverlayFragment
import com.infinum.thimble.ui.fragments.RecorderOverlayFragment
import com.infinum.thimble.ui.shared.viewBinding
import timber.log.Timber

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ThimbleActivity : ServiceActivity() {

    private val binding by viewBinding(ThimbleActivityThimbleBinding::inflate)

    private var contract: ActivityResultLauncher<Unit>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            contract = registerForActivityResult(OverlayPermissionContract(this)) { canDrawOverlays ->
                when (canDrawOverlays) {
                    false -> showPermission()
                    true -> Unit
                }
            }
        }

        setupToolbar()
        setupUi(ThimbleConfiguration())
        checkPermission()
    }

    override fun onDestroy() {
        contract?.unregister()
        super.onDestroy()
    }

    override fun setupUi(configuration: ThimbleConfiguration) {
        (binding.toolbar.menu.findItem(R.id.status).actionView as? SwitchMaterial)?.let {
            it.setOnCheckedChangeListener(null)
            it.isChecked = configuration.enabled
            it.setOnCheckedChangeListener { _, isChecked ->
                when (isChecked) {
                    true -> createService()
                    false -> destroyService()
                }
            }
        }
        with(supportFragmentManager) {
            (findFragmentById(R.id.gridOverlayFragment) as? GridOverlayFragment)?.let {
                it.toggleUi(configuration.enabled)
                it.configure(configuration.grid)
            }
            (findFragmentById(R.id.mockupOverlayFragment) as? MockupOverlayFragment)?.let {
                it.toggleUi(configuration.enabled)
                it.configure(configuration.mockup)
            }
            (findFragmentById(R.id.magnifierOverlayFragment) as? MagnifierOverlayFragment)?.let {
                it.toggleUi(configuration.enabled)
                it.configure(configuration.magnifier)
            }
            (findFragmentById(R.id.recorderOverlayFragment) as? RecorderOverlayFragment)?.let {
                it.toggleUi(configuration.enabled)
                it.configure(configuration.recorder)
            }
        }
    }

    private fun setupToolbar() {
        with(binding) {
            toolbar.setNavigationOnClickListener { finish() }
        }
    }

    private fun checkPermission() {
        if (!OverlayPermissionContract.canDrawOverlays(this)) {
            showPermission()
        }
    }

    private fun showPermission() =
        MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setTitle(R.string.thimble_name)
            .setMessage(R.string.thimble_message_permission_error)
            .setPositiveButton(R.string.thimble_allow) { dialog, _ ->
                dialog.dismiss()
                requestPermission()
            }
            .setNegativeButton(R.string.thimble_deny) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .create()
            .show()

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            contract?.launch(Unit)
        }
    }
}
