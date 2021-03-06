package fr.parisrespire

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.parisrespire.mpp.data.http.model.Episode
import fr.parisrespire.mpp.data.http.model.PollutantDetails
import fr.parisrespire.mpp.data.http.model.util.Day
import fr.parisrespire.mpp.data.http.model.util.PollutionLevel
import fr.parisrespire.work.AlertWorkUtil
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertWorkUtilTest {

    private lateinit var context: Context
    private lateinit var util: AlertWorkUtil
    private val episode = Episode(
        date = Day.TOMORROW.value,
        detail = "Il est conseillé d'éviter les déplacements en Ile de France",
        o3 = PollutantDetails(listOf("km", "pop"), "info", "constate"),
        no2 = PollutantDetails(listOf("km", "pop"), "info", "constate"),
        pm10 = PollutantDetails(listOf("km"), "alerte", "constate")
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        util = AlertWorkUtil(context)
    }

    @Test
    fun testEpisodeToPollutantList() {
        val list = util.episodeToPollutantList(episode)
        val pm10 = list.find { it.first == context.getString(R.string.pm10) }
        val so2 = list.find { it.first == context.getString(R.string.so2) }
        val no2 = list.find { it.first == context.getString(R.string.no2) }
        val o3 = list.find { it.first == context.getString(R.string.o3) }
        assertNotNull(pm10)
        assertNotNull(no2)
        assertNotNull(o3)
        assertNull(so2)
        assertEquals(PollutionLevel.ALERT.value, pm10?.second?.niveau)
        assertEquals(PollutionLevel.INFO.value, o3?.second?.niveau)
        assertEquals(PollutionLevel.INFO.value, no2?.second?.niveau)
    }

    @Test
    fun testFormatAlertMessage() {
        val alertPart = String.format(
            context.getString(R.string.pollution_message),
            context.getString(R.string.alert),
            context.getString(R.string.pm10)
        ).capitalize()
        val pollutants = "${context.getString(R.string.o3)}, ${context.getString(fr.parisrespire.R.string.no2)}"
        val infoPart = String.format(
            context.getString(R.string.pollution_message),
            context.getString(R.string.information),
            pollutants
        ).capitalize()
        val expectedMessage =
            "$alertPart<br>$infoPart<br>Il est conseillé d'éviter les déplacements en Ile de France"
        val list = util.episodeToPollutantList(episode)
        val actual = util.formatAlertMessage(list, episode.detail)
        assertTrue(actual.second)
        assertEquals("expected=$expectedMessage actual=$actual", expectedMessage, actual.first)
    }
}
