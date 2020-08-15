package io.github.mnizarzr.whatsapp_sticker

import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.util.Log
import io.github.mnizarzr.whatsapp_sticker.model.StickerPack
import io.github.mnizarzr.whatsapp_sticker.util.StickerJsonReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

class StickerContentProvider : ContentProvider() {

    companion object {

        private val TAG = StickerContentProvider::class.java.simpleName

        val STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier"
        val STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name"
        val STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher"
        val STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon"
        val ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link"
        val IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link";
        val PUBLISHER_EMAIL = "sticker_pack_publisher_email";
        val PUBLISHER_WEBSITE = "sticker_pack_publisher_website";
        val PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website";
        val LICENSE_AGREENMENT_WEBSITE = "sticker_pack_license_agreement_website";
        val IMAGE_DATA_VERSION = "image_data_version";
        val AVOID_CACHE = "whatsapp_will_not_cache_stickers";

        val STICKER_FILE_NAME_IN_QUERY = "sticker_file_name";
        val STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji";
        val CONTENT_FILE_NAME = "contents.json";
        val STICKER_PACKS_DIRECTORY = "sticker_packs"


        private const val METADATA = "metadata"
        val AUTHORITY_URI = Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(METADATA).build()

        val MATCHER = UriMatcher(UriMatcher.NO_MATCH)

        private const val METADATA_CODE = 1
        private const val METADATA_CODE_FOR_SINGLE_PACK = 2
        private const val STICKERS_CODE = 3
        private const val STICKERS_ASSET_CODE = 4
        private const val STICKER_PACK_TRAY_ICON_CODE = 5

        val STICKERS = "stickers"
        val STICKERS_ASSET = "stickers_asset"

        private val ANIMATED_STICKER = "animated_sticker_pack"
        private val STICKER_FILE_ANIMATED = "is_animated_sticker"

    }

    private val stickerPacksList = arrayListOf<List<StickerPack>>()
    private lateinit var mContext: Context

    override fun onCreate(): Boolean {

        mContext = requireNotNull(context)

        val authority = BuildConfig.CONTENT_PROVIDER_AUTHORITY
        if (!authority.startsWith(requireNotNull(context).packageName)) {
            throw IllegalStateException("your authority \"$authority\" for the content provider should start with your package name: ${context?.packageName}")
        }

        //the call to get the metadata for the sticker packs.
        MATCHER.addURI(authority, METADATA, METADATA_CODE)

        //the call to get the metadata for single sticker pack. * represent the identifier
        MATCHER.addURI(authority, "$METADATA/*", METADATA_CODE_FOR_SINGLE_PACK)

        //gets the list of stickers for a sticker pack, * represent the identifier.
        MATCHER.addURI(authority, "$STICKERS/*", STICKERS_CODE)
        MATCHER.addURI(authority, "$STICKERS_ASSET/*/tray.png", STICKER_PACK_TRAY_ICON_CODE)
        MATCHER.addURI(authority, "$STICKERS_ASSET/*/*", STICKERS_ASSET_CODE)

        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return when (MATCHER.match(uri)) {
            METADATA_CODE -> getPackForAllStickerPacks(uri)
            METADATA_CODE_FOR_SINGLE_PACK -> getCursorForSingleStickerPack(uri)
            STICKERS_CODE -> getStickersForAStickerPack(uri)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun getType(uri: Uri): String? {
        return when (MATCHER.match(uri)) {
            METADATA_CODE -> "vnd.android.cursor.dir/vnd.${BuildConfig.CONTENT_PROVIDER_AUTHORITY}.${METADATA}"
            METADATA_CODE_FOR_SINGLE_PACK -> "vnd.android.cursor.item/vnd.${BuildConfig.CONTENT_PROVIDER_AUTHORITY}.${METADATA}"
            STICKERS_CODE -> "vnd.android.cursor.dir/vnd.${BuildConfig.CONTENT_PROVIDER_AUTHORITY}.${STICKERS}"
            STICKERS_ASSET_CODE -> "image/webp"
            STICKER_PACK_TRAY_ICON_CODE -> "image/png"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        return when (MATCHER.match(uri)) {
            STICKERS_ASSET_CODE, STICKER_PACK_TRAY_ICON_CODE -> getImageFile(uri)
            else -> null
        }
    }

    private fun getImageFile(uri: Uri): ParcelFileDescriptor? {
        val pathSegments = uri.pathSegments
        if (pathSegments.size != 3) throw IllegalArgumentException("path segments should be 3, uri is: $uri")
        val fileName = pathSegments[pathSegments.lastIndex]
        val identifier = pathSegments[pathSegments.size - 2]
        if (identifier.isNullOrBlank()) throw IllegalArgumentException("identifier is empty, uri: $uri")
        if (fileName.isNullOrBlank()) throw IllegalArgumentException("file name is empty, uri: $uri")
        val stickerPacks = readContentFile(identifier)
        for (stickerPack in stickerPacks) {
            if (identifier == stickerPack.identifier) {
                if (fileName == stickerPack.trayImageFile) return fetchFile(uri, identifier, fileName)
                else {
                    for (sticker in stickerPack.stickers) {
                        if (fileName == sticker.imageFileName)
                            return fetchFile(uri, identifier, fileName)
                    }
                }
            }
        }
        return null
    }

    private fun fetchFile(uri: Uri, identifier: String, fileName: String): ParcelFileDescriptor? {
        val stickerPacksDir = mContext.filesDir.path + File.separator + STICKER_PACKS_DIRECTORY
        val stickerPackDir = stickerPacksDir + File.separator + identifier
        return try {
            ParcelFileDescriptor.open(File(stickerPackDir + File.separator + fileName), ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (e: IOException) {
            Log.e(TAG, "IOException when getting asset file, uri: $uri", e)
            null
        }
    }

    private fun getPackForAllStickerPacks(uri: Uri): Cursor? {
        for (stickerPacks in getStickerPackList()) {
            return getStickerPackInfo(uri, stickerPacks)
        }
        return null
    }

    private fun getCursorForSingleStickerPack(uri: Uri): Cursor? {
        val identifier = uri.lastPathSegment
        checkNotNull(identifier)
        for (stickerPacks in getStickerPackList()) {
            for (stickerPack in stickerPacks) {
                if (identifier == stickerPack.identifier) {
                    return getStickerPackInfo(uri, listOf(stickerPack))
                }
            }
        }
        return getStickerPackInfo(uri, ArrayList())
    }


    private fun getStickerPackInfo(uri: Uri, stickerPackList: List<StickerPack>): Cursor {
        val cursor = MatrixCursor(
                arrayOf(
                        STICKER_PACK_IDENTIFIER_IN_QUERY,
                        STICKER_PACK_NAME_IN_QUERY,
                        STICKER_PACK_PUBLISHER_IN_QUERY,
                        STICKER_PACK_ICON_IN_QUERY,
                        ANDROID_APP_DOWNLOAD_LINK_IN_QUERY,
                        IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                        PUBLISHER_EMAIL,
                        PUBLISHER_WEBSITE,
                        PRIVACY_POLICY_WEBSITE,
                        LICENSE_AGREENMENT_WEBSITE,
                        IMAGE_DATA_VERSION,
                        AVOID_CACHE,
                        ANIMATED_STICKER
                )
        )

        for (stickerPack in stickerPackList) {
            val builder = cursor.newRow()
            builder.add(stickerPack.identifier)
            builder.add(stickerPack.name)
            builder.add(stickerPack.publisher)
            builder.add(stickerPack.trayImageFile)
            builder.add(stickerPack.androidPlayStoreLink)
            builder.add(stickerPack.iosAppStoreLink)
            builder.add(stickerPack.publisherEmail)
            builder.add(stickerPack.publisherWebsite)
            builder.add(stickerPack.privacyPolicyWebsite)
            builder.add(stickerPack.licenseAgreementWebsite)
            builder.add(stickerPack.imageDataVersion)
            builder.add(if (stickerPack.avoidCache) 1 else 0)
            builder.add(stickerPack.isAnimated)
        }
        cursor.setNotificationUri(requireNotNull(context).contentResolver, uri)
        return cursor
    }

    private fun getStickersForAStickerPack(uri: Uri): Cursor {
        val identifier = uri.lastPathSegment
        checkNotNull(identifier)
        val cursor = MatrixCursor(arrayOf(STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY, STICKER_FILE_ANIMATED))
        for (stickerPack in readContentFile(identifier)) {
            if (identifier == stickerPack.identifier) {
                for ((imageFileName, emojis, isAnimated) in stickerPack.stickers) {
                    cursor.addRow(arrayOf(imageFileName, TextUtils.join(",", emojis), isAnimated))
                }
            }
        }
        cursor.setNotificationUri(requireNotNull(context).contentResolver, uri)
        return cursor
    }

    private fun readContentFile(identifier: String): List<StickerPack> {
        synchronized(this) {
            val stickerPacksDir = mContext.filesDir.path + File.separator + STICKER_PACKS_DIRECTORY
            val stickerPackDir = stickerPacksDir + File.separator + identifier
            FileInputStream(stickerPackDir + File.separator + CONTENT_FILE_NAME).use { inputStream ->
                return StickerJsonReader.parseStickerPacks(inputStream)
            }
        }
    }

    private fun getStickerPackList(): List<List<StickerPack>> {
        if (stickerPacksList.size == 0) {
            readAllContentFile()
        }
        return stickerPacksList
    }


    private fun readAllContentFile() {
        synchronized(this) {
            var stickerPacksLists = arrayListOf<List<StickerPack>>()
            val stickerPacksDir = mContext.filesDir.path + File.separator + STICKER_PACKS_DIRECTORY
            val stickerPackDir = stickerPacksDir + File.separator
            val allStickerFiles = File(stickerPackDir).listFiles()
            if (!allStickerFiles.isNullOrEmpty()) {
                for (file in File(stickerPackDir).listFiles()) {
                    if (file.isDirectory) {
                        val contentJsonFile = file.path + File.separator + CONTENT_FILE_NAME
                        val inputStream = FileInputStream(file.path + File.separator + CONTENT_FILE_NAME)
                        val stickerPacks = StickerJsonReader.parseStickerPacks(inputStream)
                        stickerPacksLists.add(stickerPacks)
                    }
                }
                stickerPacksList.addAll(stickerPacksLists)
            }
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Not supported")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Not supported")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Not supported")
    }
}
