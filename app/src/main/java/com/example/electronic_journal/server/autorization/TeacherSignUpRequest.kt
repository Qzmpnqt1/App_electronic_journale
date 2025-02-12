package com.example.electronic_journal.server.autorization

data class TeacherSignUpRequest(
    val name: String,
    val surname: String,
    val patronymic: String?,
    val email: String,
    val password: String,
    val subjectIds: List<Int>
)