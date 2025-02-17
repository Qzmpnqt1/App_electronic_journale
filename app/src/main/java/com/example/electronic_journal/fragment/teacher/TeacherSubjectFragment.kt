package com.example.electronic_journal.fragment.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.example.electronic_journal.R
import com.example.electronic_journal.databinding.FragmentTeacherSubjectBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.model.Subject
import com.example.electronic_journal.server.model.Teacher
import com.example.electronic_journal.server.service.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TeacherSubjectFragment : Fragment(R.layout.fragment_teacher_subject) {

    private lateinit var binding: FragmentTeacherSubjectBinding
    private lateinit var apiService: ApiService
    private lateinit var teacher: Teacher
    private var allSubjects: Set<Subject> = emptySet()
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инициализация binding
        binding = FragmentTeacherSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Восстанавливаем состояние поиска
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("SEARCH_QUERY", "")
            binding.searchInputLayout.editText?.setText(searchQuery)
        }

        // Получаем ApiService через WebServerSingleton
        apiService = WebServerSingleton.getApiService(requireContext())

        // Получение данных о текущем учителе
        apiService.getPersonalDataTeacher().enqueue(object : Callback<Teacher> {
            override fun onResponse(call: Call<Teacher>, response: Response<Teacher>) {
                if (response.isSuccessful) {
                    teacher = response.body()!!
                    allSubjects = teacher.subjects
                    loadSubjects(allSubjects)
                } else {
                    Toast.makeText(context, "Ошибка получения данных", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Teacher>, t: Throwable) {
                Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        })

        // Устанавливаем обработчик нажатия на иконку поиска
        binding.searchInputLayout.setEndIconOnClickListener {
            searchQuery = binding.searchInputLayout.editText?.text.toString().trim()
            if (searchQuery.isNotEmpty()) {
                searchSubject(searchQuery)
            } else {
                Toast.makeText(context, "Введите название предмета", Toast.LENGTH_SHORT).show()
            }
        }

        // Устанавливаем обработчик нажатия на кнопку перезагрузки
        binding.btnReload.setOnClickListener {
            loadSubjects(allSubjects)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем текущий запрос поиска
        outState.putString("SEARCH_QUERY", searchQuery)
    }

    private fun loadSubjects(subjects: Set<Subject>) {
        // Очищаем контейнер перед добавлением
        binding.subjectsContainer.removeAllViews()

        val constraintSet = ConstraintSet()

        // Для каждого предмета создаем карточку
        for ((index, subject) in subjects.withIndex()) {
            val cardView = layoutInflater.inflate(R.layout.item_subject_card, binding.subjectsContainer, false)
            cardView.id = View.generateViewId()

            val subjectNameTextView = cardView.findViewById<TextView>(R.id.tvSubjectName)
            subjectNameTextView.text = subject.name

            // Обработка нажатия на карточку
            cardView.setOnClickListener {
                // Переход к фрагменту с группами для выбранного предмета
                val groupsFragment = GroupsStudyingSubjectFragment.newInstance(subject)
                fragmentManager?.beginTransaction()
                    ?.replace(R.id.fragmentContainer, groupsFragment)
                    ?.commit()
            }

            binding.subjectsContainer.addView(cardView)

            // Устанавливаем ограничения для карточки
            constraintSet.clone(binding.subjectsContainer)
            if (index == 0) {
                constraintSet.connect(cardView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            } else {
                val previousCardId = binding.subjectsContainer.getChildAt(index - 1).id
                constraintSet.connect(cardView.id, ConstraintSet.TOP, previousCardId, ConstraintSet.BOTTOM)
            }
            constraintSet.applyTo(binding.subjectsContainer)
        }
    }

    private fun searchSubject(query: String) {
        val foundSubject = allSubjects.find { it.name.contains(query, ignoreCase = true) }
        if (foundSubject != null) {
            loadSubjects(setOf(foundSubject))
        } else {
            Toast.makeText(context, "Предмет не найден", Toast.LENGTH_SHORT).show()
        }
    }
}
