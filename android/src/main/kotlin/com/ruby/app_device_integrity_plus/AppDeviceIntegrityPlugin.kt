package com.ruby.app_device_integrity_plus

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** AppDeviceIntegrityPlugin */
class AppDeviceIntegrityPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {

  private lateinit var channel: MethodChannel
  private lateinit var context: Context
  private lateinit var activity: Activity

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "app_attestation")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
      if (call.method == "getAttestationServiceSupport") {

          val challenge = call.argument<String>("challengeString")!!
          val gcp = call.argument<Long>("gcp")!!

          // 여기서 challengeString을 AppDeviceIntegrity로 넘김
          val attestation = AppDeviceIntegrity(context, gcp)

          attestation.requestIntegrityToken(challenge) { resultObj ->
              resultObj.onSuccess { token ->
                  result.success(token) // 플러터로 토큰 반환
              }.onFailure { e ->
                  result.error("INTEGRITY_ERROR", e.message, null)
              }
          }
      } else {
          result.notImplemented()
      }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onDetachedFromActivity() {}

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}


  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity;
  }
  
  override fun onDetachedFromActivityForConfigChanges() {}
}