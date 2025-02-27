package com.example.electronic_journal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.electronic_journal.databinding.ItemGradebookCardBinding
import com.example.electronic_journal.server.autorization.GradeEntryDTO

class GradeAdapter(private var gradeEntries: List<GradeEntryDTO>) :
    RecyclerView.Adapter<GradeAdapter.GradeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemGradebookCardBinding.inflate(inflater, parent, false)
        return GradeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GradeViewHolder, position: Int) {
        holder.bind(gradeEntries[position])
    }

    override fun getItemCount(): Int = gradeEntries.size

    fun updateData(newGradeEntries: List<GradeEntryDTO>) {
        gradeEntries = newGradeEntries
        notifyDataSetChanged()
    }

    inner class GradeViewHolder(private val binding: ItemGradebookCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: GradeEntryDTO) {
            // Название предмета
            binding.tvSubjectName.text = "Предмет: ${entry.subjectName}"

            // Зимняя оценка
            val winterGradeText = if (entry.winterGrade != null) {
                val dateStr = entry.winterDateAssigned ?: "-"
                "Зимняя сессия: ${entry.winterGrade} ($dateStr)"
            } else {
                "Зимняя сессия: -"
            }
            binding.tvWinterSession.text = winterGradeText

            // Летняя оценка
            val summerGradeText = if (entry.summerGrade != null) {
                val dateStr = entry.summerDateAssigned ?: "-"
                "Летняя сессия: ${entry.summerGrade} ($dateStr)"
            } else {
                "Летняя сессия: -"
            }
            binding.tvSummerSession.text = summerGradeText
        }
    }
}
