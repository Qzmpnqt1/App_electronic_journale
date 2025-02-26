package com.example.electronic_journal.fragment.teacher

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
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

    private var allGroups: List<Group> = emptyList()
    private var searchQuery: String = ""

    companion object {
        private const val ARG_SUBJECT = "arg_subject"

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
        binding = FragmentGroupsStudyingSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("SEARCH_QUERY", "")
            binding.searchInputLayout.editText?.setText(searchQuery)
        }

        binding.searchInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
            }
            override fun afterTextChanged(s: Editable?) { }
        })

        subject = arguments?.getParcelable(ARG_SUBJECT) ?: return

        apiService = WebServerSingleton.getApiService(requireContext())

        apiService.getGroupsForSubject(subject.subjectId).enqueue(object : Callback<List<Group>> {
            override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {
                if (response.isSuccessful) {
                    Log.d("Groups", "Ответ получен: ${response.body()}")
                    allGroups = response.body()!!
                    loadGroups(allGroups)
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

        binding.searchInputLayout.setEndIconOnClickListener {
            if (searchQuery.isNotEmpty()) {
                searchGroup(searchQuery)
            } else {
                Toast.makeText(context, "Введите название группы", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnReload.setOnClickListener {
            loadGroups(allGroups)
        }

        // Обработчик кнопки "Назад"
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SEARCH_QUERY", searchQuery)
    }

    private fun loadGroups(groups: List<Group>) {
        // Сортируем группы по названию в алфавитном порядке
        val sortedGroups = groups.sortedBy { it.name }

        binding.groupsContainer.removeAllViews()
        val constraintSet = ConstraintSet()

        for ((index, group) in sortedGroups.withIndex()) {
            // Используем универсальную карточку для группы
            val cardView = layoutInflater.inflate(R.layout.item_name_card, binding.groupsContainer, false)
            cardView.id = View.generateViewId()

            // Получаем TextView и устанавливаем название группы
            val nameTextView = cardView.findViewById<TextView>(R.id.tvName)
            nameTextView.text = group.name

            // Обработка нажатия на карточку – переход к фрагменту со студентами выбранной группы
            cardView.setOnClickListener {
                val fragment = StudentInGroupFragment.newInstance(group.groupId, subject.subjectId, subject.name)
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            binding.groupsContainer.addView(cardView)

            // Устанавливаем ограничения для карточки
            constraintSet.clone(binding.groupsContainer)
            if (index == 0) {
                constraintSet.connect(cardView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16)
            } else {
                val previousCard = binding.groupsContainer.getChildAt(index - 1)
                constraintSet.connect(cardView.id, ConstraintSet.TOP, previousCard.id, ConstraintSet.BOTTOM, 16)
            }
            constraintSet.connect(cardView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16)
            constraintSet.connect(cardView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16)
            constraintSet.applyTo(binding.groupsContainer)
        }
    }

    private fun searchGroup(query: String) {
        val foundGroups = allGroups.filter { it.name.contains(query, ignoreCase = true) }
        if (foundGroups.isNotEmpty()) {
            loadGroups(foundGroups)
        } else {
            Toast.makeText(context, "Группа не найдена", Toast.LENGTH_SHORT).show()
        }
    }
}