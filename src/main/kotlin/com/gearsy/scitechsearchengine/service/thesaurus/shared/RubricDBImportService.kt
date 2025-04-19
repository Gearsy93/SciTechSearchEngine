package com.gearsy.scitechsearchengine.service.thesaurus.shared

import com.gearsy.scitechsearchengine.db.neo4j.entity.RubricNode
import com.gearsy.scitechsearchengine.db.neo4j.entity.TermNode
import com.gearsy.scitechsearchengine.db.neo4j.entity.TermSourceType
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import com.gearsy.scitechsearchengine.db.neo4j.repository.RubricNeo4jRepository
import com.gearsy.scitechsearchengine.db.neo4j.repository.TermNeo4jRepository
import com.gearsy.scitechsearchengine.model.thesaurus.RubricImportDTO
import com.gearsy.scitechsearchengine.model.thesaurus.RubricTermImportDTO
import com.gearsy.scitechsearchengine.model.viniti.catalog.VinitiDocumentMeta
import com.gearsy.scitechsearchengine.service.lang.model.EmbeddingService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RubricDBImportService(
    private val rubricRepository: RubricNeo4jRepository,
    private val termRepository: TermNeo4jRepository,

) {
    private val logger = LoggerFactory.getLogger(RubricDBImportService::class.java)

    @Transactional
    fun insertRubricsAndTermsHierarchy(root: RubricImportDTO, thesaurusType: ThesaurusType) {
        val rubricList = mutableListOf<Map<String, Any?>>()
        val termList = mutableListOf<Map<String, Any?>>()

        fun flattenRubric(r: RubricImportDTO, parentCipher: String?) {
            rubricList += mapOf(
                "cipher" to r.cipher,
                "title" to r.title,
                "parentCipher" to parentCipher,
                "embedding" to r.embedding,
                "thesaurusType" to thesaurusType.toString(),
                "sessionId" to null,
                "queryId" to null
            )

            termList += r.termList.orEmpty().map { term ->
                mapOf(
                    "content" to term.content,
                    "embedding" to term.embedding,
                    "thesaurusType" to thesaurusType.toString(),
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

    @Transactional
    fun insertRubricsAndTermsFlat(
        queryId: Long,
        sessionId: Long,
        thesaurusType: ThesaurusType,
        flatRubrics: List<RubricNode>

    ) {
        val rubricList = flatRubrics.map { r ->
            mapOf(
                "cipher" to r.cipher,
                "title" to r.title,
                "parentCipher" to null,
                "embedding" to r.embedding,
                "thesaurusType" to thesaurusType.toString(),
                "sessionId" to sessionId,
                "queryId" to queryId
            )
        }

        val termList = flatRubrics.flatMap { r ->
            r.termList.orEmpty().map { term ->
                mapOf(
                    "content" to term.content,
                    "embedding" to term.embedding,
                    "thesaurusType" to thesaurusType.toString(),
                    "sourceType" to term.sourceType.toString(),
                    "sessionId" to sessionId,
                    "queryId" to queryId,
                    "cipher" to r.cipher,
                    "score" to term.score
                )
            }
        }
        rubricRepository.createRubricHierarchy(rubricList)

        termList.chunked(500).forEachIndexed { index, batch ->
            logger.info("Inserting term batch ${index + 1}/${(termList.size + 499) / 500} (${batch.size} terms)...")
            termRepository.createTermLinks(batch)
        }
        logger.info("Flat insert complete: ${rubricList.size} rubrics, ${termList.size} terms.")
    }


}