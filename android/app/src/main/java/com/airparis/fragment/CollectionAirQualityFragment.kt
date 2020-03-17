package com.airparis.fragment

import airparis.data.http.model.util.Day
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.airparis.R
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_air_quality_collection.*

class CollectionAirQualityFragment : Fragment() {

    // When requested, this adapter returns a AirQualityDetailsFragment,
    // representing an object in the collection.
    private lateinit var airQualityCollectionAdapter: AirQualityCollectionAdapter
    private lateinit var viewPager: ViewPager2
    private var tabIndex: Int = 1
    private val TAB_ARG = "tab"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        savedInstanceState?.let {
            tabIndex = it.getInt(TAB_ARG, 1)
        }
        return inflater.inflate(R.layout.fragment_air_quality_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        airQualityCollectionAdapter = AirQualityCollectionAdapter(this)
        viewPager = pager
        viewPager.adapter = airQualityCollectionAdapter
        TabLayoutMediator(tab_layout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.yesterday)
                1 -> getString(R.string.today)
                2 -> getString(R.string.tomorrow)
                else -> null
            }
        }.attach()
        tab_layout.getTabAt(tabIndex)?.select()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putInt(TAB_ARG, tab_layout.selectedTabPosition)
        }
    }
}