package com.gearsy.scitechsearchengine.service.search

import com.gearsy.scitechsearchengine.config.properties.VinitiECatalogProperties
import com.gearsy.scitechsearchengine.controller.dto.search.SearchRequestDTO
import com.gearsy.scitechsearchengine.controller.dto.search.SearchResultResponseDTO
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.repository.SearchResultRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.ViewedDocumentRepository
import com.gearsy.scitechsearchengine.model.viniti.catalog.VinitiServiceInput
import com.gearsy.scitechsearchengine.service.external.VinitiSearchService
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricDBImportService
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricSearchAlgorithmService
import com.gearsy.scitechsearchengine.service.thesaurus.type.ExtendedIterativeThesaurusService
import com.gearsy.scitechsearchengine.utils.generateMockResults
import com.gearsy.scitechsearchengine.utils.getVinitiCatalogMock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SearchConveyorService(
    private val relevantRubricTermSearchService: RubricSearchAlgorithmService,
    private val viewedDocumentRepository: ViewedDocumentRepository,
    private val searchResultRepository: SearchResultRepository,
    private val rubricDBImportService: RubricDBImportService,
    private val vinitiDocSearchService: VinitiSearchService,
    private val vinitiECatalogProperties: VinitiECatalogProperties,
    private val extendedIterativeThesaurusService: ExtendedIterativeThesaurusService
) {

    @Transactional
    fun handleSearchConveyor(request: SearchRequestDTO, query: Query): List<SearchResultResponseDTO> {

        // Выполнение конвейера поиска
        performSearchConveyor(query.id, request.sessionId, request.query)

        // Временные прикольные результаты
        val fakeResults = generateMockResults(query)

        // Сохраняем
        val savedResults = searchResultRepository.saveAll(fakeResults)

        // Получаем просмотренные document.id в пределах всей сессии
        val viewedDocsInSession = viewedDocumentRepository
            .findAllByQuerySessionId(request.sessionId)
            .map { it.document.documentId }
            .toSet()

        val searchResultResponseDTOList = savedResults.map {
            SearchResultResponseDTO(
                id = it.id,
                documentUrl = it.documentUrl,
                title = it.title,
                snippet = it.snippet,
                score = it.score ?: 0.0,
                viewed = viewedDocsInSession.contains(it.documentId)
            )
        }

        return searchResultResponseDTOList
    }

    fun performSearchConveyor(queryId: Long, sessionId: Long, queryText: String) {

        // Получение релевантных рубрик и терминов терминологического тезауруса
        val rubricTermList = relevantRubricTermSearchService.getRelevantTermListFromTermThesaurus(queryText)

        // Заполнение итерационного тезауруса
        rubricDBImportService.insertRubricsAndTermsFlat(queryId, sessionId, ThesaurusType.ITERATIVE, rubricTermList)

        // Получение результатов структурированного поиска
        val vinitiSearchInput = VinitiServiceInput(
            rubricCodes = rubricTermList.map { it.cipher },
            maxPages = vinitiECatalogProperties.maxPages.toInt(),
            queryId = queryId,
            sessionId = sessionId
        )
//        val vinitiSearchResults = vinitiDocSearchService.getActualRubricListTerm(vinitiSearchInput)
        val vinitiSearchResults = getVinitiCatalogMock()

        // Заполнение расширенного итерационного тезауруса, сохранение структурированных данных в реляционную БД
        extendedIterativeThesaurusService.insertStructuredRubricAndTerms(queryId, sessionId, queryText, ThesaurusType.EXTENDED_ITERATIVE, vinitiSearchResults)
    }
}