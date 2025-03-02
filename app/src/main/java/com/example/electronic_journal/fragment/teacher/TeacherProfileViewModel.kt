package com.example.electronic_journal.fragment.teacher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.electronic_journal.server.model.Teacher

class TeacherProfileViewModel : ViewModel() {
    private val _teacherData = MutableLiveData<Teacher?>()
    val teacherData: LiveData<Teacher?> get() = _teacherData

    fun setTeacherData(teacher: Teacher?) {
        _teacherData.value = teacher
    }

    fun clearData() {
        _teacherData.value = null
    }
}
