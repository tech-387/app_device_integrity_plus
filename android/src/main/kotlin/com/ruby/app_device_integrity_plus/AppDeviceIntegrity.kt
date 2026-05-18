package com.ruby.app_device_integrity_plus

import com.google.android.gms.tasks.Task
import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest

class AppDeviceIntegrity(private val context: Context, private val cloudProjectNumber: Long) {
    private val standardIntegrityManager = IntegrityManagerFactory.createStandard(context);

    fun requestIntegrityToken(requestHash: String, callback: (Result<String>) -> Unit) {
        // 1) prepare
        standardIntegrityManager.prepareIntegrityToken(
            PrepareIntegrityTokenRequest.builder()
                .setCloudProjectNumber(cloudProjectNumber)
                .build()
        ).addOnSuccessListener { provider ->
            // 2) prepare 성공 → provider 로 request
            provider.request(
                StandardIntegrityTokenRequest.builder()
                    .setRequestHash(requestHash)
                    .build()
            ).addOnSuccessListener { token ->
                callback(Result.success(token.token()))
            }.addOnFailureListener { e ->
                callback(Result.failure(e))
            }
        }.addOnFailureListener { e ->
            callback(Result.failure(e))
        }
    }
}