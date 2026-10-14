package at.asitplus.etsi.relyingParty

import at.asitplus.etsi.relyingParty.WrpConstants.POLICY_IDENTIFIER
import at.asitplus.signum.indispensable.io.InstantLongSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * ETSI TS 119 475 V1.2.1 Annex C WRPRC payload.
 */
@Serializable
data class WrpPayload(
    /** Trade name, common name or service name of the relying party (Table 7; clause B.2.1). */
    @SerialName("name")
    val name: String? = null,

    /**
     * Officially recorded legal names, when the relying party is a legal person (Table 7; clause B.2.3). `legalName` is
     * [1..*] in B.2.3, so this is decoded from a string or an array, see [WrpStringOrArraySerializer].
     */
    @SerialName("sub_ln")
    @Serializable(with = WrpStringOrArraySerializer::class)
    val subjectLegalName: List<String>? = null,

    /** Officially recorded given names, including middle names, for a natural person (Table 7; clause B.2.4). */
    @SerialName("sub_gn")
    val subjectGivenName: String? = null,

    /** Officially recorded family names or surnames for a natural person (Table 7; clause B.2.4). */
    @SerialName("sub_fn")
    val subjectFamilyName: String? = null,

    /** Registered identifier of the relying party on whose behalf data access occurs, even via an intermediary (Table 7). */
    @SerialName("sub")
    val subjectIdentifier: String,

    /** ISO 3166-1 alpha-2 country of establishment, or "EU" for providers operating at European level (clause B.2.2). */
    @SerialName("country")
    val country: String,

    /** URL of the national registry API endpoint for the registered relying party (Table 7). */
    @SerialName("registry_uri")
    val registryUri: String,

    /** Service descriptions, each containing localized versions of the same description (Table 7; clause B.2.1). */
    @SerialName("srv_description")
    val srvDescription: List<List<WrpLangString>>,

    /** Entitlement identifiers assigned to the relying party as specified in Annex A (Table 7). */
    @SerialName("entitlements")
    val entitlements: List<String>,

    /**
     * URLs of the privacy policies explaining data processing and storage practices (Table 7; `policyURI` of clause
     * B.2.8). `privacyPolicy` is [1..*] in B.2.7, so this is decoded from a string or an array, see
     * [WrpStringOrArraySerializer].
     */
    @SerialName("privacy_policy")
    @Serializable(with = WrpStringOrArraySerializer::class)
    val privacyPolicy: List<String>,

    /**
     * General-purpose URLs providing public information about the relying party (Table 7; clause B.2.2). `infoURI`
     * is [0..*] in B.2.2, so this is only present if provided by the registry (GEN-5.2.4-01), for legal and natural
     * persons alike (Tables 1 and 3). Decoded from a string or an array, see [WrpStringOrArraySerializer].
     */
    @SerialName("info_uri")
    @Serializable(with = WrpStringOrArraySerializer::class)
    val infoUri: List<String>? = null,

    /**
     * URLs or email addresses for data deletion or portability requests (Table 7). `supportURI` is [1..*] in B.2.1,
     * so a valid WRPRC carries at least one; it is optional here to decode WRPRCs that lack it. Decoded from a string
     * or an array, see [WrpStringOrArraySerializer].
     */
    @SerialName("support_uri")
    @Serializable(with = WrpStringOrArraySerializer::class)
    val supportUri: List<String>? = null,

    /** Contact details of the competent data protection supervisory authority (Table 7). */
    @SerialName("supervisory_authority")
    val supervisoryAuthority: WrpSupervisoryAuthority? = null,

    /** Certificate policy identifiers applicable to the WRPRC (Table 7; clause 6.1.3). */
    @SerialName("policy_id")
    val policyId: List<String> = listOf(POLICY_IDENTIFIER),

    /** URL of the certificate policy and certification practice statement (Table 7). */
    @SerialName("certificate_policy")
    val certificatePolicy: String,

    /** Time the WRPRC was issued, encoded as a Unix timestamp (Table 7). */
    @SerialName("iat")
    @Serializable(with = InstantLongSerializer::class)
    val iat: Instant,

    /** Status list reference conveying the validity of the WRPRC (Table 7; Annex C). */
    @SerialName("status")
    val status: WrpStatus,

    /** Localized descriptions of data processing associated with the intended use (Table 9). */
    @SerialName("purpose")
    val purpose: List<WrpLangString> = emptyList(),

    /** Credential queries describing what the relying party may request, used for over-asking validation (Table 9). */
    @SerialName("credentials")
    val credentials: List<WrpCredential> = emptyList(),

    /** Registry-provided unique identifier used to retrieve the intended use from the registry (Table 9). */
    @SerialName("intended_use_id")
    val intendedUseId: String? = null,

    /** Credentials issued by the relying party with attestation-provider entitlements (Table 8). */
    @SerialName("provides_attestations")
    val providesAttestations: List<WrpCredential> = emptyList(),

    /** Whether the relying party is a public sector body (Table 10). */
    @SerialName("public_body")
    val publicBody: Boolean? = null,

    /** Intermediary acting on behalf of the relying party in intermediated interactions (Table 10). */
    @SerialName("intermediary")
    val intermediary: WrpIntermediary? = null,

    /** WRPRC expiration time, at most 12 months after [iat], encoded as a Unix timestamp (Table 10; GEN-5.2.4-08). */
    @SerialName("exp")
    @Serializable(with = InstantLongSerializer::class)
    val exp: Instant? = null,
)
