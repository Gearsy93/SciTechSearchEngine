package com.gearsy.scitechsearchengine.db.neo4j.repository

import com.gearsy.scitechsearchengine.db.neo4j.entity.TermNode
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TermNeo4jRepository : Neo4jRepository<TermNode, UUID> {



    @Query(
            "UNWIND \$terms AS term " +
            "CREATE (t:Term {content: term.content}) " +
            "SET t.embedding = term.embedding, " +
            "    t.thesaurusType = term.thesaurusType, " +
            "    t.sourceType = term.sourceType, " +
            "    t.sessionId = term.sessionId, " +
            "    t.queryId = term.queryId, " +
            "    t.score = term.score " +
            "WITH t, term " +
            "MATCH (r:Rubric {cipher: term.cipher}) " +
            "MERGE (t)-[:BELONGS_TO]->(r) ")
    fun createTermLinks(@Param("terms") terms: List<Map<String, Any?>>)

}