package com.example.electronic_journal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.electronic_journal.databinding.ItemSubjectStatsBinding
import com.example.electronic_journal.server.autorization.SubjectStatsDTO

class SubjectStatsAdapter(private var statsList: List<SubjectStatsDTO>)
    : RecyclerView.Adapter<SubjectStatsAdapter.StatsViewHolder>() {

    inner class StatsViewHolder(val binding: ItemSubjectStatsBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSubjectStatsBinding.inflate(inflater, parent, false)
        return StatsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        val item = statsList[position]
        holder.binding.tvSubjectName.text = "Предмет: ${item.subjectName}"

        // Зимняя
        if (item.bestWinterGrade != null && item.bestWinterStudent != null) {
            holder.binding.tvBestWinter.text = "Лучшая: ${item.bestWinterStudent} (${item.bestWinterGrade})"
        } else {
            holder.binding.tvBestWinter.text = "Лучшая: —"
        }

        if (item.averageWinterGrade != null) {
            holder.binding.tvAverageWinter.text = "Средний балл: %.2f".format(item.averageWinterGrade)
        } else {
            holder.binding.tvAverageWinter.text = "Средний балл: —"
        }

        if (item.worstWinterGrade != null && item.worstWinterStudent != null) {
            holder.binding.tvWorstWinter.text = "Худшая: ${item.worstWinterStudent} (${item.worstWinterGrade})"
        } else {
            holder.binding.tvWorstWinter.text = "Худшая: —"
        }

        // Летняя
        if (item.bestSummerGrade != null && item.bestSummerStudent != null) {
            holder.binding.tvBestSummer.text = "Лучшая: ${item.bestSummerStudent} (${item.bestSummerGrade})"
        } else {
            holder.binding.tvBestSummer.text = "Лучшая: —"
        }

        if (item.averageSummerGrade != null) {
            holder.binding.tvAverageSummer.text = "Средний балл: %.2f".format(item.averageSummerGrade)
        } else {
            holder.binding.tvAverageSummer.text = "Средний балл: —"
        }

        if (item.worstSummerGrade != null && item.worstSummerStudent != null) {
            holder.binding.tvWorstSummer.text = "Худшая: ${item.worstSummerStudent} (${item.worstSummerGrade})"
        } else {
            holder.binding.tvWorstSummer.text = "Худшая: —"
        }
    }

    override fun getItemCount(): Int {
        return statsList.size
    }

    fun updateData(newStats: List<SubjectStatsDTO>) {
        statsList = newStats
        notifyDataSetChanged()
    }
}
