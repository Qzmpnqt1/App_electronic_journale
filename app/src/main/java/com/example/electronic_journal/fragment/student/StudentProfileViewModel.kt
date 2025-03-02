package com.example.electronic_journal.fragment.student

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.electronic_journal.server.model.Student

class StudentProfileViewModel : ViewModel() {
    private val _studentData = MutableLiveData<Student?>()
    val studentData: LiveData<Student?> get() = _studentData

    fun setStudentData(student: Student?) {
        _studentData.value = student
    }

    fun clearData() {
        _studentData.value = null
    }
}
