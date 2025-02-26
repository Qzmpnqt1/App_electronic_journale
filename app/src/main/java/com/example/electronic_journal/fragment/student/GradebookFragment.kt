package com.example.electronic_journal.fragment.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.electronic_journal.adapter.GradeAdapter
import com.example.electronic_journal.databinding.FragmentGradebookBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.autorization.GradeEntryDTO
import com.example.electronic_journal.server.autorization.GradebookDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GradebookFragment : Fragment() {

    private var _binding: FragmentGradebookBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: GradeAdapter
    private var gradeEntries: List<GradeEntryDTO> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGradebookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем адаптер и назначаем его для RecyclerView
        adapter = GradeAdapter(gradeEntries)
        binding.rvGradebook.layoutManager = LinearLayoutManager(context)
        binding.rvGradebook.adapter = adapter

        // Запрос к серверу для получения зачетки
        val apiService = WebServerSingleton.getApiService(requireContext())
        apiService.getGradebook().enqueue(object : Callback<GradebookDTO> {
            override fun onResponse(call: Call<GradebookDTO>, response: Response<GradebookDTO>) {
                if (response.isSuccessful) {
                    val gradebook = response.body()
                    if (gradebook != null) {
                        gradeEntries = gradebook.gradeEntries
                        adapter.updateData(gradeEntries)

                        if (gradeEntries.isEmpty()) {
                            Toast.makeText(context, "В зачетке нет оценок", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Не удалось получить зачетку", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Ошибка сервера: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GradebookDTO>, t: Throwable) {
                Toast.makeText(context, "Ошибка соединения: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
