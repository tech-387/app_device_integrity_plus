package com.ruby.app_device_integrity_plus

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityException
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.google.android.play.core.integrity.model.StandardIntegrityErrorCode

class AppDeviceIntegrity(context: Context, val cloudProjectNumber: Long) {
    private val standardIntegrityManager = IntegrityManagerFactory.createStandard(context)

    // Prepared once and reused: Play throttles frequent prepareIntegrityToken calls (error -8).
    private var providerTask: Task<StandardIntegrityTokenProvider>? = null

    fun requestIntegrityToken(requestHash: String, callback: (Result<String>) -> Unit) {
        requestIntegrityToken(requestHash, canReprepare = true, callback)
    }

    private fun requestIntegrityToken(
        requestHash: String,
        canReprepare: Boolean,
        callback: (Result<String>) -> Unit,
    ) {
        val task = getProviderTask()
        task.addOnSuccessListener { provider ->
            provider.request(
                StandardIntegrityTokenRequest.builder()
                    .setRequestHash(requestHash)
                    .build()
            ).addOnSuccessListener { token ->
                callback(Result.success(token.token()))
            }.addOnFailureListener { e ->
                val isProviderInvalid = e is StandardIntegrityException &&
                    e.errorCode == StandardIntegrityErrorCode.INTEGRITY_TOKEN_PROVIDER_INVALID
                if (!canReprepare || !isProviderInvalid) {
                    callback(Result.failure(e))
                    return@addOnFailureListener
                }
                clearProviderTask(task)
                requestIntegrityToken(requestHash, canReprepare = false, callback)
            }
        }.addOnFailureListener { e ->
            clearProviderTask(task)
            callback(Result.failure(e))
        }
    }

    private fun getProviderTask(): Task<StandardIntegrityTokenProvider> =
        providerTask ?: standardIntegrityManager.prepareIntegrityToken(
            PrepareIntegrityTokenRequest.builder()
                .setCloudProjectNumber(cloudProjectNumber)
                .build()
        ).also { providerTask = it }

    // Only clears the given task, so a newer prepare started meanwhile is kept.
    private fun clearProviderTask(task: Task<StandardIntegrityTokenProvider>) {
        if (providerTask === task) providerTask = null
    }
}
