package io.github.mnizarzr.whatsapp_sticker

import android.app.Activity
import android.content.Context
import android.util.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class MethodCallHandlerImpl(private val context: Context, private val activity: Activity) : MethodChannel.MethodCallHandler {

    companion object {
        private val TAG = MethodCallHandlerImpl::class.java.simpleName
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "addToWhatsapp" -> {
                Log.i(TAG, "method addToWhatsapp() called")
                val directory: String? = call.argument("directory")
                val stickerAdder = StickerAdder(context, activity)
                val addResult = stickerAdder.execute(directory).get()
                if(addResult!!) result.success(true)
                else result.error("100", "TIDAK TAU ERRORNYA","KANDANI ORA EROH KOK")
            }
            else -> result.notImplemented()
        }
    }

}