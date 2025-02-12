package com.example.electronic_journal.fragment.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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

        // Получаем ApiService через WebServerSingleton
        apiService = WebServerSingleton.getApiService(requireContext())

        // Получение данных о текущем учителе
        apiService.getPersonalDataTeacher().enqueue(object : Callback<Teacher> {
            override fun onResponse(call: Call<Teacher>, response: Response<Teacher>) {
                if (response.isSuccessful) {
                    teacher = response.body()!!
                    loadSubjects(teacher.subjects)
                } else {
                    Toast.makeText(context, "Ошибка получения данных", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Teacher>, t: Throwable) {
                Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadSubjects(subjects: Set<Subject>) {
        // Очищаем контейнер перед добавлением
        binding.subjectsContainer.removeAllViews()

        // Для каждого предмета создаем карточку
        for (subject in subjects) {
            val cardView = layoutInflater.inflate(R.layout.item_subject_card, binding.subjectsContainer, false)

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
        }
    }
}
