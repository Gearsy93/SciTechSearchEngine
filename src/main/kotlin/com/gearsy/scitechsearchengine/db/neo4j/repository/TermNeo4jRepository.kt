package com.gearsy.scitechsearchengine.db.neo4j.repository

import com.gearsy.scitechsearchengine.db.neo4j.entity.TermNode
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TermNeo4jRepository : Neo4jRepository<TermNode, UUID> {

    @Query("MERGE (t:Term {content: \$content}) " +
        "SET t.embedding = \$embedding, " +
            "t.thesaurusType = \$thesaurusType, " +
            "t.sessionId = \$sessionId, " +
            "t.queryId = \$queryId " +
            "t.sourceType = \$sourceType" +
        "WITH t " +
        "MATCH (r:Rubric {cipher: \$cipher}) " +
        "MERGE (t)-[:BELONGS_TO]->(r)")
    fun createOrUpdateTerm(
        @Param("content") content: String,
        @Param("embedding") embedding: List<Double>,
        @Param("cipher") rubricCipher: String,
        @Param("thesaurusType") thesaurusType: String,
        @Param("sessionId") sessionId: Long?,
        @Param("queryId") queryId: Long?,
        @Param("sourceType") sourceType: String
    )

    @Query("MATCH (t:Term) " +
           "WHERE t.thesaurusType = 'CONTEXTUAL' AND t.sessionId = \$sessionId " +
           "RETURN t")
    fun findContextualTerms(@Param("sessionId") sessionId: Long): List<TermNode>

    @Query("MATCH (t:Term) " +
           "WHERE t.thesaurusType = 'ITERATIVE' " +
               "AND t.sessionId =\$sessionId " +
               "AND t.queryId = \$queryId " +
           "RETURN t")
    fun findIterativeTerms(
        @Param("sessionId") sessionId: Long,
        @Param("queryId") queryId: Long
    ): List<TermNode>

    @Query("MATCH (t:Term) " +
           "WHERE t.thesaurusType = 'EXTENDED_ITERATIVE' " +
                "AND t.sessionId = \$sessionId " +
                "AND t.queryId = \$queryId " +
           "RETURN t")
    fun findExtendedIterativeTerms(
        @Param("sessionId") sessionId: Long,
        @Param("queryId") queryId: Long
    ): List<TermNode>

    @Query(
            "UNWIND \$terms AS term " +
            "MERGE (t:Term {content: term.content}) " +
            "SET t.embedding = term.embedding, " +
            "    t.thesaurusType = term.thesaurusType, " +
            "    t.sourceType = term.sourceType, " +
            "    t.sessionId = term.sessionId, " +
            "    t.queryId = term.queryId " +
            "WITH t, term " +
            "MATCH (r:Rubric {cipher: term.cipher}) " +
            "MERGE (t)-[:BELONGS_TO]->(r) ")
    fun createTermLinks(@Param("terms") terms: List<Map<String, Any?>>)
}