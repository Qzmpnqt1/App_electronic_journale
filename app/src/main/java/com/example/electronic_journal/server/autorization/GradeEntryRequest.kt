package com.example.electronic_journal.server.autorization

data class GradeEntryRequest(
    val studentId: Int,
    val subjectId: Int,
    val grade: Int
)