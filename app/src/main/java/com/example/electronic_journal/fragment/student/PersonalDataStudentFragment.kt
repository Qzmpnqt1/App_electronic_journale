package com.example.electronic_journal.fragment.student

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
import com.example.electronic_journal.databinding.FragmentPersonalDataStudentBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.model.Student
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PersonalDataStudentFragment : Fragment() {

    private var _binding: FragmentPersonalDataStudentBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPersonalDataStudentBinding.inflate(inflater, container, false)
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
        apiService.getPersonalDataStudent().enqueue(object : Callback<Student> {
            override fun onResponse(call: Call<Student>, response: Response<Student>) {
                if (response.isSuccessful) {
                    val student = response.body()
                    student?.let {
                        displayStudentData(it)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки данных студента",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Student>, t: Throwable) {
                Log.e("PersonalDataFragment", "Ошибка сети: ${t.message}")
                Toast.makeText(requireContext(), "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayStudentData(student: Student) {
        binding.tvStudentFullName.text =
            "ФИО: ${student.surname} ${student.name} ${student.patronymic ?: ""}".trim()
        binding.tvStudentID.text = "ID Студента: ${student.studentId}"
        binding.tvStudentEmail.text = "Email: ${student.email}"
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
