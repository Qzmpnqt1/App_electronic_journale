package com.example.electronic_journal.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.electronic_journal.R
import com.example.electronic_journal.databinding.FragmentAuthorizationBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.autorization.AuthRequest
import com.example.electronic_journal.server.autorization.AuthResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthorizationFragment : Fragment() {

    private lateinit var binding: FragmentAuthorizationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorizationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        val role = sharedPref.getString("role", null)

        if (token != null && role != null) {
            redirectToRoleFragment(role)
        }

        binding.txSignUp.setOnClickListener {
            navigateToFragment(R.id.signUpFragment)
        }

        binding.btLogin.setOnClickListener {
            val email = binding.edEmail.text.toString().trim()
            val password = binding.edPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Введите email и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val authRequest = AuthRequest(email, password)
            Log.d("AuthorizationFragment", "JSON для отправки: $authRequest")
            WebServerSingleton.getApiService(requireContext()).login(authRequest)
                .enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        if (response.isSuccessful) {
                            val authResponse = response.body()
                            if (authResponse != null) {
                                val token = authResponse.token
                                val role = authResponse.role

                                with(sharedPref.edit()) {
                                    putString("token", token)
                                    putString("role", role)
                                    apply()
                                }
                                redirectToRoleFragment(role)
                            } else {
                                Toast.makeText(context, "Ошибка обработки ответа", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Неверный email или пароль", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        Log.e("AuthorizationFragment", "Ошибка соединения", t)
                        Toast.makeText(context, "Ошибка соединения: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun redirectToRoleFragment(role: String) {
        when (role) {
            "ROLE_ADMIN" -> navigateToFragment(R.id.administratorFragment)
            "ROLE_TEACHER" -> navigateToFragment(R.id.teacherFragment)
            "ROLE_STUDENT" -> navigateToFragment(R.id.studentFragment)
            else -> Toast.makeText(context, "Неизвестная роль", Toast.LENGTH_SHORT).show()
        }
    }

    // Функция возвращает NavOptions с заданными анимациями
    private fun getNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()
    }

    private fun navigateToFragment(fragmentId: Int) {
        // Передаём NavOptions для программного задания анимаций
        findNavController().navigate(fragmentId, null, getNavOptions())
    }
}
