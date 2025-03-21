package com.example.electronic_journal.fragment.entity

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.electronic_journal.R
import com.example.electronic_journal.adapter.SubjectAdapter
import com.example.electronic_journal.databinding.FragmentAdministratorBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.autorization.GroupDTO
import com.example.electronic_journal.server.autorization.SubjectDTO
import com.example.electronic_journal.server.model.Group
import com.example.electronic_journal.server.model.Student
import com.example.electronic_journal.server.model.Subject
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdministratorFragment : Fragment() {

    private lateinit var binding: FragmentAdministratorBinding
    private lateinit var subjectAdapter: SubjectAdapter
    private val selectedSubjects = mutableListOf<Subject>()

    // Кэш для загруженных данных
    private var cachedSubjects: List<Subject>? = null
    private var cachedGroups: List<Group>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdministratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // Если предметы уже загружены, используем кэш, иначе загружаем с сервера
        if (cachedSubjects == null) {
            loadSubjects()
        } else {
            subjectAdapter.updateSubjects(cachedSubjects!!)
            binding.tvSubjectDetails.text = cachedSubjects!!.joinToString("\n") {
                "ID: ${it.subjectId}, Название: ${it.name}, Курс: ${it.course}"
            }
        }
        if (cachedGroups == null) {
            loadGroups()
        } else {
            val groupDetails = cachedGroups!!.sortedBy { it.name }
                .joinToString("\n") { "ID: ${it.groupId}, Название: ${it.name}" }
            binding.tvGroupDetails.text = groupDetails
            binding.tvGroupList.text = groupDetails
        }

        binding.btLogout.setOnClickListener {
            confirmLogout() // Диалог подтверждения выхода
        }

        binding.btChangeTheme?.setOnClickListener {
            val newMode = if ((requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.MODE_NIGHT_NO
            } else {
                AppCompatDelegate.MODE_NIGHT_YES
            }
            AppCompatDelegate.setDefaultNightMode(newMode)
            // Сохранение выбранного режима
            val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPref.edit().putInt("theme_mode", newMode).apply()

            requireActivity().recreate()
        }

        binding.btAddGroup.setOnClickListener {
            val groupName = binding.edGroupName.text.toString().trim()
            if (groupName.isEmpty()) {
                Toast.makeText(context, "Введите название группы", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedSubjects.isEmpty()) {
                Toast.makeText(context, "Выберите хотя бы один предмет", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val subjectIds = selectedSubjects.map { it.subjectId }
            val groupDTO = GroupDTO(name = groupName, subjectIds = subjectIds)
            WebServerSingleton.getApiService(requireContext()).addGroup(groupDTO)
                .enqueue(object : Callback<Group> {
                    override fun onResponse(call: Call<Group>, response: Response<Group>) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Группа добавлена", Toast.LENGTH_SHORT).show()
                            loadGroups() // Обновляем группы (и кэш)
                        } else {
                            Toast.makeText(context, "Ошибка добавления группы", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Group>, t: Throwable) {
                        Toast.makeText(context, "Ошибка соединения", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.btDeleteGroup.setOnClickListener {
            val groupIdStr = binding.edDeleteGroupName.text.toString().trim()
            if (groupIdStr.isEmpty()) {
                Toast.makeText(context, "Введите ID группы для удаления", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val groupIdInt = groupIdStr.toIntOrNull()
            if (groupIdInt == null) {
                Toast.makeText(context, "Введите корректный числовой ID группы", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            WebServerSingleton.getApiService(requireContext()).deleteGroup(groupIdInt)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Группа успешно удалена", Toast.LENGTH_SHORT).show()
                            loadGroups()
                        } else if (response.code() == 400) {
                            Toast.makeText(context, "Невозможно удалить группу, в ней есть студенты", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Ошибка удаления группы", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(context, "Ошибка соединения", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.btAddSubject.setOnClickListener {
            val subjectName = binding.edSubjectName.text.toString().trim()
            val subjectCourse = binding.edSubjectCourse.text.toString().trim()
            if (subjectName.isEmpty() || subjectCourse.isEmpty()) {
                Toast.makeText(context, "Введите название и курс предмета", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val course = subjectCourse.toIntOrNull()
            if (course == null || course !in 1..4) {
                Toast.makeText(context, "Курс должен быть числом от 1 до 4", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val subjectDTO = SubjectDTO(name = subjectName, course = course)
            WebServerSingleton.getApiService(requireContext()).addSubject(subjectDTO)
                .enqueue(object : Callback<Subject> {
                    override fun onResponse(call: Call<Subject>, response: Response<Subject>) {
                        if (response.isSuccessful) {
                            val addedSubject = response.body()
                            if (addedSubject != null) {
                                Toast.makeText(context, "Предмет добавлен: ${addedSubject.name}", Toast.LENGTH_SHORT).show()
                                loadSubjects()
                            }
                        } else if (response.code() == 400) {
                            val errorMessage = response.errorBody()?.string()
                            Toast.makeText(context, errorMessage ?: "Ошибка добавления предмета", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Ошибка добавления предмета", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Subject>, t: Throwable) {
                        Toast.makeText(context, "Ошибка соединения", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.btDeleteSubject.setOnClickListener {
            val subjectIdStr = binding.edDeleteSubjectName.text.toString().trim()
            if (subjectIdStr.isEmpty()) {
                Toast.makeText(context, "Введите ID предмета для удаления", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val subjectIdInt = subjectIdStr.toIntOrNull()
            if (subjectIdInt == null) {
                Toast.makeText(context, "Введите корректный числовой ID предмета", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            WebServerSingleton.getApiService(requireContext()).deleteSubject(subjectIdInt)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Предмет удалён", Toast.LENGTH_SHORT).show()
                            loadSubjects()
                        } else {
                            Toast.makeText(context, "Ошибка удаления предмета", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(context, "Ошибка соединения", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.btLoadStudents.setOnClickListener {
            val groupIdStr = binding.edGroupIdForStudents.text.toString().trim()
            if (groupIdStr.isEmpty()) {
                Toast.makeText(context, "Введите ID группы", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val groupIdInt = groupIdStr.toIntOrNull()
            if (groupIdInt == null) {
                Toast.makeText(context, "Введите корректный числовой ID группы", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            WebServerSingleton.getApiService(requireContext()).getStudentsByGroupId(groupIdInt)
                .enqueue(object : Callback<List<Student>> {
                    override fun onResponse(call: Call<List<Student>>, response: Response<List<Student>>) {
                        if (response.isSuccessful) {
                            val students = response.body()
                            if (!students.isNullOrEmpty()) {
                                val sortedStudents = students.sortedBy { it.surname }
                                val studentDetails = sortedStudents.joinToString("\n") {
                                    "ID: ${it.studentId}, ${it.surname} ${it.name}"
                                }
                                binding.tvStudentList.text = studentDetails
                            } else {
                                binding.tvStudentList.text = "Нет студентов в этой группе"
                            }
                        } else {
                            Toast.makeText(context, "Ошибка загрузки студентов", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<List<Student>>, t: Throwable) {
                        Toast.makeText(context, "Ошибка соединения", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.btDeleteStudent.setOnClickListener {
            val studentIdStr = binding.edStudentIdToDelete.text.toString().trim()
            if (studentIdStr.isEmpty()) {
                Toast.makeText(context, "Введите ID студента для удаления", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val studentIdInt = studentIdStr.toIntOrNull()
            if (studentIdInt == null) {
                Toast.makeText(context, "Введите корректный числовой ID студента", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            WebServerSingleton.getApiService(requireContext()).removeStudentFromGroup(studentIdInt)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Студент удалён из группы", Toast.LENGTH_SHORT).show()
                            binding.btLoadStudents.performClick()
                        } else {
                            Toast.makeText(context, "Ошибка удаления студента", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(context, "Ошибка соединения", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.tvSearch?.setOnClickListener {
            navigateToFragment(R.id.administratorSearchFragment, getSearchNavOptions())
        }
    }

    private fun loadSubjects() {
        WebServerSingleton.getApiService(requireContext()).getAllSubjects().enqueue(object : Callback<List<Subject>> {
            override fun onResponse(call: Call<List<Subject>>, response: Response<List<Subject>>) {
                if (response.isSuccessful) {
                    val subjects = response.body()
                    if (subjects != null) {
                        cachedSubjects = subjects // кэшируем предметы
                        subjectAdapter.updateSubjects(subjects)
                        val subjectDetails = subjects.joinToString("\n") {
                            "ID: ${it.subjectId}, Название: ${it.name}, Курс: ${it.course}"
                        }
                        binding.tvSubjectDetails.text = subjectDetails
                    } else {
                        binding.tvSubjectDetails.text = "Нет доступных предметов"
                    }
                } else {
                    Log.e("loadSubjects", "Ошибка: ${response.code()} - ${response.message()}")
                    Toast.makeText(context, "Ошибка загрузки предметов", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Subject>>, t: Throwable) {
                Log.e("loadSubjects", "Ошибка соединения: ${t.message}")
                Toast.makeText(context, "Ошибка соединения", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadGroups() {
        WebServerSingleton.getApiService(requireContext()).getAllGroups().enqueue(object : Callback<List<Group>> {
            override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {
                if (response.isSuccessful) {
                    val groups = response.body()
                    if (groups != null) {
                        cachedGroups = groups // кэшируем группы
                        val sortedGroups = groups.sortedBy { it.name }
                        val groupDetails = sortedGroups.joinToString("\n") {
                            "ID: ${it.groupId}, Название: ${it.name}"
                        }
                        binding.tvGroupDetails.text = groupDetails
                        binding.tvGroupList.text = groupDetails
                    } else {
                        binding.tvGroupDetails.text = "Нет доступных групп"
                        binding.tvGroupList.text = "Нет доступных групп"
                    }
                } else {
                    Log.e("loadGroups", "Ошибка: ${response.code()} - ${response.message()}")
                    Toast.makeText(context, "Ошибка загрузки групп", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Group>>, t: Throwable) {
                Log.e("loadGroups", "Ошибка соединения: ${t.message}")
                Toast.makeText(context, "Ошибка соединения", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView() {
        subjectAdapter = SubjectAdapter(mutableListOf()) { subject, isSelected ->
            if (isSelected) selectedSubjects.add(subject) else selectedSubjects.remove(subject)
        }
        binding.rvSubjects.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = subjectAdapter
        }
    }

    private fun getNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_left)
            .setExitAnim(R.anim.slide_out_right)
            .setPopEnterAnim(R.anim.slide_in_right)
            .setPopExitAnim(R.anim.slide_out_left)
            .build()
    }

    private fun getSearchNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()
    }

    private fun navigateToFragment(fragmentId: Int, navOptions: NavOptions) {
        findNavController().navigate(fragmentId, null, navOptions)
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Подтверждение выхода")
            .setMessage("Вы уверены, что хотите выйти из аккаунта?")
            .setPositiveButton("Да") { _, _ ->
                logout()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun logout() {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("token")
            remove("role")
            apply()
        }
        navigateToFragment(R.id.authorizationFragment, getNavOptions())
    }
}
