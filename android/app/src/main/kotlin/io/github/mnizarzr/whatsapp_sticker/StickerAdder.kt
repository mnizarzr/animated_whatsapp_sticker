package io.github.mnizarzr.whatsapp_sticker

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import io.github.mnizarzr.whatsapp_sticker.model.Sticker
import io.github.mnizarzr.whatsapp_sticker.model.StickerPack
import io.github.mnizarzr.whatsapp_sticker.util.StickerJsonWriter
import java.io.*
import java.nio.ByteBuffer
import java.util.*

class StickerAdder(private val context: Context, private val activity: Activity) : AsyncTask<String, Void, Boolean?>()   {

    override fun doInBackground(vararg params: String?): Boolean? {

        val filesDir = context.filesDir.path
        val stickersDir = filesDir + File.separator + StickerContentProvider.STICKER_PACKS_DIRECTORY
        val directory = File(params[0] + File.separator)
        val newDirectoryName = generateName()
        val newDirectory = File(stickersDir + File.separator + newDirectoryName)

        val stickers = arrayListOf<Sticker>()
        for ((index, file) in directory.listFiles().withIndex()) {
            if (index >= 30) break
            else {
                try {
                    if (file.name.contains(".webp")) {
//                        val fileName = file.name
//                        val fileExt = fileName.substring(fileName.lastIndexOf(".") + 1)
                        newDirectory.mkdirs()
                        if (index == 0) makeTray(file, newDirectory.path)
                        val input: InputStream = BufferedInputStream(FileInputStream(file))
                        val output: OutputStream = BufferedOutputStream(FileOutputStream(newDirectory.path + File.separator + file.name))
                        val buffer = ByteArray(4096)
                        var lengthRead: Int
                        while (input.read(buffer).also { lengthRead = it } != -1) {
                            output.write(buffer, 0, lengthRead)
                            output.flush()
                        }
                        stickers.add(Sticker(file.name, listOf(""), true, null))
                    }
                } catch (e: IOException) {
                    Log.e("STICKER ADDER", e.message, e)
                    return false
                }
            }
        }
        val name = params[0]!!.split("/")
        val stickerPack = StickerPack(newDirectoryName, name[name.lastIndex], "You", "tray.png", "", "", "", "", "1", avoidCache = false, iosAppStoreLink = null, androidPlayStoreLink = null, isAnimated = true, stickers = stickers, totalSize = null, isWhitelisted = null)
        StickerJsonWriter.writeJson(FileOutputStream(newDirectory.path + File.separator + StickerContentProvider.CONTENT_FILE_NAME), stickerPack)
        val intent = createIntentToAddStickerPack(stickerPack.identifier, stickerPack.name)
        try {
            activity.startActivityForResult(intent, 200)
        } catch (e: ActivityNotFoundException) {
            Log.e("ERROR", e.message, e)
        }
        return true
    }

    private fun makeTray(file: File, directory: String) {
        val bitmap = BitmapFactory.decodeFile(file.path)
        val resized = Bitmap.createScaledBitmap(bitmap, 96, 96, false)
        val fos = FileOutputStream(directory + File.separator + "tray.png")
        resized.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()
    }

    private fun createIntentToAddStickerPack(identifier: String, stickerPackName: String): Intent? {
        val intent = Intent()
        intent.action = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
        intent.putExtra("sticker_pack_id", identifier)
        intent.putExtra("sticker_pack_authority", BuildConfig.CONTENT_PROVIDER_AUTHORITY)
        intent.putExtra("sticker_pack_name", stickerPackName)
        return intent
    }

    companion object {
        private fun generateName(): String {
            val uuid = UUID.randomUUID()
            val l = ByteBuffer.wrap(uuid.toString().toByteArray()).long
            return l.toString(Character.MAX_RADIX)
        }
    }

}