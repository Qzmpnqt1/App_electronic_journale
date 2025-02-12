package com.example.electronic_journal.fragment.teacher

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.electronic_journal.R
import com.example.electronic_journal.databinding.FragmentGroupsStudyingSubjectBinding
import com.example.electronic_journal.server.WebServerSingleton
import com.example.electronic_journal.server.model.Group
import com.example.electronic_journal.server.model.Subject
import com.example.electronic_journal.server.service.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupsStudyingSubjectFragment : Fragment(R.layout.fragment_groups_studying_subject) {

    private lateinit var binding: FragmentGroupsStudyingSubjectBinding
    private lateinit var apiService: ApiService
    private lateinit var subject: Subject

    companion object {
        private const val ARG_SUBJECT = "arg_subject"

        // Создание нового экземпляра фрагмента с аргументами
        fun newInstance(subject: Subject): GroupsStudyingSubjectFragment {
            val fragment = GroupsStudyingSubjectFragment()
            val bundle = Bundle().apply {
                putParcelable(ARG_SUBJECT, subject)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инициализация binding
        binding = FragmentGroupsStudyingSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем аргумент (предмет) из Bundle
        subject = arguments?.getParcelable(ARG_SUBJECT) ?: return

        // Получаем ApiService через WebServerSingleton
        apiService = WebServerSingleton.getApiService(requireContext())

        // Запрашиваем данные о группах для выбранного предмета
        apiService.getGroupsForSubject(subject.subjectId).enqueue(object : Callback<List<Group>> {
            override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {
                if (response.isSuccessful) {
                    Log.d("Groups", "Ответ получен: ${response.body()}")
                    loadGroups(response.body()!!)
                } else {
                    Log.e("Groups", "Ошибка: ${response.code()} - ${response.message()}")
                    Toast.makeText(context, "Ошибка получения групп", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Group>>, t: Throwable) {
                Log.e("Groups", "Ошибка сети: ${t.message}")
                Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadGroups(groups: List<Group>) {
        // Очищаем контейнер перед добавлением
        binding.groupsContainer.removeAllViews()

        // Для каждой группы создаем карточку
        for (group in groups) {
            val cardView = layoutInflater.inflate(R.layout.item_group_card, binding.groupsContainer, false)

            val groupNameTextView = cardView.findViewById<TextView>(R.id.tvGroupName)
            groupNameTextView.text = group.name

            // Обработчик клика на группу
            cardView.setOnClickListener {
                // Переход на фрагмент с подгруженными студентами
                val fragment = StudentInGroupFragment.newInstance(group.groupId, subject.subjectId)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)  // контейнер фрагмента
                    .commit()
            }

            binding.groupsContainer.addView(cardView)
        }
    }
}


