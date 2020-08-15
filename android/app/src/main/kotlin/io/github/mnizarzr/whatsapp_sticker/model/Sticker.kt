package io.github.mnizarzr.whatsapp_sticker.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Sticker(
        val imageFileName: String,
        val emojis: List<String>,
        val isAnimated: Boolean,
        val size: Long?
) : Parcelable