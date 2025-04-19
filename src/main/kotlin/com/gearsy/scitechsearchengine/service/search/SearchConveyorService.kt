package com.gearsy.scitechsearchengine.service.search

import com.gearsy.scitechsearchengine.controller.dto.search.SearchRequestDTO
import com.gearsy.scitechsearchengine.controller.dto.search.SearchResultResponseDTO
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.repository.SearchResultRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.ViewedDocumentRepository
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricDBImportService
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricSearchAlgorithmService
import com.gearsy.scitechsearchengine.utils.generateMockResults
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SearchConveyorService(
    private val relevantRubricTermSearchService: RubricSearchAlgorithmService,
    private val viewedDocumentRepository: ViewedDocumentRepository,
    private val searchResultRepository: SearchResultRepository,
    private val rubricDBImportService: RubricDBImportService,
) {

    @Transactional
    fun handleSearchConveyor(request: SearchRequestDTO, query: Query): List<SearchResultResponseDTO> {

        // Выполнение конвейера поиска
        performSearchConveyor(query.id, request.query)

        // Временные прикольные результаты
        val fakeResults = generateMockResults(query)

        // Сохраняем
        val savedResults = searchResultRepository.saveAll(fakeResults)

        // Получаем просмотренные document.id в пределах всей сессии
        val viewedDocsInSession = viewedDocumentRepository
            .findAllByQuerySessionId(request.sessionId)
            .map { it.document.documentId }
            .toSet()

        val dtos = savedResults.map {
            SearchResultResponseDTO(
                id = it.id,
                documentUrl = it.documentUrl,
                title = it.title,
                snippet = it.snippet,
                score = it.score ?: 0.0,
                viewed = viewedDocsInSession.contains(it.documentId)
            )
        }

        return dtos
    }

    fun performSearchConveyor(queryId: Long,query: String) {

        // Получение релевантных рубрик и терминов терминологического тезауруса
        val rubricTermList = relevantRubricTermSearchService.getRelevantTermListFromTermThesaurus(query)

        // Заполнение итерационного тезауруса
        rubricDBImportService.insertRubricsAndTermsFlat(queryId, rubricTermList, ThesaurusType.ITERATIVE)


    }

}