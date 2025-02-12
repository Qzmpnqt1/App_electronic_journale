package com.example.electronic_journal.fragment.teacher

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.electronic_journal.R
import com.example.electronic_journal.adapter.StudentAdapter
import com.example.electronic_journal.databinding.FragmentStudentInGroupBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.autorization.GradeEntryRequest
import com.example.electronic_journal.server.model.GradeEntry
import com.example.electronic_journal.server.model.Student
import com.example.electronic_journal.server.service.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentInGroupFragment : Fragment(R.layout.fragment_student_in_group) {

    private lateinit var binding: FragmentStudentInGroupBinding
    private lateinit var apiService: ApiService
    private var groupId: Int = 0
    private var selectedStudent: Student? = null
    private var subjectId: Int = 0

    companion object {
        private const val ARG_GROUP_ID = "arg_group_id"
        private const val ARG_SUBJECT_ID = "arg_subject_id"

        // Создание нового экземпляра фрагмента с аргументами
        fun newInstance(groupId: Int, subjectId: Int): StudentInGroupFragment {
            val fragment = StudentInGroupFragment()
            val bundle = Bundle().apply {
                putInt(ARG_GROUP_ID, groupId)
                putInt(ARG_SUBJECT_ID, subjectId)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentInGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupId = arguments?.getInt(ARG_GROUP_ID) ?: return
        subjectId = arguments?.getInt(ARG_SUBJECT_ID) ?: return
        apiService = WebServerSingleton.getApiService(requireContext())

        // Запрашиваем студентов для группы
        apiService.getStudentsByGroupIdFromTeacher(groupId).enqueue(object : Callback<List<Student>> {
            override fun onResponse(call: Call<List<Student>>, response: Response<List<Student>>) {
                if (response.isSuccessful) {
                    loadStudents(response.body()!!)
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

        // Устанавливаем обработчик для кнопки сохранения
        binding.btSaveGrade.setOnClickListener {
            val selectedGrade = binding.spinnerGrades.selectedItem.toString().toIntOrNull()
            val student = selectedStudent

            if (student == null || selectedGrade == null) {
                Toast.makeText(context, "Выберите студента и оценку", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveGrade(student.studentId, subjectId, selectedGrade)
        }
    }

    private fun loadStudents(students: List<Student>) {
        val adapter = StudentAdapter(students) { student ->
            // При клике на студента обновляем выбранного студента
            selectedStudent = student
            val fullName = "${student.surname} ${student.name} ${student.patronymic ?: ""}".trim()
            binding.tvSelectedStudentName.text = "$fullName"
        }
        binding.rvStudentList.adapter = adapter
        binding.rvStudentList.layoutManager = LinearLayoutManager(requireContext())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveGrade(studentId: Int, subjectId: Int, grade: Int) {
        // Создаем объект GradeEntryRequest
        val gradeEntryRequest = GradeEntryRequest(
            studentId = studentId,
            subjectId = subjectId,
            grade = grade
        )

        // Передаем объект gradeEntryRequest в метод addGrade
        apiService.addGrade(gradeEntryRequest).enqueue(object : Callback<GradeEntry> {
            override fun onResponse(call: Call<GradeEntry>, response: Response<GradeEntry>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Оценка успешно сохранена", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Ошибка сохранения оценки", Toast.LENGTH_SHORT).show()
                    Log.e("SaveGrade", "Ошибка: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<GradeEntry>, t: Throwable) {
                Toast.makeText(context, "Оценка успешно сохранена", Toast.LENGTH_SHORT).show()
                Log.e("SaveGrade", "Ошибка сети: ${t.message}")
            }
        })
    }
}
