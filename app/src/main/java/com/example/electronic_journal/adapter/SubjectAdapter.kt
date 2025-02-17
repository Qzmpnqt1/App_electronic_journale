package com.example.electronic_journal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.electronic_journal.databinding.ItemSubjectBinding
import com.example.electronic_journal.server.model.Subject

class SubjectAdapter(
    private val subjects: MutableList<Subject>,
    private val onSubjectSelected: (Subject, Boolean) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    private val selectedSubjects = mutableSetOf<Subject>() // Хранит выбранные предметы

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]
        holder.bind(subject, selectedSubjects.contains(subject))
    }

    override fun getItemCount(): Int = subjects.size

    fun updateSubjects(newSubjects: List<Subject>) {
        subjects.clear()
        subjects.addAll(newSubjects.sortedBy { it.name }) // Сортировка по имени в алфавитном порядке
        selectedSubjects.clear() // Очищаем состояния выбранных предметов
        notifyDataSetChanged()
    }

    fun getSelectedSubjects(): List<Subject> = selectedSubjects.toList()

    inner class SubjectViewHolder(private val binding: ItemSubjectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(subject: Subject, isSelected: Boolean) {
            binding.tvSubjectName.text = subject.name
            binding.cbSelectSubject.setOnCheckedChangeListener(null) // Сбросить старый слушатель
            binding.cbSelectSubject.isChecked = isSelected // Установить правильное состояние
            binding.cbSelectSubject.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedSubjects.add(subject)
                } else {
                    selectedSubjects.remove(subject)
                }
                onSubjectSelected(subject, isChecked)
            }
        }
    }
}