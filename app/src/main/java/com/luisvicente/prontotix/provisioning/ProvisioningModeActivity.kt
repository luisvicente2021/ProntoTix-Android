package com.luisvicente.prontotix.provisioning

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle

class ProvisioningModeActivity : Activity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        when (intent.action) {

            DevicePolicyManager.ACTION_GET_PROVISIONING_MODE -> {

                val resultIntent =
                    Intent().apply {

                        putExtra(
                            DevicePolicyManager.EXTRA_PROVISIONING_MODE,
                            DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE
                        )

                        putExtra(
                            DevicePolicyManager.EXTRA_PROVISIONING_SKIP_EDUCATION_SCREENS,
                            true
                        )
                    }

                setResult(
                    RESULT_OK,
                    resultIntent
                )

                finish()
            }

            DevicePolicyManager.ACTION_ADMIN_POLICY_COMPLIANCE -> {

                setResult(
                    RESULT_OK
                )

                finish()
            }

            else -> {

                setResult(
                    RESULT_CANCELED
                )

                finish()
            }
        }
    }
}
