package com.gearsy.scitechsearchengine.service.search

import com.fasterxml.jackson.databind.ObjectMapper
import com.gearsy.scitechsearchengine.config.properties.VinitiECatalogProperties
import com.gearsy.scitechsearchengine.controller.dto.search.SearchRequestDTO
import com.gearsy.scitechsearchengine.controller.dto.search.SearchResultResponseDTO
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.entity.SearchResult
import com.gearsy.scitechsearchengine.db.postgres.repository.SearchResultRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.ViewedDocumentRepository
import com.gearsy.scitechsearchengine.model.viniti.catalog.VinitiServiceInput
import com.gearsy.scitechsearchengine.service.external.VinitiSearchService
import com.gearsy.scitechsearchengine.service.external.YandexService
import com.gearsy.scitechsearchengine.service.query.expansion.QueryExpansionService
import com.gearsy.scitechsearchengine.service.rank.summarize.SummarizationAndRankingService
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricDBImportService
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricSearchAlgorithmService
import com.gearsy.scitechsearchengine.service.thesaurus.type.ContextualThesaurusService
import com.gearsy.scitechsearchengine.service.thesaurus.type.ExtendedIterativeThesaurusService
import com.gearsy.scitechsearchengine.service.viniti.document.VinitiDocumentService
import com.gearsy.scitechsearchengine.utils.SearchProgressHandler
import com.gearsy.scitechsearchengine.utils.generateMockResults
import com.gearsy.scitechsearchengine.utils.getVinitiCatalogMock
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File

@Service
class SearchConveyorService(
    private val relevantRubricTermSearchService: RubricSearchAlgorithmService,
    private val viewedDocumentRepository: ViewedDocumentRepository,
    private val searchResultRepository: SearchResultRepository,
    private val rubricDBImportService: RubricDBImportService,
    private val vinitiDocSearchService: VinitiSearchService,
    private val vinitiECatalogProperties: VinitiECatalogProperties,
    private val extendedIterativeThesaurusService: ExtendedIterativeThesaurusService,
    private val queryExpansionService: QueryExpansionService,
    private val contextualThesaurusService: ContextualThesaurusService,
    private val vinitiDocumentService: VinitiDocumentService,
    private val yandexService: YandexService,
    private val summarizationAndRankingService: SummarizationAndRankingService,
    private val progressHandler: SearchProgressHandler
) {

    @Transactional
    fun handleSearchConveyor(request: SearchRequestDTO, query: Query): List<SearchResultResponseDTO> {

        // Выполнение конвейера поиска
        val searchResultList = performSearchConveyor(query, request.sessionId, request.query)


//        progressHandler.broadcast("Ъе ъе")
//        runBlocking { delay(2000) }
//        progressHandler.broadcast("Ъе")
        // Временные прикольные результаты
//        val fakeResults = generateMockResults(query)

        // Сохраняем
//        val savedResults = searchResultRepository.saveAll(fakeResults)
        val savedResults = searchResultRepository.saveAll(searchResultList)

        // Просмотренные document.id в пределах всей сессии
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

    fun performSearchConveyor(query: Query, sessionId: Long, queryText: String): List<SearchResult> {

        // Получение релевантных рубрик и терминов терминологического тезауруса
        progressHandler.broadcast("Отбор релевантных запросу рубрик и терминов")
        val iterativeRubricTermList = relevantRubricTermSearchService.getRelevantTermListFromTermThesaurus(queryText)

        // Заполнение итерационного тезауруса
        rubricDBImportService.insertRubricsAndTermsFlat(query.id, sessionId, ThesaurusType.ITERATIVE, iterativeRubricTermList)

        // Получение результатов структурированного поиска
        val vinitiSearchInput = VinitiServiceInput(
            rubricCodes = iterativeRubricTermList.map { it.cipher },
            maxPages = vinitiECatalogProperties.maxPages.toInt(),
            queryId = query.id,
            sessionId = sessionId
        )
        progressHandler.broadcast("Извлечение терминологии из каталога ВИНИТИ")
        val vinitiSearchResults = vinitiDocSearchService.getActualRubricListTerm(vinitiSearchInput)
//        val vinitiSearchResults = getVinitiCatalogMock()

        // Заполнение расширенного итерационного тезауруса,
        val extendedRubricTermList = extendedIterativeThesaurusService.insertStructuredRubricAndTerms(query.id, sessionId, queryText, ThesaurusType.EXTENDED_ITERATIVE, vinitiSearchResults)

        // Сохранение структурированных данных в реляционную БД
        vinitiDocumentService.saveVinitiResults(query, vinitiSearchResults)

        // Сохранение данных итерационных тезаурусов в контекстный
        contextualThesaurusService.updateSessionTerms(query, sessionId, iterativeRubricTermList, extendedRubricTermList)

        // Подсчет весов терминов на основе встречаемости в сессии контекстного тезауруса
        val evaluatedScoreTermList = queryExpansionService.evaluateTermListFinalScore(sessionId, iterativeRubricTermList, extendedRubricTermList)

        // Подготовка поисковых предписаний
        progressHandler.broadcast("Подготовка подзапросов для неструктурированного поиска")
        val prescriptionList = queryExpansionService.buildBalancedSearchPrescriptions(queryText, evaluatedScoreTermList)

        // Неструктурированный поиск
        progressHandler.broadcast("Неструктурированных поиск через Yandex Search API")
        val yandexResultList = runBlocking { yandexService.processUnstructuredSearch(query.id, prescriptionList) }

//        val prescriptionPath = "D:\\Project\\HSE\\SciTechSearchEngine\\src\\main\\resources\\prescriptionMock.json"
//        val mapper = ObjectMapper()
//        mapper.writerWithDefaultPrettyPrinter().writeValue(File(prescriptionPath), yandexResultList)

        // Ранжирование и реферирование
        progressHandler.broadcast("Ранжирование и реферирование результатов поиска")
        val searchResultList = summarizationAndRankingService.performRankingAndSummarization(query, yandexResultList)

        return searchResultList
    }
}