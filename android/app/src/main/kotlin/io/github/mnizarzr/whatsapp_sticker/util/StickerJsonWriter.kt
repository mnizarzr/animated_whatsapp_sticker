package io.github.mnizarzr.whatsapp_sticker.util

import android.util.JsonWriter
import io.github.mnizarzr.whatsapp_sticker.model.Sticker
import io.github.mnizarzr.whatsapp_sticker.model.StickerPack
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter

internal object StickerJsonWriter {

    private const val UTF_8 = "UTF-8"

    @Throws(IOException::class)
    fun writeJson(outputStream: OutputStream, stickerPack: StickerPack) {
        JsonWriter(OutputStreamWriter(outputStream, UTF_8)).use { writer ->

            writer.beginObject()
            writer.name("android_play_store_link").value("")
            writer.name("ios_app_store_link").value("")
            writer.name("sticker_packs")
            writerStickerPack(writer, stickerPack)
            writer.endObject()

            writer.flush()
            writer.close()
        }
    }

    private fun writerStickerPack(writer: JsonWriter, stickerPack: StickerPack) {
        writer.beginArray()
        writer.beginObject()
        writer.name("identifier").value(stickerPack.identifier)
        writer.name("name").value(stickerPack.name)
        writer.name("publisher").value(stickerPack.publisher)
        writer.name("tray_image_file").value(stickerPack.trayImageFile)
        writer.name("image_data_version").value(stickerPack.imageDataVersion)
        writer.name("avoid_cache").value(stickerPack.avoidCache)
        writer.name("publisher_email").value(stickerPack.publisherEmail)
        writer.name("publisher_website").value(stickerPack.publisherWebsite)
        writer.name("privacy_policy_website").value(stickerPack.privacyPolicyWebsite)
        writer.name("license_agreement_website").value(stickerPack.licenseAgreementWebsite)
        writer.name("animated_sticker_pack").value(stickerPack.isAnimated)
        writeStickers(writer, stickerPack.stickers)
        writer.endObject()
        writer.endArray()
    }

    private fun writeStickers(writer: JsonWriter, stickers: List<Sticker>) {
        writer.name("stickers")
        writer.beginArray()
        for (sticker in stickers) {
            writer.beginObject()
            writer.name("image_file").value(sticker.imageFileName)

            writer.name("emojis")
            writer.beginArray()
            writer.endArray()

            writer.name("is_animated_sticker").value(sticker.isAnimated)
            writer.endObject()
        }
        writer.endArray()
    }

}