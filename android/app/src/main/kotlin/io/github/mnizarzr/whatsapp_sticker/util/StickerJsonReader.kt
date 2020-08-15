/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.github.mnizarzr.whatsapp_sticker.util

import android.util.JsonReader
import io.github.mnizarzr.whatsapp_sticker.model.Sticker
import io.github.mnizarzr.whatsapp_sticker.model.StickerPack
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

internal object StickerJsonReader {

    @Throws(IOException::class, IllegalStateException::class)
    fun parseStickerPacks(inputStream: InputStream): List<StickerPack> {
        JsonReader(InputStreamReader(inputStream)).use { reader ->
            return readStickerPacks(reader)
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    private fun readStickerPacks(reader: JsonReader): List<StickerPack> {
        val stickerPacks = arrayListOf<StickerPack>()
        var androidPlayStoreLink = ""
        var iosAppStoreLink = ""
        reader.beginObject()
        while (reader.hasNext()) {
            var key = reader.nextName()
            when (key) {
                "android_play_store_link" -> androidPlayStoreLink = reader.nextString()
                "ios_app_store_link" -> iosAppStoreLink = reader.nextString()
                "sticker_packs" -> {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        val stickerPack = readStickerPack(reader)
                        stickerPacks.add(stickerPack)
                    }
                    reader.endArray()
                }
                else -> throw IllegalStateException("Unknown field in json: $key")
            }
        }
        reader.endObject()
        if (stickerPacks.isNullOrEmpty()) throw IllegalStateException("Sticker pack list cannot be empty")
        for (stickerPack in stickerPacks) {
            stickerPack.androidPlayStoreLink = androidPlayStoreLink
            stickerPack.iosAppStoreLink = iosAppStoreLink
        }
        return stickerPacks
    }

    @Throws(IOException::class, IllegalStateException::class)
    private fun readStickerPack(reader: JsonReader): StickerPack {
        reader.beginObject()
        var identifier: String? = null
        var name: String? = null
        var publisher: String? = null
        var trayImageFile: String? = null
        var publisherEmail: String? = null
        var publisherWebsite: String? = null
        var privacyPolicyWebsite: String? = null
        var licenseAgreementWebsite: String? = null
        var imageDataVersion: String? = null
        var avoidCache = false
        var stickers: List<Sticker>? = null
        var isAnimated = false
        while (reader.hasNext()) {
            var key = reader.nextName()
            when (key) {
                "identifier" -> identifier = reader.nextString()
                "name" -> name = reader.nextString()
                "publisher" -> publisher = reader.nextString()
                "tray_image_file" -> trayImageFile = reader.nextString()
                "publisher_email" -> publisherEmail = reader.nextString()
                "publisher_website" -> publisherWebsite = reader.nextString()
                "privacy_policy_website" -> privacyPolicyWebsite = reader.nextString()
                "license_agreement_website" -> licenseAgreementWebsite = reader.nextString()
                "stickers" -> stickers = readStickers(reader)
                "image_data_version" -> imageDataVersion = reader.nextString()
                "avoid_cache" -> avoidCache = reader.nextBoolean()
                "animated_sticker_pack" -> isAnimated = reader.nextBoolean()
                else -> reader.skipValue()
            }
        }
        if (identifier.isNullOrBlank()) throw IllegalStateException("identifier cannot be empty")
        if (name.isNullOrBlank()) throw IllegalStateException("name cannot be empty")
        if (publisher.isNullOrBlank()) throw IllegalStateException("publisher cannot be empty")
        if (trayImageFile.isNullOrBlank()) throw IllegalStateException("tray_image_file cannot be empty")
        if (stickers.isNullOrEmpty()) throw IllegalStateException("sticker list is empty")
        if (identifier.contains("..") || identifier.contains("/")) throw IllegalStateException("identifier should not contain .. or / to prevent directory traversal")
        if (imageDataVersion.isNullOrBlank()) throw IllegalStateException("image_data_version should not be empty")
        reader.endObject()
        return StickerPack(identifier, name, publisher, trayImageFile, publisherEmail
                ?: "", publisherWebsite ?: "", privacyPolicyWebsite ?: "", licenseAgreementWebsite
                ?: "", imageDataVersion, avoidCache, null, null, stickers, null, null, isAnimated = isAnimated)
    }

    @Throws(IOException::class, IllegalStateException::class)
    private fun readStickers(reader: JsonReader): List<Sticker> {
        reader.beginArray()
        val stickers = arrayListOf<Sticker>()
        while (reader.hasNext()) {
            reader.beginObject()
            var imageFile: String? = null
            val emojis = ArrayList<String>(3)
            var isAnimated = false
            while (reader.hasNext()) {
                var key = reader.nextName()
                when (key) {
                    "image_file" -> imageFile = reader.nextString()
                    "emojis" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            var emoji = reader.nextString()
                            if (emoji.isNotBlank()) {
                                emojis.add(emoji)
                            }
                        }
                        reader.endArray()
                    }
                    "is_animated_sticker" -> isAnimated = reader.nextBoolean()
                    else -> throw  IllegalStateException("Unknown field in json: $key")
                }
            }
            reader.endObject()
            if (imageFile.isNullOrBlank()) throw IllegalStateException("sticker image_file cannot be empty")
            if (!imageFile.endsWith(".webp")) throw IllegalStateException("image file for stickers should be webp files, image file is: $imageFile")
            if (imageFile.contains("..") || imageFile.contains("/")) throw IllegalStateException("the file name should not contain .. or / to prevent directory traversal, image file is:$imageFile")
            stickers.add(Sticker(imageFile, emojis, isAnimated, null))
        }
        reader.endArray()
        return stickers
    }

}