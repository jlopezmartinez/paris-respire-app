package parisrespire.data.http.model

import kotlinx.serialization.Serializable

@Serializable
data class IndiceJour(
    val date: String,
    val global: PollutantIndex? = null,
    val no2: PollutantIndex? = null,
    val o3: PollutantIndex? = null,
    val pm10: PollutantIndex? = null,
    val url_carte: String? = null,
    val indices: String? = null
) {
    fun isEmpty() =
        date.isEmpty() && global == null && no2 == null && o3 == null && pm10 == null && url_carte.isNullOrEmpty() && indices.isNullOrEmpty()

    fun isNotEmpty() =
        !isEmpty()
}