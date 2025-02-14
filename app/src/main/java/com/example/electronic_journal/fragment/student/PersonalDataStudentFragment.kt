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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.electronic_journal.R
import com.example.electronic_journal.databinding.FragmentPersonalDataStudentBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.model.Student
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PersonalDataStudentFragment : Fragment() {

    private var _binding: FragmentPersonalDataStudentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentProfileViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalDataStudentBinding.inflate(inflater, container, false)
        setupObservers()

        if (viewModel.studentData.value == null) {
            fetchPersonalData()
        }

        binding.btLogout.setOnClickListener {
            confirmLogout()
        }
        return binding.root
    }

    private fun setupObservers() {
        viewModel.studentData.observe(viewLifecycleOwner) { student ->
            student?.let {
                displayStudentData(it)
            } ?: run {
                binding.tvStudentID.text = ""
                binding.tvStudentEmail.text = ""
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchPersonalData() {
        val apiService = WebServerSingleton.getApiService(requireContext())
        apiService.getPersonalDataStudent().enqueue(object : Callback<Student> {
            override fun onResponse(call: Call<Student>, response: Response<Student>) {
                if (response.isSuccessful) {
                    viewModel.setStudentData(response.body())
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки данных: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Student>, t: Throwable) {
                Log.e("PersonalDataFragment", "Network error: ${t.message}")
                Toast.makeText(
                    requireContext(),
                    "Ошибка сети: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun displayStudentData(student: Student) {
        binding.tvStudentFullName.text =
            "ФИО: ${student.surname} ${student.name} ${student.patronymic ?: ""}".trim()
        binding.tvStudentID.text = "ID Студента: ${student.studentId}"
        binding.tvStudentEmail.text = "Email: ${student.email}"
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
        viewModel.clearData()
        navigateToFragment(R.id.authorizationFragment)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class StudentProfileViewModel : ViewModel() {
    private val _studentData = androidx.lifecycle.MutableLiveData<Student?>()
    val studentData: androidx.lifecycle.LiveData<Student?> get() = _studentData

    fun setStudentData(student: Student?) {
        _studentData.value = student
    }

    fun clearData() {
        _studentData.value = null
    }
}