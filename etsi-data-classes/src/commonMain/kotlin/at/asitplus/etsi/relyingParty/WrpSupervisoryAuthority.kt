package at.asitplus.etsi.relyingParty

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ETSI TS 119 475 V1.2.1 Annex C supervisory-authority contact block. Its fields map to `email`, `phone` and `infoURI`
 * of B.2.2 LegalEntity (Table 7), each [0..*], so they are decoded from a string or an array, see
 * [WrpStringOrArraySerializer].
 */
@Serializable
data class WrpSupervisoryAuthority(
    /** Email addresses of the competent data protection authority (Table 7). */
    @SerialName("email")
    @Serializable(with = WrpStringOrArraySerializer::class)
    val email: List<String>? = null,

    /** Telephone numbers of the competent data protection authority (Table 7). */
    @SerialName("phone")
    @Serializable(with = WrpStringOrArraySerializer::class)
    val phone: List<String>? = null,

    /** URLs of the web form provided by the competent data protection authority (Table 7). */
    @SerialName("uri")
    @Serializable(with = WrpStringOrArraySerializer::class)
    val uri: List<String>? = null,
)
