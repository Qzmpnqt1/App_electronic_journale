package com.example.electronic_journal.fragment.entity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.electronic_journal.R
import com.example.electronic_journal.databinding.FragmentStudentBinding
import com.example.electronic_journal.fragment.student.PersonalDataStudentFragment
import com.example.electronic_journal.fragment.student.GradebookFragment

class StudentFragment : Fragment() {

    private lateinit var binding: FragmentStudentBinding
    private var currentFragment: Fragment? = null
    private val personalDataFragment = PersonalDataStudentFragment()
    private val gradebookFragment = GradebookFragment()

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

        // При первом запуске показываем личные данные
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
                    showFragment(gradebookFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        // Если уже отображается нужный фрагмент, ничего не делаем
        if (fragment == currentFragment) return

        val transaction = childFragmentManager.beginTransaction()

        // Определяем анимацию перехода в зависимости от текущего и нового фрагмента
        if (fragment is PersonalDataStudentFragment && currentFragment is GradebookFragment) {
            // Переход с зачетки к личным данным – анимация появления слева
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
        } else if (fragment is GradebookFragment && currentFragment is PersonalDataStudentFragment) {
            // Переход от личных данных к зачетке – анимация появления справа
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            // По умолчанию
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Используем replace, чтобы гарантировать создание нового экземпляра (с анимацией)
        transaction.replace(R.id.fragmentContainer, fragment, fragment.javaClass.simpleName)
        transaction.commit()
        currentFragment = fragment
    }
}
