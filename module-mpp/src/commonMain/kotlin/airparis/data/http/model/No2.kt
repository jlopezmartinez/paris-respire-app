package airparis.data.http.model

import kotlinx.serialization.Serializable

@Serializable
data class No2(
    val indice: Int?,
    val url_carte: String?
)
