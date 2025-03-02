package com.example.electronic_journal.fragment.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.electronic_journal.adapter.SubjectStatsAdapter
import com.example.electronic_journal.databinding.FragmentStatsBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.autorization.SubjectStatsDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SubjectStatsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SubjectStatsAdapter(emptyList())
        binding.rvStats.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStats.adapter = adapter

        loadStats()
    }

    private fun loadStats() {
        val api = WebServerSingleton.getApiService(requireContext())
        api.getGroupStats().enqueue(object : Callback<List<SubjectStatsDTO>> {
            override fun onResponse(call: Call<List<SubjectStatsDTO>>, response: Response<List<SubjectStatsDTO>>) {
                if (response.isSuccessful) {
                    val statsList = response.body() ?: emptyList()
                    adapter.updateData(statsList)
                    if (statsList.isEmpty()) {
                        Toast.makeText(requireContext(), "Статистика недоступна", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Ошибка сервера: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<SubjectStatsDTO>>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
