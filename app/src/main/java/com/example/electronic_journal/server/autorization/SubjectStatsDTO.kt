package com.example.electronic_journal.server.autorization

data class SubjectStatsDTO(
    val subjectName: String,

    val bestWinterStudent: String?,
    val bestWinterGrade: Int?,
    val averageWinterGrade: Double?,
    val worstWinterStudent: String?,
    val worstWinterGrade: Int?,

    val bestSummerStudent: String?,
    val bestSummerGrade: Int?,
    val averageSummerGrade: Double?,
    val worstSummerStudent: String?,
    val worstSummerGrade: Int?
)
