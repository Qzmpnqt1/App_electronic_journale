package com.example.electronic_journal.fragment.teacher

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.electronic_journal.R
import com.example.electronic_journal.databinding.FragmentTeacherSetGradeBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.autorization.GradeEntryRequest
import com.example.electronic_journal.server.model.GradeEntry
import com.example.electronic_journal.server.service.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class TeacherSetGradeFragment : Fragment(R.layout.fragment_teacher_set_grade) {

    private lateinit var binding: FragmentTeacherSetGradeBinding
    private lateinit var apiService: ApiService

    private var studentId: Int = 0
    private var subjectId: Int = 0

    // Дополнительные поля для отображения в интерфейсе
    private var studentFullName: String = ""
    private var subjectName: String = ""

    companion object {
        private const val ARG_STUDENT_ID = "arg_student_id"
        private const val ARG_SUBJECT_ID = "arg_subject_id"
        private const val ARG_STUDENT_FULL_NAME = "arg_student_full_name"
        private const val ARG_SUBJECT_NAME = "arg_subject_name"

        fun newInstance(
            studentId: Int,
            subjectId: Int,
            studentFullName: String,
            subjectName: String
        ): TeacherSetGradeFragment {
            val fragment = TeacherSetGradeFragment()
            val args = Bundle().apply {
                putInt(ARG_STUDENT_ID, studentId)
                putInt(ARG_SUBJECT_ID, subjectId)
                putString(ARG_STUDENT_FULL_NAME, studentFullName)
                putString(ARG_SUBJECT_NAME, subjectName)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTeacherSetGradeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = WebServerSingleton.getApiService(requireContext())

        // Получаем аргументы
        studentId = arguments?.getInt(ARG_STUDENT_ID) ?: 0
        subjectId = arguments?.getInt(ARG_SUBJECT_ID) ?: 0
        studentFullName = arguments?.getString(ARG_STUDENT_FULL_NAME) ?: ""
        subjectName = arguments?.getString(ARG_SUBJECT_NAME) ?: ""

        // Отображаем ФИО студента и название предмета
        val infoText = "Студент: $studentFullName\nПредмет: $subjectName"
        binding.tvStudentInfo.text = infoText

        // Настраиваем выпадающий список (Exposed Dropdown Menu) с оценками [2, 3, 4, 5]
        val gradesArray = listOf("2", "3", "4", "5")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, gradesArray)
        binding.autoCompleteGrade.setAdapter(adapter)

        // Обработка нажатия "Сохранить"
        binding.btnSaveGrade.setOnClickListener {
            // Проверяем дату (сессии)
            val today = LocalDate.now()
            val isWinterSession = (today.monthValue == 1 && today.dayOfMonth in 9..31)
            val isSummerSession = (today.monthValue == 6 && today.dayOfMonth in 5..30)

            if (!isWinterSession && !isSummerSession) {
                Toast.makeText(
                    context,
                    "Оценка может быть выставлена только в период сессии: зимняя (9-31 января) или летняя (5-30 июня)",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val selectedGradeStr = binding.autoCompleteGrade.text.toString()
            val selectedGrade = selectedGradeStr.toIntOrNull() ?: 0
            if (selectedGrade !in 2..5) {
                Toast.makeText(context, "Некорректная оценка", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveGrade(studentId, subjectId, selectedGrade)
        }

        // Обработка нажатия "Назад"
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveGrade(studentId: Int, subjectId: Int, grade: Int) {
        val gradeEntryRequest = GradeEntryRequest(
            studentId = studentId,
            subjectId = subjectId,
            grade = grade
        )
        apiService.addGrade(gradeEntryRequest).enqueue(object : Callback<GradeEntry> {
            override fun onResponse(call: Call<GradeEntry>, response: Response<GradeEntry>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Оценка успешно выставлена", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "Ошибка сохранения оценки", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GradeEntry>, t: Throwable) {
                Toast.makeText(context, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
