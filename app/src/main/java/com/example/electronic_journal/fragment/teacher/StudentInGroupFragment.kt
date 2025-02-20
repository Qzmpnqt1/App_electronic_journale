package com.example.electronic_journal.fragment.teacher

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.example.electronic_journal.R
import com.example.electronic_journal.databinding.FragmentStudentInGroupBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.model.Student
import com.example.electronic_journal.server.service.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentInGroupFragment : Fragment(R.layout.fragment_student_in_group) {

    private lateinit var binding: FragmentStudentInGroupBinding
    private lateinit var apiService: ApiService
    private var groupId: Int = 0
    private var subjectId: Int = 0
    private var subjectName: String = ""
    private var selectedStudent: Student? = null

    // Храним полный список студентов для поиска
    private var allStudents: List<Student> = emptyList()
    private var searchQuery: String = ""

    companion object {
        private const val ARG_GROUP_ID = "arg_group_id"
        private const val ARG_SUBJECT_ID = "arg_subject_id"
        private const val ARG_SUBJECT_NAME = "arg_subject_name"

        // Создание нового экземпляра фрагмента с передачей subjectName
        fun newInstance(groupId: Int, subjectId: Int, subjectName: String): StudentInGroupFragment {
            val fragment = StudentInGroupFragment()
            val bundle = Bundle().apply {
                putInt(ARG_GROUP_ID, groupId)
                putInt(ARG_SUBJECT_ID, subjectId)
                putString(ARG_SUBJECT_NAME, subjectName)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStudentInGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupId = arguments?.getInt(ARG_GROUP_ID) ?: return
        subjectId = arguments?.getInt(ARG_SUBJECT_ID) ?: return
        subjectName = arguments?.getString(ARG_SUBJECT_NAME) ?: ""
        apiService = WebServerSingleton.getApiService(requireContext())

        // Восстанавливаем состояние поиска, если оно было сохранено
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("SEARCH_QUERY", "")
            binding.searchInputLayout.editText?.setText(searchQuery)
        }

        // Добавляем TextWatcher для обновления переменной searchQuery
        binding.searchInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
            }
            override fun afterTextChanged(s: Editable?) { }
        })

        // Обработчик нажатия на иконку поиска
        binding.searchInputLayout.setEndIconOnClickListener {
            if (searchQuery.isNotEmpty()) {
                searchStudent(searchQuery)
            } else {
                Toast.makeText(context, "Введите имя студента", Toast.LENGTH_SHORT).show()
            }
        }

        // Обработчик кнопки "Назад"
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnReload.setOnClickListener {
            loadStudents(allStudents)
        }

        // Запрашиваем студентов для группы
        apiService.getStudentsByGroupIdFromTeacher(groupId).enqueue(object : Callback<List<Student>> {
            override fun onResponse(call: Call<List<Student>>, response: Response<List<Student>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        allStudents = it
                        loadStudents(allStudents)
                    }
                } else {
                    Log.e("Students", "Ошибка: ${response.code()} - ${response.message()}")
                    Toast.makeText(context, "Ошибка получения студентов", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Student>>, t: Throwable) {
                Log.e("Students", "Ошибка сети: ${t.message}")
                Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SEARCH_QUERY", searchQuery)
    }

    private fun loadStudents(students: List<Student>) {
        // Сортируем студентов по фамилии
        val sortedStudents = students.sortedBy { it.surname }
        // Очищаем контейнер
        binding.studentsContainer.removeAllViews()

        // Добавляем карточки для каждого студента
        for ((index, student) in sortedStudents.withIndex()) {
            val studentCard = layoutInflater.inflate(R.layout.item_student, binding.studentsContainer, false)
            if (studentCard.id == View.NO_ID) {
                studentCard.id = View.generateViewId()
            }
            val tvStudentName = studentCard.findViewById<TextView>(R.id.tvStudentName)
            val tvStudentID = studentCard.findViewById<TextView>(R.id.tvStudentID)
            val fullName = "${student.surname} ${student.name} ${student.patronymic ?: ""}".trim()
            tvStudentName.text = fullName
            tvStudentID.text = "ID: ${student.studentId}"

            // При клике переходим на фрагмент выставления оценок, передавая полное ФИО и название предмета
            studentCard.setOnClickListener {
                val setGradeFragment = TeacherSetGradeFragment.newInstance(
                    student.studentId,
                    subjectId,
                    fullName,
                    subjectName
                )
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(R.id.fragmentContainer, setGradeFragment)
                    .addToBackStack(null)
                    .commit()
            }

            binding.studentsContainer.addView(studentCard)
        }

        // Устанавливаем ограничения для вертикальной цепочки карточек
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.studentsContainer)
        val marginHorizontalDp = 8
        val scale = resources.displayMetrics.density
        val marginHorizontalPx = (marginHorizontalDp * scale + 0.5f).toInt()

        for (i in 0 until binding.studentsContainer.childCount) {
            val studentCard = binding.studentsContainer.getChildAt(i)
            constraintSet.connect(studentCard.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, marginHorizontalPx)
            constraintSet.connect(studentCard.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, marginHorizontalPx)
            if (i == 0) {
                constraintSet.connect(studentCard.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
            } else {
                val previousCard = binding.studentsContainer.getChildAt(i - 1)
                val marginVerticalDp = 8
                val marginVerticalPx = (marginVerticalDp * scale + 0.5f).toInt()
                constraintSet.connect(studentCard.id, ConstraintSet.TOP, previousCard.id, ConstraintSet.BOTTOM, marginVerticalPx)
            }
        }
        constraintSet.applyTo(binding.studentsContainer)
    }

    private fun searchStudent(query: String) {
        val filteredStudents = allStudents.filter { student ->
            val fullName = "${student.surname} ${student.name} ${student.patronymic ?: ""}".trim()
            fullName.contains(query, ignoreCase = true)
        }
        if (filteredStudents.isNotEmpty()) {
            loadStudents(filteredStudents)
        } else {
            Toast.makeText(context, "Студент не найден", Toast.LENGTH_SHORT).show()
        }
    }
}
