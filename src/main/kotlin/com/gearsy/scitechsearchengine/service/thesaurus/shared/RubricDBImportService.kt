package com.gearsy.scitechsearchengine.service.thesaurus.shared

import com.gearsy.scitechsearchengine.db.neo4j.entity.RubricNode
import com.gearsy.scitechsearchengine.db.neo4j.entity.TermNode
import com.gearsy.scitechsearchengine.db.neo4j.entity.TermSourceType
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import com.gearsy.scitechsearchengine.db.neo4j.repository.RubricNeo4jRepository
import com.gearsy.scitechsearchengine.db.neo4j.repository.TermNeo4jRepository
import com.gearsy.scitechsearchengine.model.thesaurus.RubricImportDTO
import com.gearsy.scitechsearchengine.model.thesaurus.RubricTermImportDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RubricDBImportService(
    private val rubricRepository: RubricNeo4jRepository,
    private val termRepository: TermNeo4jRepository
) {
    private val logger = LoggerFactory.getLogger(RubricDBImportService::class.java)

    fun TermNode.toImportDTO(cipher: String): RubricTermImportDTO {
        return RubricTermImportDTO(
            content = this.content,
            embedding = this.embedding,
            rubricatorId = when (this.sourceType) {
                TermSourceType.GRNTI -> 1
                TermSourceType.RVINITI -> 2
                TermSourceType.VINITI_CATALOG -> 3
                else -> 0
            },
        )
    }

    fun RubricNode.toImportDTO(): RubricImportDTO {
        return RubricImportDTO(
            cipher = this.cipher,
            title = this.title,
            embedding = this.embedding,
            termList = this.termList?.map { it.toImportDTO(this.cipher) } ?: emptyList(),
            children = emptyList()
        )
    }


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
        flatRubrics: List<RubricNode>,
        thesaurusType: ThesaurusType
    ) {
        val rubricList = flatRubrics.map { it.toImportDTO() }.map { r ->
            mapOf(
                "cipher" to r.cipher,
                "title" to r.title,
                "parentCipher" to null,
                "embedding" to r.embedding,
                "thesaurusType" to thesaurusType.toString(),
                "sessionId" to null,
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
                    "sessionId" to null,
                    "queryId" to queryId,
                    "cipher" to r.cipher
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