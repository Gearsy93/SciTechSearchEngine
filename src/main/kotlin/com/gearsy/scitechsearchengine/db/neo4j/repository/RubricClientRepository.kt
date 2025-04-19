package com.gearsy.scitechsearchengine.db.neo4j.repository

import com.gearsy.scitechsearchengine.db.neo4j.entity.RubricNode
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Repository

@Repository
class RubricClientRepository(
    private val neo4jClient: Neo4jClient
) {

    fun loadRubricHierarchy(): List<RubricNode> {
        val result = neo4jClient.query(
            """
            MATCH (r:Rubric)
            OPTIONAL MATCH (r)-[:HAS_CHILD]->(child:Rubric)
            RETURN r.cipher AS cipher,
                   r.title AS title,
                   r.embedding AS embedding,
                   collect(child.cipher) AS childCiphers
            """
        ).fetch().all()

        val rubricMap = mutableMapOf<String, RubricNode>()
        val childLinks = mutableMapOf<String, List<String>>()

        for (record in result) {
            val cipher = record["cipher"] as? String ?: continue
            val title = record["title"] as? String ?: continue
            val embedding = (record["embedding"] as? List<*>)
                ?.mapNotNull { (it as? Number)?.toDouble() } ?: continue
            val childCiphers = (record["childCiphers"] as? List<*>)
                ?.mapNotNull { it as? String } ?: emptyList()

            rubricMap[cipher] = RubricNode(
                cipher = cipher,
                title = title,
                embedding = embedding,
                thesaurusType = ThesaurusType.TERMINOLOGICAL,
                sessionId = null,
                queryId = null,
                termList = null,
                children = emptyList()
            )

            childLinks[cipher] = childCiphers
        }

        for ((parentCipher, childCipherList) in childLinks) {
            val parent = rubricMap[parentCipher]
            if (parent != null) {
                val children = childCipherList.mapNotNull { rubricMap[it] }
                rubricMap[parentCipher] = parent.copy(children = children)
            }
        }

        return rubricMap.values.toList()
    }

    fun getRubricTitles(ciphers: Collection<String>): List<Map<String, String>> {
        val query =
            "MATCH (r:Rubric) " +
                    "WHERE r.cipher IN \$ciphers " +
                    "RETURN r.cipher AS cipher, r.title AS title"

        return neo4jClient.query(query)
            .bind(ciphers).to("ciphers")
            .fetch()
            .all()
            .map {
                mapOf(
                    "cipher" to (it["cipher"]?.toString() ?: ""),
                    "title" to (it["title"]?.toString() ?: "")
                )
            }
    }
}
