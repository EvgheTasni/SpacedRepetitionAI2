package com.example.spacedrepetition.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.spacedrepetition.R

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2
)

/**
 * A [FragmentStateAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> com.example.spacedrepetition.ui.topic.TopicListFragment()
            1 -> com.example.spacedrepetition.ui.interval.IntervalListFragment()
            else -> com.example.spacedrepetition.ui.topic.TopicListFragment()
        }
    }

    override fun getItemCount() = TAB_TITLES.size

    fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }
}