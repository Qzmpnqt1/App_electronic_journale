package com.example.electronic_journal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.electronic_journal.databinding.ItemGradebookCardBinding
import com.example.electronic_journal.server.autorization.GradeEntryDTO
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GradeAdapter(private var gradeEntries: List<GradeEntryDTO>) :
    RecyclerView.Adapter<GradeAdapter.GradeViewHolder>() {

    // Формат "yyyy-MM-dd HH:mm:ss"
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

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

    // Функция-расширение для String
    private fun String.parseAndFormatDateTime(): String {
        return try {
            val dateTime = LocalDateTime.parse(this) // Парсим ISO-8601
            dateTime.format(dateTimeFormatter)
        } catch (e: Exception) {
            // Если формат не совпадает или парсинг не удался, вернём исходную строку
            this
        }
    }

    inner class GradeViewHolder(private val binding: ItemGradebookCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: GradeEntryDTO) {
            // Название предмета
            binding.tvSubjectName.text = "Предмет: ${entry.subjectName}"

            // Зимняя оценка
            val winterGradeText = if (entry.winterGrade != null) {
                val dateStr = entry.winterDateAssigned?.parseAndFormatDateTime() ?: ""
                "Зимняя оценка: ${entry.winterGrade}, выставлена $dateStr"
            } else {
                "Зимняя оценка: -"
            }
            binding.tvWinterSession.text = winterGradeText

            // Летняя оценка
            val summerGradeText = if (entry.summerGrade != null) {
                val dateStr = entry.summerDateAssigned?.parseAndFormatDateTime() ?: ""
                "Летняя оценка: ${entry.summerGrade}, выставлена $dateStr"
            } else {
                "Летняя оценка: -"
            }
            binding.tvSummerSession.text = summerGradeText
        }
    }
}

