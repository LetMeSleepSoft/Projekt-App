package org.dieschnittstelle.mobile.android.kotlin.skeleton.remote

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItemDetails
import java.util.UUID

@Serializable
data class MediaItemDTO(
    @SerialName("id")
    val id: String,
    @SerialName("remote_id")
    @Serializable(with = UUIDSerializer::class)
    val remoteId: UUID = UUID(0L, 0L),
    @SerialName("title")
    val title: String,
    @SerialName("src")
    @Serializable(with = ByteArraySerializer::class)
    val src: ByteArray?,
    @SerialName("createDate")
    val createDate: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaItemDTO

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
        result = 31 * result + (src?.contentHashCode() ?: 0)
        return result
    }
}
fun MediaItemDTO.toEntity(): MediaItem = MediaItem(
    id = 0,
    remoteId = remoteId,
    title = this.title,
    src = this.src,
    createDate = createDate
)

fun MediaItemDTO.toMediaItemDetails(): MediaItemDetails = MediaItemDetails(
    id = id.toLong(),
    remoteId = remoteId,
    title = title,
    src = src,
    createDate = createDate
)

object ByteArraySerializer : KSerializer<ByteArray> {
    override val descriptor = PrimitiveSerialDescriptor("ByteArray", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): ByteArray {
        return decoder.decodeString().decodeBase64()
    }

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.encodeString(value.encodeBase64())
    }

}

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())

    }
}

fun ByteArray.encodeBase64(): String =
    android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)

fun String.decodeBase64(): ByteArray =
    android.util.Base64.decode(this, android.util.Base64.NO_WRAP)