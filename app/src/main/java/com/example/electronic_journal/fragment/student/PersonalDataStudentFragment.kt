package com.example.electronic_journal.fragment.student

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.electronic_journal.R
import com.example.electronic_journal.databinding.FragmentPersonalDataStudentBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.autorization.UploadPhotoResponse
import com.example.electronic_journal.server.model.Student
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream

class PersonalDataStudentFragment : Fragment() {

    private var _binding: FragmentPersonalDataStudentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentProfileViewModel by viewModels()

    // Лаунчер для выбора изображения из галереи
    private lateinit var galleryLauncher: androidx.activity.result.ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Регистрируем ActivityResultLauncher
        galleryLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                // Анимация ImageView
                binding.ivProfilePicture?.animate()?.scaleX(1.1f)?.scaleY(1.1f)
                    ?.setDuration(150)
                    ?.withEndAction {
                        binding.ivProfilePicture?.animate()?.scaleX(1f)?.scaleY(1f)
                            ?.setDuration(150)
                            ?.start()
                    }
                    ?.start()
                // Мгновенно отображаем выбранное изображение через Glide
                binding.ivProfilePicture?.let { imageView ->
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(imageView)
                }
                // Отправляем фото на сервер
                uploadPhotoToServer(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalDataStudentBinding.inflate(inflater, container, false)
        setupObservers()
        // Если данные профиля ещё не загружены, делаем запрос
        if (viewModel.studentData.value == null) {
            fetchPersonalData()
        }
        // Обработка клика по фото – открытие галереи
        binding.ivProfilePicture?.setOnClickListener {
            val scaleAnimation = ScaleAnimation(
                1f, 0.9f, 1f, 0.9f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
            )
            scaleAnimation.duration = 100
            scaleAnimation.fillAfter = true
            binding.ivProfilePicture!!.startAnimation(scaleAnimation)
            galleryLauncher.launch("image/*")
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
                binding.ivProfilePicture?.setImageResource(R.drawable.ic_profile_placeholder)
            }
        }
    }

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

        // Загружаем фото по URL, если оно задано
        if (!student.photoUrl.isNullOrEmpty()) {
            binding.ivProfilePicture?.let {
                Glide.with(requireContext())
                    .load(student.photoUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(it)
            }
        } else {
            binding.ivProfilePicture?.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Подтверждение выхода")
            .setMessage("Вы уверены, что хотите выйти из аккаунта?")
            .setPositiveButton("Да") { _, _ -> logout() }
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
        requireActivity().findNavController(R.id.fragmentContainer)
            .navigate(R.id.authorizationFragment, null, getNavOptions())
    }

    private fun getNavOptions() = androidx.navigation.NavOptions.Builder()
        .setEnterAnim(R.anim.slide_in_left)
        .setExitAnim(R.anim.slide_out_right)
        .setPopEnterAnim(R.anim.slide_in_right)
        .setPopExitAnim(R.anim.slide_out_left)
        .build()

    private fun uploadPhotoToServer(uri: Uri) {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes == null) {
            Toast.makeText(requireContext(), "Не удалось прочитать изображение", Toast.LENGTH_SHORT).show()
            return
        }
        val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("photo", "profile.jpg", requestFile)
        val apiService = WebServerSingleton.getApiService(requireContext())
        apiService.uploadPhoto(body).enqueue(object : Callback<UploadPhotoResponse> {
            override fun onResponse(call: Call<UploadPhotoResponse>, response: Response<UploadPhotoResponse>) {
                if (response.isSuccessful) {
                    val newPhotoUrl = response.body()?.photoUrl
                    Toast.makeText(requireContext(), "Фотография обновлена", Toast.LENGTH_SHORT).show()
                    viewModel.studentData.value?.let { student ->
                        val updatedStudent = student.copy(photoUrl = newPhotoUrl)
                        viewModel.setStudentData(updatedStudent)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}