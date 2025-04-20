package com.gearsy.scitechsearchengine.service.thesaurus.type

import com.gearsy.scitechsearchengine.db.neo4j.entity.RubricNode
import com.gearsy.scitechsearchengine.db.neo4j.repository.ContextTermClientRepository
import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ContextualThesaurusService(
    private val contextTermClientRepository: ContextTermClientRepository
) {
    private val logger = LoggerFactory.getLogger(ContextualThesaurusService::class.java)

    fun updateSessionTerms(query: Query,
                           sessionId: Long,
                           iterativeRubricTermList: List<RubricNode>,
                           extendedRubricTermList: List<RubricNode>) {
        val flatTerms = (iterativeRubricTermList + extendedRubricTermList)
            .flatMap { rubric ->
                rubric.termList.orEmpty().map { term ->
                    mapOf(
                        "content" to term.content,
                        "cipher" to rubric.cipher,
                        "sessionId" to sessionId,
                        "lastQueryId" to query.id
                    )
                }
            }
            .distinctBy { it["cipher"].toString() + "_" + it["content"].toString() }

        contextTermClientRepository.insertOrUpdateContextTerms(flatTerms)

        logger.info("Контекстный тезаурус обновлён: ${flatTerms.size} терминов")
    }
}