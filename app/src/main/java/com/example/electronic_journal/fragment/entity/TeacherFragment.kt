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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeacherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(PersonalDataTeacherFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_personal_data -> {
                    loadFragment(PersonalDataTeacherFragment())
                    true
                }
                R.id.nav_grade -> {
                    loadFragment(TeacherSubjectFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}

