package com.gearsy.scitechsearchengine.service.thesaurus

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gearsy.scitechsearchengine.db.neo4j.entity.RubricNode
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import com.gearsy.scitechsearchengine.db.neo4j.repository.RubricNeo4jRepository
import com.gearsy.scitechsearchengine.db.neo4j.repository.TermNeo4jRepository
import com.gearsy.scitechsearchengine.model.thesaurus.RubricImportDTO
import com.gearsy.scitechsearchengine.utils.Neo4jDriverProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class RubricImportService(
    private val rubricImportTransactionalService: RubricImportTransactionalService,
    private val neo4jDriverProvider: Neo4jDriverProvider,
    private val rubricRepository: RubricNeo4jRepository,
    private val termRepository: TermNeo4jRepository
) {
    val mapper = jacksonObjectMapper()

    private val logger = LoggerFactory.getLogger(RubricImportService::class.java)

    fun fillTermThesaurus(rubricCipher: String) {

        // Путь к файлу JSON
        val rubricEmbeddingsPath = "src/main/resources/rubricator/embedding/$rubricCipher.json"
        val rootRubric: RubricImportDTO = mapper.readValue(File(rubricEmbeddingsPath))

        // Открываем сессию и заполняем базу
        rubricImportTransactionalService.insertRubricsAndTerms(rootRubric)
    }

    @Deprecated(message = "Для единичных загрузок")
    fun fillTermThesaurusRubric(
        rubric: RubricNode,
        parentCipher: String?,
    ) {
        rubricRepository.createOrUpdateRubric(
            cipher = rubric.cipher,
            title = rubric.title,
            embedding = rubric.embedding,
            thesaurusType = rubric.thesaurusType!!.name,
            sessionId = rubric.sessionId,
            queryId = rubric.queryId
        )

        if (parentCipher != null) {
            rubricRepository.linkRubrics(parentCipher, rubric.cipher)
        }

        rubric.termList?.forEach { term ->
            termRepository.createOrUpdateTerm(
                content = term.content,
                embedding = term.embedding,
                rubricCipher = rubric.cipher,
                thesaurusType = ThesaurusType.TERMINOLOGICAL.toString(),
                sessionId = null,
                queryId = null,
                sourceType = ""

            )
        }

        rubric.children.forEach { child ->
            fillTermThesaurusRubric(child, rubric.cipher)
        }
    }

}