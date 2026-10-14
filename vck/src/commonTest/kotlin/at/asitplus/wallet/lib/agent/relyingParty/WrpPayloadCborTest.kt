package at.asitplus.wallet.lib.agent.relyingParty

import at.asitplus.etsi.relyingParty.WrpPayload
import at.asitplus.etsi.relyingParty.WrpStringOrArraySerializer
import at.asitplus.etsi.relyingParty.WrpSupervisoryAuthority
import at.asitplus.signum.indispensable.cosef.io.coseCompliantSerializer
import at.asitplus.testballoon.matrix.matrixSuite
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

/**
 * Fields of a CWT WRPRC with multiplicity [0..*] or [1..*] in ETSI TS 119 475 V1.2.1 Annex B carry a single string or
 * an array of strings, see [WrpStringOrArraySerializer].
 */
val WrpPayloadCborTest by matrixSuite {

    "a single string decodes as a list of one, and a list of one encodes as a string" {
        val bytes = coseCompliantSerializer.encodeToByteArray(AsString("https://example.com/support"))

        val decoded = coseCompliantSerializer.decodeFromByteArray<AsStringOrArray>(bytes)

        decoded.value shouldBe listOf("https://example.com/support")
        coseCompliantSerializer.encodeToByteArray(decoded) shouldBe bytes
    }

    "an array decodes as a list, and a list of several values encodes as an array" {
        val values = listOf("https://example.com/a", "mailto:support@example.com")
        val bytes = coseCompliantSerializer.encodeToByteArray(AsArray(values))

        val decoded = coseCompliantSerializer.decodeFromByteArray<AsStringOrArray>(bytes)

        decoded.value shouldBe values
        coseCompliantSerializer.encodeToByteArray(decoded) shouldBe bytes
    }

    "a number is no string or array" {
        val bytes = coseCompliantSerializer.encodeToByteArray(AsNumber(1))

        shouldThrowAny { coseCompliantSerializer.decodeFromByteArray<AsStringOrArray>(bytes) }
    }

    "a payload with several values in each field round-trips" {
        val values = listOf("https://example.com/a", "mailto:support@example.com")
        val payload = buildWrpPayload("WRP-cbor", supportUri = values).copy(
            subjectLegalName = values,
            privacyPolicy = values,
            infoUri = values,
            supervisoryAuthority = WrpSupervisoryAuthority(email = values, phone = values, uri = values),
        )

        val bytes = coseCompliantSerializer.encodeToByteArray(payload)

        coseCompliantSerializer.decodeFromByteArray<WrpPayload>(bytes) shouldBe payload
    }
}

@Serializable
private data class AsString(@SerialName("v") val value: String)

@Serializable
private data class AsArray(@SerialName("v") val value: List<String>)

@Serializable
private data class AsNumber(@SerialName("v") val value: Int)

@Serializable
private data class AsStringOrArray(
    @SerialName("v")
    @Serializable(with = WrpStringOrArraySerializer::class)
    val value: List<String>,
)
