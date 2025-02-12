package com.example.electronic_journal.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.electronic_journal.R
import com.example.electronic_journal.adapter.SubjectAdapter
import com.example.electronic_journal.databinding.FragmentSignUpBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.autorization.StudentRegistrationRequest
import com.example.electronic_journal.server.autorization.TeacherSignUpRequest
import com.example.electronic_journal.server.model.Group
import com.example.electronic_journal.server.model.Subject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var subjectAdapter: SubjectAdapter
    private val selectedSubjects = mutableListOf<Subject>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSubjects.visibility = View.GONE
        binding.rvSubjects.visibility = View.GONE
        binding.edDateOfBirth.visibility = View.VISIBLE
        binding.tvGroupDetails.visibility = View.VISIBLE
        binding.edGroupId.visibility = View.VISIBLE

        setupRecyclerView()
        loadSubjects()
        loadGroups()

        binding.btSignUp.setOnClickListener {
            val name = binding.edName.text.toString().trim()
            val surname = binding.edSurname.text.toString().trim()
            val patronymic = binding.edPatronymic.text.toString().trim()
            val email = binding.edEmail.text.toString().trim()
            val password = binding.edPassword.text.toString().trim()

            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when {
                binding.rbStudent.isChecked -> {
                    val dateOfBirth = binding.edDateOfBirth.text.toString().trim()
                    val groupIdText = binding.edGroupId.text.toString().trim()

                    if (dateOfBirth.isEmpty() || groupIdText.isEmpty()) {
                        Toast.makeText(context, "Пожалуйста, заполните все поля для студента", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val groupId = groupIdText.toIntOrNull()
                    if (groupId == null) {
                        Toast.makeText(context, "ID группы должно быть числом", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val request = StudentRegistrationRequest(
                        name = name,
                        surname = surname,
                        patronymic = patronymic,
                        dateOfBirth = dateOfBirth,
                        email = email,
                        password = password,
                        groupId = groupId
                    )

                    WebServerSingleton.getApiService(requireContext()).registerStudent(request)
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Регистрация успешна", Toast.LENGTH_SHORT).show()
                                    navigateToFragment(R.id.authorizationFragment)
                                } else {
                                    val errorMessage = response.errorBody()?.string()
                                    Toast.makeText(context, "Ошибка регистрации: $errorMessage", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(context, "Ошибка соединения: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                }

                binding.rbTeacher.isChecked -> {
                    if (selectedSubjects.isEmpty()) {
                        Toast.makeText(context, "Пожалуйста, выберите хотя бы один предмет", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val subjectIds = selectedSubjects.map { it.subjectId }
                    val request = TeacherSignUpRequest(
                        name = name,
                        surname = surname,
                        patronymic = patronymic,
                        email = email,
                        password = password,
                        subjectIds = subjectIds
                    )

                    WebServerSingleton.getApiService(requireContext()).registerTeacher(request)
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Регистрация успешна", Toast.LENGTH_SHORT).show()
                                    navigateToFragment(R.id.authorizationFragment)
                                } else {
                                    val errorMessage = response.errorBody()?.string()
                                    Toast.makeText(context, "Ошибка регистрации: $errorMessage", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(context, "Ошибка соединения: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }
        }

        binding.txAutorization.setOnClickListener {
            navigateToFragment(R.id.authorizationFragment)
        }

        binding.rgUserType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbStudent -> {
                    binding.tvSubjects.visibility = View.GONE
                    binding.rvSubjects.visibility = View.GONE
                    binding.edDateOfBirth.visibility = View.VISIBLE
                    binding.tvGroupDetails.visibility = View.VISIBLE
                    binding.edGroupId.visibility = View.VISIBLE
                }
                R.id.rbTeacher -> {
                    binding.edDateOfBirth.visibility = View.GONE
                    binding.tvGroupDetails.visibility = View.GONE
                    binding.edGroupId.visibility = View.GONE
                    binding.tvSubjects.visibility = View.VISIBLE
                    binding.rvSubjects.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadGroups() {
        WebServerSingleton.getApiService(requireContext()).getAllGroups().enqueue(object : Callback<List<Group>> {
            override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {
                if (response.isSuccessful) {
                    val groups = response.body()
                    if (groups != null) {
                        val sortedGroups = groups.sortedBy { it.name }
                        val groupDetails = StringBuilder()
                        for (group in sortedGroups) {
                            groupDetails.append("ID: ${group.groupId}, Название: ${group.name}\n")
                        }
                        binding.tvGroupDetails.text = groupDetails.toString()
                    } else {
                        binding.tvGroupDetails.text = "Нет доступных групп"
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

    private fun loadSubjects() {
        WebServerSingleton.getApiService(requireContext()).getAllSubjects().enqueue(object : Callback<List<Subject>> {
            override fun onResponse(call: Call<List<Subject>>, response: Response<List<Subject>>) {
                if (response.isSuccessful) {
                    val subjects = response.body()
                    if (subjects != null) {
                        subjectAdapter.updateSubjects(subjects)
                    }
                } else {
                    Toast.makeText(context, "Ошибка загрузки предметов", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Subject>>, t: Throwable) {
                Toast.makeText(context, "Ошибка соединения", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView() {
        subjectAdapter = SubjectAdapter(mutableListOf()) { subject, isSelected ->
            if (isSelected) {
                selectedSubjects.add(subject)
            } else {
                selectedSubjects.remove(subject)
            }
        }
        binding.rvSubjects.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = subjectAdapter
        }
    }

    private fun navigateToFragment(fragmentId: Int) {
        findNavController().navigate(fragmentId)
    }
}

