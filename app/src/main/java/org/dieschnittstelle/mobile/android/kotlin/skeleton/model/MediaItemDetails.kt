package org.dieschnittstelle.mobile.android.kotlin.skeleton.model

import java.lang.System.currentTimeMillis

data class MediaItemDetails(
    val id: Long = 0,
    val title: String = "",
    val src: ByteArray? = byteArrayOf(),
    val createDate: Long = currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaItemDetails

        if (id != other.id) return false
        if (createDate != other.createDate) return false
        if (title != other.title) return false
        if (!src.contentEquals(other.src)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + createDate.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + src.contentHashCode()
        return result
    }
}

fun MediaItemDetails.toMediaItem(): MediaItem = MediaItem(
    id = id,
    title = title,
    src = src,
    createDate = createDate
)