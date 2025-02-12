package com.example.electronic_journal.fragment.teacher

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.electronic_journal.R
import com.example.electronic_journal.databinding.FragmentPersonalDataTeacherBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.model.Teacher
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PersonalDataTeacherFragment : Fragment(R.layout.fragment_personal_data_teacher) {

    private var _binding: FragmentPersonalDataTeacherBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPersonalDataTeacherBinding.inflate(inflater, container, false)
        fetchPersonalData()

        // Обработчик клика по кнопке выхода
        binding.btLogout.setOnClickListener {
            logout()
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchPersonalData() {
        val apiService = WebServerSingleton.getApiService(requireContext())
        apiService.getPersonalDataTeacher().enqueue(object : Callback<Teacher> {
            override fun onResponse(call: Call<Teacher>, response: Response<Teacher>) {
                if (response.isSuccessful) {
                    val teacher = response.body()
                    teacher?.let {
                        displayTeacherData(it)
                    }
                } else {
                    // Логирование детальной информации об ошибке
                    val errorBody = response.errorBody()?.string()
                    Log.e("PersonalDataTeacher", "Ошибка загрузки данных учителя: ${response.code()} - ${response.message()} - $errorBody")
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки данных учителя",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Teacher>, t: Throwable) {
                Log.e("PersonalDataTeacher", "Ошибка сети: ${t.message}")
                Toast.makeText(requireContext(), "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayTeacherData(teacher: Teacher) {
        binding.tvTeacherFullName.text =
            "ФИО: ${teacher.surname} ${teacher.name} ${teacher.patronymic ?: ""}".trim()
        binding.tvTeacherID.text = "ID Преподавателя: ${teacher.teacherId}"
        binding.tvTeacherEmail.text = "Email: ${teacher.email}"
        // Если хотите отобразить роль или предметы, добавьте соответствующие TextView
    }

    private fun logout() {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("token")
            remove("role")
            apply()
        }

        // Переход к фрагменту авторизации
        navigateToFragment(R.id.authorizationFragment)
    }

    private fun navigateToFragment(fragmentId: Int) {
        findNavController().navigate(fragmentId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
