package fr.parisrespire.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.crashlytics.android.Crashlytics
import com.google.android.material.snackbar.Snackbar
import com.mancj.slimchart.SlimChart
import com.squareup.picasso.Picasso
import dev.icerock.moko.mvvm.MvvmFragment
import dev.icerock.moko.mvvm.createViewModelFactory
import fr.parisrespire.R
import fr.parisrespire.databinding.FragmentAirQualityDetailsBinding
import fr.parisrespire.mpp.data.AirQualityViewModel
import fr.parisrespire.mpp.data.CustomException
import fr.parisrespire.mpp.data.UIExceptionHandler
import fr.parisrespire.mpp.data.http.model.util.Day
import fr.parisrespire.util.FragmentUtil
import fr.parisrespire.util.getErrorMessage
import kotlinx.android.synthetic.main.fragment_air_quality_details.*

const val POSITION_ARG = "position"

class AirQualityDetailsFragment :
    MvvmFragment<FragmentAirQualityDetailsBinding, AirQualityViewModel>(),
    Refresh,
    UIExceptionHandler {

    override val layoutId = R.layout.fragment_air_quality_details
    override val viewModelClass: Class<AirQualityViewModel> = AirQualityViewModel::class.java
    override val viewModelVariableId: Int = dev.icerock.moko.mvvm.BR.viewModel

    private var day: Day? = null
    private var position = -1
    private var snackbar: Snackbar? = null
    private var hasError = false
    private lateinit var util: FragmentUtil

    override fun viewModelFactory(): ViewModelProvider.Factory {
        return createViewModelFactory {
            AirQualityViewModel(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(POSITION_ARG)
            day = when (position) {
                0 -> Day.YESTERDAY
                1 -> Day.TODAY
                2 -> Day.TOMORROW
                else -> throw Exception("There must be only 3 instances of AirQualityDetailsFragment")
            }
        }
        util = FragmentUtil(resources)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        displayLoading()
        addObservers()
        viewModel.fetchDayIndex(day!!)
        viewModel.fetchPollutionEpisode(day!!)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        if (snackbar != null && snackbar!!.isShown)
            snackbar!!.dismiss()
    }

    override fun onResume() {
        super.onResume()
        if (snackbar != null && hasError)
            snackbar!!.show()
        // giving a reference of itself to the parent
        (parentFragment as CollectionAirQualityFragment).setCurrentPage(position, this)
    }

    private fun displayLoading() {
        progress.visibility = View.VISIBLE
        air_quality_group.visibility = View.GONE
        map_iv.visibility = View.GONE
    }

    private fun displayViews() {
        air_quality_group.visibility = View.VISIBLE
    }

    private fun addObservers() {
        viewModel.dayIndex.addObserver {
            if (context != null && it != null) {
                // show image "données disponibles au bulletin de 11h"
                when {
                    it.url_carte != null -> Picasso.get().load(it.url_carte)
                        .error(R.drawable.baseline_highlight_off_24).into(map_iv)
                    it.global != null -> {
                        // show pollution map
                        Picasso.get().load(it.global?.url_carte)
                            .error(R.drawable.baseline_highlight_off_24).into(map_iv)
                        displayViews()
                        // Global air quality
                        global_index_tv.text =
                            util.getQualityAdjectiveFromIndex(it.global?.indice, context)
                                ?.capitalize()
                        global_index_tv.setTextColor(util.getColorResFromIndex(it.global?.indice))
                        // SlimChart
                        it.global?.indice?.let { index ->
                            setSlimChart(slimchart_global, index)
                        }
                        it.pm10?.indice?.let { index ->
                            setSlimChart(slimchart_pm10, index)
                        }
                        it.no2?.indice?.let { index ->
                            setSlimChart(slimchart_no2, index)
                        }
                        it.o3?.indice?.let { index ->
                            setSlimChart(slimchart_o3, index)
                        }
                    }
                    else -> {
                        map_iv.setImageDrawable(context!!.getDrawable(R.drawable.baseline_highlight_off_24))
                        showError(context!!.getString(R.string.error_no_data))
                    }
                }
                map_iv.visibility = View.VISIBLE
                progress.visibility = View.GONE
            }
        }
        viewModel.pollutionEpisode.addObserver {
            if (it != null && context != null) {
                pollution_advice_tv.visibility = View.VISIBLE
            }
        }
    }

    override fun refresh() {
        hasError = false
        Log.d(AirQualityDetailsFragment::class.simpleName, "refresh() day=$day")
        displayLoading()
        snackbar?.dismiss()
        viewModel.fetchDayIndex(day!!)
        viewModel.fetchPollutionEpisode(day!!)
    }

    override fun showError(exception: CustomException) {
        context?.let {
            showError(getErrorMessage(it, exception))
        }
        Crashlytics.logException(exception)
    }

    private fun showError(message: String) {
        hasError = true
        context?.let {
            progress.visibility = View.GONE
            snackbar?.dismiss()
            snackbar = Snackbar.make(
                coordinator_layout,
                message,
                Snackbar.LENGTH_INDEFINITE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                snackbar!!.setBackgroundTint(resources.getColor(R.color.red_600, null))
            } else {
                snackbar!!.setBackgroundTint(resources.getColor(R.color.red_600))
            }
            snackbar!!.show()
            map_iv.setImageDrawable(it.getDrawable(R.drawable.baseline_highlight_off_24))
            map_iv.visibility = View.VISIBLE
        }
    }

    private fun setSlimChart(slimChart: SlimChart, index: Int) {
        val mIndex = minOf(90F, index * 0.72F)
        slimChart.stats = arrayOf(mIndex, 90F).toFloatArray()
        slimChart.colors = arrayOf(
            util.getColorResFromIndex(index),
            ContextCompat.getColor(context!!, R.color.very_light_grey)
        ).toIntArray()
        slimChart.setStartAnimationDuration(1700)
        slimChart.textColor = util.getColorFromIndex(index)
        slimChart.playStartAnimation()
        slimChart.visibility = View.VISIBLE
    }
}
