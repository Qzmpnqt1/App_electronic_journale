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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.electronic_journal.R
import com.example.electronic_journal.databinding.FragmentPersonalDataTeacherBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.model.Teacher
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PersonalDataTeacherFragment : Fragment() {

    private var _binding: FragmentPersonalDataTeacherBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TeacherProfileViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalDataTeacherBinding.inflate(inflater, container, false)
        setupObservers()

        if (viewModel.teacherData.value == null) {
            fetchPersonalData()
        }

        binding.btLogout.setOnClickListener {
            confirmLogout()
        }
        return binding.root
    }

    private fun setupObservers() {
        viewModel.teacherData.observe(viewLifecycleOwner, Observer { teacher ->
            teacher?.let {
                displayTeacherData(it)
            } ?: run {
                binding.tvTeacherID.text = ""
                binding.tvTeacherEmail.text = ""
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchPersonalData() {
        val apiService = WebServerSingleton.getApiService(requireContext())
        apiService.getPersonalDataTeacher().enqueue(object : Callback<Teacher> {
            override fun onResponse(call: Call<Teacher>, response: Response<Teacher>) {
                if (response.isSuccessful) {
                    viewModel.setTeacherData(response.body())
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки данных: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Teacher>, t: Throwable) {
                Log.e("PersonalDataTeacher", "Network error: ${t.message}")
                Toast.makeText(
                    requireContext(),
                    "Ошибка сети: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun displayTeacherData(teacher: Teacher) {
        binding.tvTeacherFullName.text =
            "ФИО: ${teacher.surname} ${teacher.name} ${teacher.patronymic ?: ""}".trim()
        binding.tvTeacherID.text = "ID Преподавателя: ${teacher.teacherId}"
        binding.tvTeacherEmail.text = "Email: ${teacher.email}"
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

class TeacherProfileViewModel : androidx.lifecycle.ViewModel() {
    private val _teacherData = androidx.lifecycle.MutableLiveData<Teacher?>()
    val teacherData: androidx.lifecycle.LiveData<Teacher?> get() = _teacherData

    fun setTeacherData(teacher: Teacher?) {
        _teacherData.value = teacher
    }

    fun clearData() {
        _teacherData.value = null
    }
}