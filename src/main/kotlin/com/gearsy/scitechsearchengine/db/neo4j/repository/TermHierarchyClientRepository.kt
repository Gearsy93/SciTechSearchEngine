package com.gearsy.scitechsearchengine.db.neo4j.repository

import com.gearsy.scitechsearchengine.db.neo4j.entity.TermNode
import com.gearsy.scitechsearchengine.db.neo4j.entity.TermSourceType
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Repository


@Repository
class TermHierarchyClientRepository(
    private val neo4jClient: Neo4jClient
) {

    fun loadTermsForRubrics(rubricCiphers: List<String>): Map<String, List<TermNode>> {
        val query =
            "MATCH (t:Term)-[:BELONGS_TO]->(r:Rubric) " +
            "WHERE r.cipher IN \$rubricCiphers AND t.thesaurusType = 'TERMINOLOGICAL' " +
            "RETURN r.cipher AS rubricCipher, t.content AS content, t.embedding AS embedding, t.sourceType as sourceType"

        val result = neo4jClient.query(query)
            .bind(rubricCiphers).to("rubricCiphers")
            .fetch().all()

        return result.groupBy(
            keySelector = { it["rubricCipher"] as String },
            valueTransform = {
                val content = it["content"] as? String ?: ""
                val embedding = (it["embedding"] as? List<*>)
                    ?.mapNotNull { e -> (e as? Number)?.toDouble() } ?: emptyList()
                val sourceType = it["sourceType"] as? String ?: ""

                TermNode(
                    content = content,
                    embedding = embedding,
                    thesaurusType = ThesaurusType.TERMINOLOGICAL,
                    sourceType = try {
                        TermSourceType.valueOf(sourceType)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                )
            }
        )
    }
}
