package org.dieschnittstelle.mobile.android.kotlin.skeleton.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.lang.System.currentTimeMillis

@Entity(tableName = "mediaItems")
data class MediaItem(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String,
    val src: ByteArray?,
    val createDate: Long = currentTimeMillis(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaItem

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

fun MediaItem.toMediaItemDetails(): MediaItemDetails = MediaItemDetails(
    id = id,
    title = title,
    src = src,
    createDate = createDate
)


