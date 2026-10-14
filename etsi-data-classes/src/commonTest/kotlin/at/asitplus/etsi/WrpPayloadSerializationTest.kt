package at.asitplus.etsi

import at.asitplus.etsi.relyingParty.WrpConstants
import at.asitplus.etsi.relyingParty.WrpPayload
import at.asitplus.etsi.relyingParty.WrpStringOrArraySerializer
import at.asitplus.signum.indispensable.josef.io.joseCompliantSerializer
import at.asitplus.testballoon.matrix.matrixSuite
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.add

/**
 * The payload of the WRPRC example of ETSI TS 119 475 V1.2.1 Annex C, with the intermediary name in `sname` (Table 10)
 * instead of the `name` of the example.
 */
private val annexCPayload = """
{
    "name": "Example Company",
    "sub_ln": "Example Company GmbH",
    "sub": "LEIXG-529900T8BM49AURSD055",
    "country": "DE",
    "registry_uri": "https://registrar.com",
    "srv_description": [[
        {"lang": "en-US", "value": "Awesome Service by Example Company"},
        {"lang": "de-DE", "value": "Super Dienst von Example Company"}
    ]],
    "entitlements": ["https://uri.etsi.org/19475/Entitlement/Non_Q_EAA_Provider"],
    "privacy_policy": "https://example.com/privacy-policy",
    "info_uri": "https://example.com/info",
    "support_uri": "https://example.com/support",
    "supervisory_authority": {
        "email": "supervisory@dpa.com",
        "phone": "+49 123 4567890",
        "uri": "https://dpa.com/supervisory-authority"
    },
    "policy_id": ["0.4.0.19475.3.1"],
    "certificate_policy": "https://registrar.com/certificate-policy",
    "iat": 1683000000,
    "status": {"status_list": {"idx": 0, "uri": "https://example.com/statuslists/1"}},
    "purpose": [
        {"lang": "en-US", "value": "Required for checking the minimum age"},
        {"lang": "de-DE", "value": "Benötigt für die Überprüfung des Mindestalters"}
    ],
    "credentials": [
        {
            "format": "dc+sd-jwt",
            "meta": {"vct_values": ["urn:eudi:pid:de:1"]},
            "claim": [{"path": ["age_equal_or_over", "18"]}]
        },
        {
            "format": "mso_mdoc",
            "meta": {"doctype_value": "eu.europa.ec.eudi.pid.1"},
            "claim": [{"path": ["eu.europa.ec.eudi.pid.1", "age_over_18"]}]
        }
    ],
    "provides_attestations": [
        {"format": "dc+sd-jwt", "meta": {"vct_values": ["https://example.com/attestations/age_over_18"]}}
    ],
    "intermediary": {"sub": "LEIXG-INTERMEDIARY-1234567890", "sname": "Intermediary Services Ltd."}
}
""".trimIndent()

val WrpPayloadSerializationTest by matrixSuite {

    "the Annex C payload decodes, with single strings as lists of one" {
        val payload = joseCompliantSerializer.decodeFromString(WrpPayload.serializer(), annexCPayload)

        payload.subjectLegalName shouldBe listOf("Example Company GmbH")
        payload.privacyPolicy shouldBe listOf("https://example.com/privacy-policy")
        payload.infoUri shouldBe listOf("https://example.com/info")
        payload.supportUri shouldBe listOf("https://example.com/support")
        payload.supervisoryAuthority?.email shouldBe listOf("supervisory@dpa.com")
        payload.supervisoryAuthority?.phone shouldBe listOf("+49 123 4567890")
        payload.supervisoryAuthority?.uri shouldBe listOf("https://dpa.com/supervisory-authority")
        payload.policyId shouldBe listOf(WrpConstants.POLICY_IDENTIFIER)
    }

    "single values encode as strings, as in Annex C" {
        val payload = joseCompliantSerializer.decodeFromString(WrpPayload.serializer(), annexCPayload)

        val encoded = joseCompliantSerializer.encodeToJsonElement(WrpPayload.serializer(), payload).jsonObject

        encoded["sub_ln"] shouldBe JsonPrimitive("Example Company GmbH")
        encoded["privacy_policy"] shouldBe JsonPrimitive("https://example.com/privacy-policy")
        encoded["info_uri"] shouldBe JsonPrimitive("https://example.com/info")
        encoded["support_uri"] shouldBe JsonPrimitive("https://example.com/support")
        encoded["supervisory_authority"]?.jsonObject?.get("email") shouldBe JsonPrimitive("supervisory@dpa.com")
        joseCompliantSerializer.decodeFromJsonElement(WrpPayload.serializer(), encoded) shouldBe payload
    }

    "fields with multiplicity [0..*] or [1..*] decode from arrays and encode several values as arrays" {
        val twoValues = buildJsonArray { add("https://example.com/a"); add("mailto:support@example.com") }
        val json = joseCompliantSerializer.parseToJsonElement(annexCPayload).jsonObject.let { payload ->
            JsonObject(
                payload + mapOf(
                    "sub_ln" to twoValues,
                    "privacy_policy" to twoValues,
                    "info_uri" to twoValues,
                    "support_uri" to twoValues,
                    "supervisory_authority" to JsonObject(
                        mapOf("email" to twoValues, "phone" to twoValues, "uri" to twoValues)
                    ),
                )
            )
        }

        val payload = joseCompliantSerializer.decodeFromJsonElement(WrpPayload.serializer(), json)

        val expected = listOf("https://example.com/a", "mailto:support@example.com")
        payload.subjectLegalName shouldBe expected
        payload.privacyPolicy shouldBe expected
        payload.infoUri shouldBe expected
        payload.supportUri shouldBe expected
        payload.supervisoryAuthority?.email shouldBe expected
        payload.supervisoryAuthority?.phone shouldBe expected
        payload.supervisoryAuthority?.uri shouldBe expected
        val encoded = joseCompliantSerializer.encodeToJsonElement(WrpPayload.serializer(), payload).jsonObject
        encoded["sub_ln"] shouldBe twoValues
        encoded["privacy_policy"] shouldBe twoValues
        encoded["info_uri"] shouldBe twoValues
        encoded["support_uri"] shouldBe twoValues
        encoded["supervisory_authority"] shouldBe json["supervisory_authority"]
    }

    "info_uri is only present if provided by the registry" {
        val json = joseCompliantSerializer.parseToJsonElement(annexCPayload).jsonObject
            .let { JsonObject(it - "info_uri") }

        val payload = joseCompliantSerializer.decodeFromJsonElement(WrpPayload.serializer(), json)

        payload.infoUri shouldBe null
        joseCompliantSerializer.encodeToString(WrpPayload.serializer(), payload) shouldNotContain "info_uri"
    }

    "a string or array is no number, object, or array of other values" {
        listOf("1", "{}", "[1]", "[{}]", "[null]").forEach {
            shouldThrowAny { joseCompliantSerializer.decodeFromString(WrpStringOrArraySerializer, it) }
        }
    }
}
