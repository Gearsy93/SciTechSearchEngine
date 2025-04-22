package com.gearsy.scitechsearchengine.db.neo4j.repository

import com.gearsy.scitechsearchengine.db.neo4j.entity.ContextRubricNode
import com.gearsy.scitechsearchengine.db.neo4j.entity.ContextTermNode
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Repository

@Repository
class ContextTermClientRepository(
    private val neo4jClient: Neo4jClient
) {

    fun insertOrUpdateContextTerms(terms: List<Map<String, Any>>) {
        neo4jClient.query(
            "UNWIND \$terms AS term " +
                    "MERGE (r:ContextRubric {cipher: term.cipher}) " +
                    "  ON CREATE SET r.title = term.cipher " +
                    "WITH r, term " +
                    "OPTIONAL MATCH (r)-[:HAS_TERM]->(existing:ContextTerm) " +
                    "  WHERE existing.content = term.content AND existing.sessionId = term.sessionId " +
                    "FOREACH (_ IN CASE WHEN existing IS NULL THEN [1] ELSE [] END | " +
                    "  CREATE (t:ContextTerm { " +
                    "    content: term.content, " +
                    "    sessionId: term.sessionId, " +
                    "    lastQueryId: term.lastQueryId, " +
                    "    count: 1 " +
                    "  }) " +
                    "  MERGE (r)-[:HAS_TERM]->(t) " +
                    ") " +
                    "FOREACH (_ IN CASE WHEN existing IS NOT NULL THEN [1] ELSE [] END | " +
                    "  SET existing.count = existing.count + 1, " +
                    "      existing.lastQueryId = term.lastQueryId) "
        ).bindAll(mapOf("terms" to terms)).run()
    }

    fun findRubricsWithTermsBySession(sessionId: Long): List<ContextRubricNode> {
        val query =
        "MATCH (r:ContextRubric)-[:HAS_TERM]->(t:ContextTerm) " +
        "WHERE t.sessionId = \$sessionId " +
        "RETURN r.cipher AS cipher, r.title AS title, t.content AS content, t.count AS count, t.sessionId AS sessionId, t.lastQueryId AS lastQueryId "

        val result = neo4jClient.query(query)
            .bind(sessionId).to("sessionId")
            .fetch().all()

        return result.groupBy { it["cipher"] to it["title"] }.map { (rubricKey, group) ->
            val (cipher, title) = rubricKey
            val terms = group.map {
                ContextTermNode(
                    content = it["content"].toString(),
                    count = (it["count"] as Number).toInt(),
                    sessionId = (it["sessionId"] as Number).toLong(),
                    lastQueryId = (it["lastQueryId"] as Number).toLong()
                )
            }
            ContextRubricNode(
                cipher = cipher.toString(),
                title = title.toString(),
                termList = terms
            )
        }
    }

}