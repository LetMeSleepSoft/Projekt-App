package org.dieschnittstelle.mobile.android.kotlin.skeleton.model

import org.dieschnittstelle.mobile.android.kotlin.skeleton.remote.MediaItemDTO
import java.lang.System.currentTimeMillis
import java.util.UUID

data class MediaItemDetails(
    val id: Long = 0,
    val remoteId: UUID = UUID(0L, 0L),
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
    remoteId = remoteId,
    title = title,
    src = src,
    createDate = createDate
)

fun MediaItemDetails.toDto(): MediaItemDTO = MediaItemDTO(
    id = id.toString(),
    remoteId = remoteId,
    title = title,
    src = src,
    createDate = createDate
)