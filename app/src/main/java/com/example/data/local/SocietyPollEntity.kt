package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PollOption
import com.example.data.model.SocietyPoll

@Entity(tableName = "society_polls")
data class SocietyPollEntity(
    @PrimaryKey val id: String,
    val question: String,
    val description: String,
    val optionsRaw: String, // Format: OptionText:::VotesCount|||OptionText:::VotesCount
    val totalVotes: Int,
    val userVotedOptionIndex: Int?,
    val createdAtMillis: Long,
    val isClosed: Boolean
) {
    fun toDomain(): SocietyPoll {
        val parsedOptions = if (optionsRaw.isBlank()) emptyList() else {
            optionsRaw.split("|||").mapIndexedNotNull { idx, item ->
                val parts = item.split(":::")
                if (parts.isNotEmpty()) {
                    PollOption(
                        index = idx,
                        text = parts[0],
                        votesCount = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    )
                } else null
            }
        }
        return SocietyPoll(
            id = id,
            question = question,
            description = description,
            options = parsedOptions,
            totalVotes = totalVotes,
            userVotedOptionIndex = userVotedOptionIndex,
            createdAtMillis = createdAtMillis,
            isClosed = isClosed
        )
    }

    companion object {
        fun formatOptions(options: List<PollOption>): String =
            options.joinToString("|||") { "${it.text}:::${it.votesCount}" }
    }
}
