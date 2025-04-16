package com.gearsy.scitechsearchengine.service.thesaurus

import com.gearsy.scitechsearchengine.db.neo4j.entity.TermSourceType
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import com.gearsy.scitechsearchengine.db.neo4j.repository.RubricNeo4jRepository
import com.gearsy.scitechsearchengine.db.neo4j.repository.TermNeo4jRepository
import com.gearsy.scitechsearchengine.model.thesaurus.RubricImportDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RubricImportTransactionalService(
    private val rubricRepository: RubricNeo4jRepository,
    private val termRepository: TermNeo4jRepository
) {
    private val logger = LoggerFactory.getLogger(RubricImportTransactionalService::class.java)


    @Transactional
    fun insertRubricsAndTerms(root: RubricImportDTO) {
        val rubricList = mutableListOf<Map<String, Any?>>()
        val termList = mutableListOf<Map<String, Any?>>()

        fun flattenRubric(r: RubricImportDTO, parentCipher: String?) {
            rubricList += mapOf(
                "cipher" to r.cipher,
                "title" to r.title,
                "parentCipher" to parentCipher,
                "embedding" to r.embedding,
                "thesaurusType" to ThesaurusType.TERMINOLOGICAL.toString(),
                "sessionId" to null,
                "queryId" to null
            )

            termList += r.termList.orEmpty().map { term ->
                mapOf(
                    "content" to term.content,
                    "embedding" to term.embedding,
                    "thesaurusType" to ThesaurusType.TERMINOLOGICAL.toString(),
                    "sourceType" to when (term.rubricatorId) {
                        1 -> TermSourceType.GRNTI
                        2 -> TermSourceType.RVINITI
                        else -> TermSourceType.VINITI_CATALOG
                    }.toString(),
                    "sessionId" to null,
                    "queryId" to null,
                    "cipher" to r.cipher
                )
            }

            r.children.forEach { child -> flattenRubric(child, r.cipher) }
        }

        flattenRubric(root, null)

        rubricRepository.createRubricHierarchy(rubricList)

        termList.chunked(500).forEachIndexed { index, batch ->
            logger.info("Inserting term batch ${index + 1}/${(termList.size + 499) / 500} (${batch.size} terms)...")
            termRepository.createTermLinks(batch)
        }

        logger.info("Insert complete: ${rubricList.size} rubrics, ${termList.size} terms.")
    }
}