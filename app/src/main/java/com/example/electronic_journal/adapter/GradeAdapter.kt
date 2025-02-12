package com.example.electronic_journal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.electronic_journal.databinding.ItemGradeEntryBinding
import com.example.electronic_journal.server.autorization.GradeEntryDTO

class GradeAdapter(private var gradeEntries: List<GradeEntryDTO>) :
    RecyclerView.Adapter<GradeAdapter.GradeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeViewHolder {
        val binding = ItemGradeEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GradeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GradeViewHolder, position: Int) {
        val gradeEntry = gradeEntries[position]
        holder.bind(gradeEntry)
    }

    override fun getItemCount(): Int = gradeEntries.size

    fun updateData(newGradeEntries: List<GradeEntryDTO>) {
        gradeEntries = newGradeEntries
        notifyDataSetChanged()
    }

    class GradeViewHolder(private val binding: ItemGradeEntryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(gradeEntry: GradeEntryDTO) {
            binding.tvSubjectName.text = "Предмет: ${gradeEntry.subjectName}"
            binding.tvGrade.text = "Оценка: ${gradeEntry.grade}"
        }
    }
}

