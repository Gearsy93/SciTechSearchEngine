package com.gearsy.scitechsearchengine.service.search

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.gearsy.scitechsearchengine.config.properties.VinitiECatalogProperties
import com.gearsy.scitechsearchengine.controller.dto.search.SearchRequestDTO
import com.gearsy.scitechsearchengine.controller.dto.search.SearchResultResponseDTO
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.repository.SearchResultRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.ViewedDocumentRepository
import com.gearsy.scitechsearchengine.model.viniti.catalog.VinitiServiceInput
import com.gearsy.scitechsearchengine.model.yandex.YandexSearchResultModel
import com.gearsy.scitechsearchengine.service.external.VinitiSearchService
import com.gearsy.scitechsearchengine.service.external.YandexService
import com.gearsy.scitechsearchengine.service.query.expansion.QueryExpansionService
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricDBImportService
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricSearchAlgorithmService
import com.gearsy.scitechsearchengine.service.thesaurus.type.ContextualThesaurusService
import com.gearsy.scitechsearchengine.service.thesaurus.type.ExtendedIterativeThesaurusService
import com.gearsy.scitechsearchengine.service.viniti.document.VinitiDocumentService
import com.gearsy.scitechsearchengine.utils.generateMockResults
import com.gearsy.scitechsearchengine.utils.getVinitiCatalogMock
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
    private val yandexService: YandexService
) {

    @Transactional
    fun handleSearchConveyor(request: SearchRequestDTO, query: Query): List<SearchResultResponseDTO> {

        // Выполнение конвейера поиска
        performSearchConveyor(query, request.sessionId, request.query)

        // Временные прикольные результаты
        val fakeResults = generateMockResults(query)

        // Сохраняем
        val savedResults = searchResultRepository.saveAll(fakeResults)

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

    fun performSearchConveyor(query: Query, sessionId: Long, queryText: String) {

        // Получение релевантных рубрик и терминов терминологического тезауруса
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
//        val vinitiSearchResults = vinitiDocSearchService.getActualRubricListTerm(vinitiSearchInput)
        val vinitiSearchResults = getVinitiCatalogMock()

        // Заполнение расширенного итерационного тезауруса,
        val extendedRubricTermList = extendedIterativeThesaurusService.insertStructuredRubricAndTerms(query.id, sessionId, queryText, ThesaurusType.EXTENDED_ITERATIVE, vinitiSearchResults)

        // Сохранение структурированных данных в реляционную БД
//        vinitiDocumentService.saveVinitiResults(query, vinitiSearchResults)

        // Сохранение данных итерационных тезаурусов в контекстный
        contextualThesaurusService.updateSessionTerms(query, sessionId, iterativeRubricTermList, extendedRubricTermList)

        // Подсчет весов терминов на основе встречаемости в сессии контекстного тезауруса
        val evaluatedScoreTermList = queryExpansionService.evaluateTermListFinalScore(sessionId, iterativeRubricTermList, extendedRubricTermList)

        // Подготовка поисковых предписаний
        val prescriptionList = queryExpansionService.buildBalancedSearchPrescriptions(queryText, evaluatedScoreTermList)

        val prescriptionPath = "D:\\Project\\HSE\\SciTechSearchEngine\\src\\main\\resources\\prescriptionMock.json"
        val mapper = ObjectMapper()

        // Неструктурированный поиск
        val yandexResultList = runBlocking { yandexService.processUnstructuredSearch(query.id, prescriptionList) }

        mapper.writerWithDefaultPrettyPrinter().writeValue(File(prescriptionPath), yandexResultList)
//        val typeRef = object : TypeReference<List<YandexSearchResultModel>>() {}
//        val prescriptionList =  mapper.readValue(File(prescriptionPath), typeRef)

        println()

        // Ранжирование и реферирование

    }
}