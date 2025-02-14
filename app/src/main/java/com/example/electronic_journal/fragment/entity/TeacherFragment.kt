package com.example.electronic_journal.fragment.entity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.electronic_journal.R
import com.example.electronic_journal.databinding.FragmentTeacherBinding
import com.example.electronic_journal.fragment.teacher.PersonalDataTeacherFragment
import com.example.electronic_journal.fragment.teacher.TeacherSubjectFragment

class TeacherFragment : Fragment() {

    private lateinit var binding: FragmentTeacherBinding
    private var currentFragment: Fragment? = null
    private val personalDataFragment = PersonalDataTeacherFragment()
    private val gradeFragment = TeacherSubjectFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTeacherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            showFragment(personalDataFragment)
        } else {
            currentFragment = childFragmentManager.findFragmentById(R.id.fragmentContainer)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_personal_data -> {
                    showFragment(personalDataFragment)
                    true
                }
                R.id.nav_grade -> {
                    showFragment(gradeFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        if (fragment == currentFragment) return

        val transaction = childFragmentManager.beginTransaction()
        currentFragment?.let { transaction.hide(it) }

        if (!fragment.isAdded) {
            transaction.add(R.id.fragmentContainer, fragment)
        } else {
            transaction.show(fragment)
        }

        transaction.commit()
        currentFragment = fragment
    }
}