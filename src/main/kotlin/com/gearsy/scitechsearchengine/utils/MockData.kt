package com.gearsy.scitechsearchengine.utils

import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.entity.SearchResult
import java.util.*

fun generateMockResults(query: Query): List<SearchResult> {
    val count = (5..15).random()
    val titles = listOf(
        "Искусственный интеллект в промышленности",
        "Цифровая трансформация производств",
        "Интернет вещей и автоматизация",
        "Умные фабрики и системы управления",
        "Оптимизация процессов с помощью ИИ",
        "Цифровизация производственных цепочек",
        "Автоматизация технологических процессов"
    )
    val used = mutableSetOf<String>()

    return (1..count).map { i ->
        val availableTitles = titles.filter { it !in used }
        val title = if (availableTitles.isNotEmpty()) {
            availableTitles.random()
        } else {
            "Автоматизированный заголовок №$i" // или просто titles.random(), если дубли допустимы
        }

        used.add(title)

        SearchResult(
            query = query,
            documentId = UUID.randomUUID().toString(),
            documentUrl = "https://example.com/doc-$i",
            title = title,
            snippet = "$title, $title, $title, $title, $title",
            score = "%.2f".format((0.6 + Math.random() * 0.39)).toDouble()
        )
    }
}