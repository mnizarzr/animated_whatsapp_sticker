package io.github.mnizarzr.whatsapp_sticker.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StickerPack(
        val identifier: String,
        val name: String,
        val publisher: String,
        val trayImageFile: String,
        val publisherEmail: String,
        val publisherWebsite: String,
        val privacyPolicyWebsite: String,
        val licenseAgreementWebsite: String,
        val imageDataVersion: String,
        val avoidCache: Boolean,
        var iosAppStoreLink: String?,
        var androidPlayStoreLink: String?,
        val stickers: List<Sticker>,
        var totalSize: Long?,
        val isWhitelisted: Boolean?,
        val isAnimated: Boolean
) :Parcelable