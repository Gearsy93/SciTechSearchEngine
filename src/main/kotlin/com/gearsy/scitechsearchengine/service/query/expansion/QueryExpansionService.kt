package com.gearsy.scitechsearchengine.service.query.expansion

import com.gearsy.scitechsearchengine.config.properties.QueryExpansionProperties
import com.gearsy.scitechsearchengine.model.conveyor.RelevantTerm
import com.gearsy.scitechsearchengine.model.conveyor.SelectedRubric
import mikera.vectorz.Vector
import org.springframework.stereotype.Service

@Service
class QueryExpansionService(
    private val queryExpansionProperties: QueryExpansionProperties
) {

    // Генерация поисковых предписаний
//    val searchQueries = buildSearchQueries(query, selectedRubricsForQuery)
//
    // Лог или возврат поисковых запросов
//    searchQueries.forEachIndexed { idx, queryLocal ->
//        log.info("Поисковое предписание #${idx + 1}: $queryLocal")
//    }

    // Подготовка к генерации поисковых предписаний
//    val selectedRubricsForQuery = result.map { rubricNode ->
//        val relevant = relevantTerms
//            .filter { it.content in (rubricNode.termList?.map { t -> t.content } ?: emptyList()) }
//
//        SelectedRubric(
//            cipher = rubricNode.cipher,
//            title = rubricNode.title,
//            relevantTerms = relevant.map {
//                RelevantTerm(
//                    content = it.content,
//                    similarity = Vector.of(*it.embedding.toDoubleArray()).cosineSimilarity(queryEmbedding)
//                )
//            }
//        )
//    }

    fun buildSearchQueries(
        originalQuery: String,
        selectedRubrics: List<SelectedRubric>,
        maxTermsPerQuery: Int = queryExpansionProperties.maxTermsPerQuery.toInt(),
        maxCharsPerQuery: Int = queryExpansionProperties.maxCharsPerQuery.toInt(),
    ): List<String> {
        val result = mutableListOf<String>()

        // Сначала по одному наиболее релевантному термину из каждой рубрики
        val balancedTerms = selectedRubrics
            .mapNotNull { it.relevantTerms.maxByOrNull { term -> term.similarity } }
            .distinctBy { it.content }

        // Затем глобальные термины по убыванию similarity
        val globalTerms = selectedRubrics
            .flatMap { it.relevantTerms }
            .distinctBy { it.content }
            .sortedByDescending { it.similarity }

        // Объединённый список терминов без повторов
        val allTerms = (balancedTerms + globalTerms)
            .distinctBy { it.content }

        // Формируем поисковые запросы с учетом ограничений
        var currentQuery = StringBuilder(originalQuery)
        var currentTermCount = 0

        for (term in allTerms) {
            val formatted = term.content
            val next = " | $formatted"
            if (currentTermCount + 1 > maxTermsPerQuery || currentQuery.length + next.length > maxCharsPerQuery) {
                result.add(currentQuery.toString())
                currentQuery = StringBuilder(originalQuery).append(" | $formatted")
                currentTermCount = 1
            } else {
                currentQuery.append(next)
                currentTermCount++
            }
        }

        if (currentQuery.isNotEmpty() && !result.contains(currentQuery.toString())) {
            result.add(currentQuery.toString())
        }

        return result
    }
}