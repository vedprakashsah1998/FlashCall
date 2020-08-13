package com.infinty8.flashcall.adapteritem

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.infinty8.flashcall.fragment.HomeFragment
import com.infinty8.flashcall.fragment.ProfileFragment

class DemoFragmentStateAdapter (fm: FragmentManager) :
    FragmentStatePagerAdapter(fm)
{
    override fun getItem(position: Int): Fragment {

        return when (position) {
            0 -> HomeFragment.newInstance(
                "HomeFragment, Instance 1"
            )
            1 -> ProfileFragment.newInstance(
                "ProfileFragment, Instance1"
            )
            else -> HomeFragment.newInstance(
                "HomeFragment, Instance 1"
            )
        }!!

    }

    override fun getCount(): Int {
        return 2
    }

}