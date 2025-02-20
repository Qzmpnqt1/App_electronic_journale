package com.example.electronic_journal.fragment.teacher

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        // Добавляем TextWatcher для обновления переменной searchQuery при изменении текста
        binding.searchInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
            }
            override fun afterTextChanged(s: Editable?) { }
        })

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

        // Обработчик нажатия на иконку поиска
        binding.searchInputLayout.setEndIconOnClickListener {
            // Теперь searchQuery уже обновлён через TextWatcher
            if (searchQuery.isNotEmpty()) {
                searchSubject(searchQuery)
            } else {
                Toast.makeText(context, "Введите название предмета", Toast.LENGTH_SHORT).show()
            }
        }

        // Обработчик нажатия на кнопку перезагрузки
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
            // Используем новый layout для универсальной карточки
            val cardView = layoutInflater.inflate(R.layout.item_name_card, binding.subjectsContainer, false)
            cardView.id = View.generateViewId()

            // Получаем универсальный TextView и заполняем название предмета
            val nameTextView = cardView.findViewById<TextView>(R.id.tvName)
            nameTextView.text = subject.name

            // Обработка нажатия на карточку
            cardView.setOnClickListener {
                val groupsFragment = GroupsStudyingSubjectFragment.newInstance(subject)
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(R.id.fragmentContainer, groupsFragment)
                    .addToBackStack(null)
                    .commit()
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

    // Реализация поиска с использованием filter для выбора всех подходящих предметов
    private fun searchSubject(query: String) {
        val foundSubjects = allSubjects.filter { it.name.contains(query, ignoreCase = true) }
        if (foundSubjects.isNotEmpty()) {
            loadSubjects(foundSubjects.toSet())
        } else {
            Toast.makeText(context, "Предмет не найден", Toast.LENGTH_SHORT).show()
        }
    }
}