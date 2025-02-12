package com.example.electronic_journal.server.autorization

data class StudentRegistrationRequest(
    val name: String,
    val surname: String,
    val patronymic: String,
    val dateOfBirth: String,
    val email: String,
    val password: String,
    val groupId: Int
)