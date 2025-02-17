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
            // При первом запуске показываем PersonalDataTeacherFragment
            showFragment(PersonalDataTeacherFragment())
        } else {
            currentFragment = childFragmentManager.findFragmentById(R.id.fragmentContainer)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_personal_data -> {
                    // Каждый раз создаём новый экземпляр PersonalDataTeacherFragment,
                    // чтобы гарантировать его подгрузку с актуальными данными.
                    showFragment(PersonalDataTeacherFragment())
                    true
                }
                R.id.nav_grade -> {
                    // При выборе раздела "Выставить оценки" создаём новый экземпляр TeacherSubjectFragment.
                    // Это гарантирует, что фрагмент не является "удалённым" из FragmentManager.
                    showFragment(TeacherSubjectFragment())
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Функция для показа целевого фрагмента с заданной анимацией.
     *
     * При переходе к PersonalDataTeacherFragment (из любого оценочного фрагмента)
     * используется анимация перехода "влево" (новый фрагмент появляется слева, предыдущий уходит вправо).
     *
     * При переходе из PersonalDataTeacherFragment к оценочным фрагментам – анимация "вправо"
     * (новый фрагмент появляется справа, а PersonalDataTeacherFragment уходит влево).
     */
    private fun showFragment(fragment: Fragment) {
        // Если уже выбранный фрагмент совпадает с новым по типу – никаких изменений не требуется.
        if (currentFragment != null && currentFragment!!::class == fragment::class) return

        val transaction = childFragmentManager.beginTransaction()

        // Определяем анимацию перехода
        if (fragment is PersonalDataTeacherFragment &&
            (currentFragment is TeacherSubjectFragment)
        ) {
            transaction.setCustomAnimations(
                R.anim.slide_in_left,    // PersonalDataTeacherFragment появляется слева
                R.anim.slide_out_right   // Текущий оценочный фрагмент уходит вправо
            )
        } else if (currentFragment is PersonalDataTeacherFragment &&
            (fragment is TeacherSubjectFragment)
        ) {
            transaction.setCustomAnimations(
                R.anim.slide_in_right,   // Новый оценочный фрагмент появляется справа
                R.anim.slide_out_left    // PersonalDataTeacherFragment уходит влево
            )
        } else {
            // В остальных случаях используем стандартные анимации
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }

        // Используем replace, чтобы гарантировать полное создание представления нового фрагмента
        transaction.replace(R.id.fragmentContainer, fragment, fragment.javaClass.simpleName)
        transaction.commit()
        currentFragment = fragment
    }
}