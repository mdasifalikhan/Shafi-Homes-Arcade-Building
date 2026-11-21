package com.example.data.model

data class PollOption(
    val index: Int,
    val text: String,
    val votesCount: Int
)

data class SocietyPoll(
    val id: String,
    val question: String,
    val description: String,
    val options: List<PollOption>,
    val totalVotes: Int,
    val userVotedOptionIndex: Int?, // null if not voted
    val createdAtMillis: Long,
    val isClosed: Boolean = false
)
