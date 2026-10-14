package at.asitplus.etsi.relyingParty

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

/**
 * Serializes a WRPRC field with multiplicity [0..*] or [1..*] in ETSI TS 119 475 V1.2.1 Annex B, e.g. `support_uri`
 * (B.2.1) or `info_uri` (B.2.2), which carries a single string or an array of strings. Encodes a single value as a
 * string, as in Annex C, and several values as an array.
 *
 * In CBOR (CWT WRPRC) a string is tried first, then an array, which relies on the CBOR parser of kotlinx-serialization
 * (since 1.10.0) checking the major type before consuming input.
 */
@OptIn(ExperimentalSerializationApi::class)
object WrpStringOrArraySerializer : KSerializer<List<String>> {

    private val listSerializer = ListSerializer(String.serializer())

    override val descriptor: SerialDescriptor =
        SerialDescriptor("at.asitplus.etsi.relyingParty.WrpStringOrArray", listSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: List<String>) {
        val single = value.singleOrNull()
        if (single != null) encoder.encodeString(single)
        else encoder.encodeSerializableValue(listSerializer, value)
    }

    override fun deserialize(decoder: Decoder): List<String> = if (decoder is JsonDecoder) {
        when (val element = decoder.decodeJsonElement()) {
            is JsonPrimitive if element.isString -> listOf(element.content)
            is JsonArray -> element.map {
                (it as? JsonPrimitive)?.takeIf { it.isString }?.content
                    ?: throw SerializationException("Invalid element of a string or array: $it")
            }

            else -> throw SerializationException("Invalid string or array: $element")
        }
    } else try {
        listOf(decoder.decodeString())
    } catch (_: SerializationException) {
        decoder.decodeSerializableValue(listSerializer)
    }
}
