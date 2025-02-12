package com.example.electronic_journal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.electronic_journal.R
import com.example.electronic_journal.server.model.Student

class StudentAdapter(
    private val students: List<Student>,
    private val onStudentClick: (Student) -> Unit // Лямбда для обработки кликов
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    private val sortedStudents = students.sortedBy { it.surname } // Сортировка студентов по фамилии

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = sortedStudents[position] // Используем отсортированный список
        holder.bind(student)
        holder.itemView.setOnClickListener {
            onStudentClick(student) // Передаем клик на студента
        }
    }

    override fun getItemCount(): Int = sortedStudents.size

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val studentNameTextView: TextView = itemView.findViewById(R.id.tvStudentName)
        private val studentIdTextView: TextView = itemView.findViewById(R.id.tvStudentID)

        fun bind(student: Student) {
            // Формируем полное имя из name, surname и patronymic
            val fullName = "${student.surname} ${student.name} ${student.patronymic ?: ""}".trim()

            studentNameTextView.text = fullName
            studentIdTextView.text = "ID: ${student.studentId}"
        }
    }
}


