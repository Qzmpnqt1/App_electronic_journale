package com.example.electronic_journal.fragment.teacher

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.electronic_journal.R
import com.example.electronic_journal.databinding.FragmentPersonalDataTeacherBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.autorization.UploadPhotoResponse
import com.example.electronic_journal.server.model.Teacher
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream

class PersonalDataTeacherFragment : Fragment() {

    private var _binding: FragmentPersonalDataTeacherBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TeacherProfileViewModel by viewModels()

    // Лаунчер для выбора изображения из галереи
    private lateinit var galleryLauncher: androidx.activity.result.ActivityResultLauncher<String>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalDataTeacherBinding.inflate(inflater, container, false)
        setupObservers()

        // Регистрация лаунчера для выбора изображения
        galleryLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                // Анимация для ImageView
                binding.ivTeacherProfilePicture?.animate()?.scaleX(1.1f)?.scaleY(1.1f)
                    ?.setDuration(150)
                    ?.withEndAction {
                        binding.ivTeacherProfilePicture?.animate()!!.scaleX(1f).scaleY(1f)
                            .setDuration(150)
                            .start()
                    }
                    ?.start()
                // Мгновенно отображаем выбранное изображение через Glide
                binding.ivTeacherProfilePicture?.let { it1 ->
                    Glide.with(this)
                        .load(it)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(it1)
                }

                // Загружаем фото на сервер
                uploadPhotoToServer(it)
            }
        }

        if (viewModel.teacherData.value == null) {
            fetchPersonalData()
        }

        // Клик по фото — открыть галерею
        binding.ivTeacherProfilePicture?.setOnClickListener {
            val scaleAnimation = ScaleAnimation(
                1f, 0.9f, 1f, 0.9f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
            )
            scaleAnimation.duration = 100
            scaleAnimation.fillAfter = true
            binding.ivTeacherProfilePicture!!.startAnimation(scaleAnimation)
            galleryLauncher.launch("image/*")
        }

        binding.btLogout.setOnClickListener {
            confirmLogout()
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

        return binding.root
    }

    private fun setupObservers() {
        viewModel.teacherData.observe(viewLifecycleOwner) { teacher ->
            teacher?.let {
                displayTeacherData(it)
            } ?: run {
                binding.tvTeacherID.text = ""
                binding.tvTeacherEmail.text = ""
            }
        }
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

        // Если URL фото есть, загружаем через Glide
        if (!teacher.photoUrl.isNullOrEmpty()) {
            binding.ivTeacherProfilePicture?.let {
                Glide.with(this)
                    .load(teacher.photoUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(it)
            }
        } else {
            binding.ivTeacherProfilePicture?.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    private fun uploadPhotoToServer(uri: Uri) {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes == null) {
            Toast.makeText(requireContext(), "Не удалось прочитать изображение", Toast.LENGTH_SHORT).show()
            return
        }
        val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("photo", "teacher_profile.jpg", requestFile)

        val apiService = WebServerSingleton.getApiService(requireContext())
        apiService.uploadTeacherPhoto(body).enqueue(object : Callback<UploadPhotoResponse> {
            override fun onResponse(call: Call<UploadPhotoResponse>, response: Response<UploadPhotoResponse>) {
                if (response.isSuccessful) {
                    val newPhotoUrl = response.body()?.photoUrl
                    Toast.makeText(requireContext(), "Фотография учителя обновлена", Toast.LENGTH_SHORT).show()
                    viewModel.teacherData.value?.let { teacher ->
                        val updatedTeacher = teacher.copy(photoUrl = newPhotoUrl)
                        viewModel.setTeacherData(updatedTeacher)
                    }
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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

    private fun navigateToFragment(fragmentId: Int) {
        findNavController().navigate(fragmentId, null, getNavOptions())
    }

    private fun getNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_left)
            .setExitAnim(R.anim.slide_out_right)
            .setPopEnterAnim(R.anim.slide_in_right)
            .setPopExitAnim(R.anim.slide_out_left)
            .build()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}