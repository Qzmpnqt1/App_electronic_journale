package com.example.electronic_journal.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.electronic_journal.R
import com.example.electronic_journal.adapter.SubjectAdapter
import com.example.electronic_journal.databinding.FragmentSignUpBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.autorization.EmailVerificationRequest
import com.example.electronic_journal.server.autorization.StudentRegistrationRequest
import com.example.electronic_journal.server.autorization.TeacherSignUpRequest
import com.example.electronic_journal.server.model.Group
import com.example.electronic_journal.server.model.Subject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.datepicker.MaterialDatePicker

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var subjectAdapter: SubjectAdapter
    private val selectedSubjects = mutableListOf<Subject>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // По умолчанию показываем поля для студента
        showStudentFields()

        setupRecyclerView()
        loadSubjects()
        loadGroups()

        // Обработчик клика для поля "Дата рождения" – открывает календарь
        binding.edDateOfBirth.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
            builder.setTitleText("Выберите дату рождения")
            val picker = builder.build()
            picker.show(childFragmentManager, "MATERIAL_DATE_PICKER")
            picker.addOnPositiveButtonClickListener { selection ->
                val selectedDate = java.time.Instant.ofEpochMilli(selection)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                val currentDate = java.time.LocalDate.now()
                val age = java.time.Period.between(selectedDate, currentDate).years
                if (age < 16) {
                    Toast.makeText(requireContext(), "Вы должны быть не моложе 16 лет", Toast.LENGTH_SHORT).show()
                } else {
                    binding.edDateOfBirth.setText(selectedDate.toString())
                }
            }
        }

        binding.btSignUp.setOnClickListener {
            registerUser()
        }

        binding.txAutorization.setOnClickListener {
            navigateToFragment(R.id.authorizationFragment)
        }

        binding.rgUserType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbStudent -> showStudentFields()
                R.id.rbTeacher -> showTeacherFields()
            }
        }
    }

    private fun registerUser() {
        val name = binding.edName.text.toString().trim()
        val surname = binding.edSurname.text.toString().trim()
        val patronymic = binding.edPatronymic.text.toString().trim()
        val email = binding.edEmail.text.toString().trim()
        val password = binding.edPassword.text.toString().trim()
        val confirmPassword = binding.edConfirmPassword.text.toString().trim()

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(context, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        if (!(email.endsWith("@gmail.com", ignoreCase = true) || email.endsWith("@mail.ru", ignoreCase = true))) {
            Toast.makeText(context, "Почта должна быть на @gmail.com или @mail.ru", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(context, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(context, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return
        }

        when {
            binding.rbStudent.isChecked -> registerStudent(name, surname, patronymic, email, password)
            binding.rbTeacher.isChecked -> registerTeacher(name, surname, patronymic, email, password)
        }
    }

    private fun registerStudent(name: String, surname: String, patronymic: String, email: String, password: String) {
        val dateOfBirth = binding.edDateOfBirth.text.toString().trim()
        val groupIdText = binding.edGroupId.text.toString().trim()

        if (dateOfBirth.isEmpty() || groupIdText.isEmpty()) {
            Toast.makeText(context, "Пожалуйста, заполните все поля для студента", Toast.LENGTH_SHORT).show()
            return
        }

        val groupId = groupIdText.toIntOrNull()
        if (groupId == null) {
            Toast.makeText(context, "ID группы должно быть числом", Toast.LENGTH_SHORT).show()
            return
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
                    handleRegistrationResponse(email, response, true) // <-- student
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Ошибка соединения: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun registerTeacher(name: String, surname: String, patronymic: String, email: String, password: String) {
        if (selectedSubjects.isEmpty()) {
            Toast.makeText(context, "Пожалуйста, выберите хотя бы один предмет", Toast.LENGTH_SHORT).show()
            return
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
                    handleRegistrationResponse(email, response, false) // <-- teacher
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Ошибка соединения: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun handleRegistrationResponse(email: String, response: Response<Void>, isStudent: Boolean) {
        if (response.isSuccessful) {
            Toast.makeText(context, "На указанный email отправлен код подтверждения", Toast.LENGTH_SHORT).show()
            showVerificationDialog(email, isStudent)
        }
        else {
            val errorMessage = response.errorBody()?.string()
            Toast.makeText(context, "Ошибка регистрации: $errorMessage", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showStudentFields() {
        binding.tvSubjects.visibility = View.GONE
        binding.rvSubjects.visibility = View.GONE
        binding.tilDateOfBirth.visibility = View.VISIBLE
        binding.tvGroupDetails.visibility = View.VISIBLE
        binding.tilGroupId.visibility = View.VISIBLE
    }

    private fun showTeacherFields() {
        binding.tvSubjects.visibility = View.VISIBLE
        binding.rvSubjects.visibility = View.VISIBLE
        binding.tilDateOfBirth.visibility = View.GONE
        binding.tvGroupDetails.visibility = View.GONE
        binding.tilGroupId.visibility = View.GONE
    }

    private fun loadGroups() {
        WebServerSingleton.getApiService(requireContext()).getAllGroups().enqueue(object : Callback<List<Group>> {
            override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {
                if (response.isSuccessful) {
                    val groups = response.body()
                    val groupDetails = groups?.sortedBy { it.name }
                        ?.joinToString("\n") { "ID: ${it.groupId}, Название: ${it.name}" }
                        ?: "Нет доступных групп"
                    binding.tvGroupDetails.text = groupDetails
                } else {
                    Toast.makeText(context, "Ошибка загрузки групп", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Group>>, t: Throwable) {
                Toast.makeText(context, "Ошибка соединения", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadSubjects() {
        WebServerSingleton.getApiService(requireContext()).getAllSubjects().enqueue(object : Callback<List<Subject>> {
            override fun onResponse(call: Call<List<Subject>>, response: Response<List<Subject>>) {
                if (response.isSuccessful) {
                    response.body()?.let { subjectAdapter.updateSubjects(it) }
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

    private fun navigateToFragment(fragmentId: Int) {
        findNavController().navigate(fragmentId, null, getNavOptions())
    }

    // Функция для показа диалога ввода кода подтверждения
    private fun showVerificationDialog(email: String, isStudent: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Подтверждение Email")
        builder.setMessage("Введите 6-значный код, отправленный на вашу почту:")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Подтвердить") { dialog, which ->
            val code = input.text.toString().trim()
            if (code.length != 6) {
                Toast.makeText(requireContext(), "Код должен состоять из 6 цифр", Toast.LENGTH_SHORT).show()
            } else {
                if (isStudent) {
                    WebServerSingleton.getApiService(requireContext())
                        .confirmStudentRegistration(EmailVerificationRequest(email, code))
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(requireContext(), "Email успешно подтвержден", Toast.LENGTH_SHORT).show()
                                    navigateToFragment(R.id.authorizationFragment)
                                } else {
                                    Toast.makeText(requireContext(), "Ошибка подтверждения email", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(requireContext(), "Ошибка сети: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        })
                } else {
                    WebServerSingleton.getApiService(requireContext())
                        .confirmTeacherRegistration(EmailVerificationRequest(email, code))
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(requireContext(), "Email успешно подтвержден", Toast.LENGTH_SHORT).show()
                                    navigateToFragment(R.id.authorizationFragment)
                                } else {
                                    Toast.makeText(requireContext(), "Ошибка подтверждения email", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(requireContext(), "Ошибка сети: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }
        }

        builder.setNegativeButton("Отмена") { dialog, which ->
            dialog.cancel()
        }
        builder.show()
    }
}
