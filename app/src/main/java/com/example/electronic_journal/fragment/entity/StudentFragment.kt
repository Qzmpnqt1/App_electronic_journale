package com.example.electronic_journal.fragment.entity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.electronic_journal.R
import com.example.electronic_journal.databinding.FragmentStudentBinding
//import com.example.electronic_journal.fragment.student.GradebookFragment
import com.example.electronic_journal.fragment.student.PersonalDataStudentFragment

class StudentFragment : Fragment() {

    private lateinit var binding: FragmentStudentBinding
    private var currentFragment: Fragment? = null
    private val personalDataFragment = PersonalDataStudentFragment()
    //private val gradebookFragment = GradebookFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStudentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomNavigation()

        // Восстановление фрагмента при повороте экрана
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
                R.id.nav_gradebook -> {
                    //showFragment(gradebookFragment)
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
