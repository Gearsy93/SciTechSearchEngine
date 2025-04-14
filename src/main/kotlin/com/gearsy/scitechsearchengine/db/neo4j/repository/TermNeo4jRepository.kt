package com.gearsy.scitechsearchengine.db.neo4j.repository

import com.gearsy.scitechsearchengine.db.neo4j.entity.TermNode
import com.gearsy.scitechsearchengine.db.neo4j.repository.projection.RubricTermProjection
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TermNeo4jRepository {

    @Query("MERGE (t:Term {content: \$content}) " +
        "SET t.embedding = \$embedding, " +
            "t.thesaurusType = \$thesaurusType, " +
            "t.sessionId = \$sessionId, " +
            "t.queryId = \$queryId " +
        "WITH t " +
        "MATCH (r:Rubric {cipher: \$cipher}) " +
        "MERGE (t)-[:BELONGS_TO]->(r) ")
    fun createOrUpdateTerm(
        @Param("content") content: String,
        @Param("embedding") embedding: List<Double>,
        @Param("cipher") rubricCipher: String,
        @Param("thesaurusType") thesaurusType: String,
        @Param("sessionId") sessionId: Long?,
        @Param("queryId") queryId: Long?
    )

    @Query(
        "MATCH (t:Term)-[:BELONGS_TO]->(r:Rubric) " +
                "WHERE r.cipher IN \$rubricCiphers " +
                "RETURN r.cipher AS rubricCipher, t.content AS content, t.embedding AS embedding"
    )
    fun loadTermsFromTermThesaurus(
        @Param("rubricCiphers") rubricCiphers: List<String>
    ): List<RubricTermProjection>


    @Query("MATCH (t:Term) " +
        "WHERE t.thesaurusType = 'CONTEXTUAL' AND t.sessionId = \$sessionId " +
        "RETURN t")
    fun findContextualTerms(@Param("sessionId") sessionId: Long): List<TermNode>

    @Query("MATCH (t:Term) " +
       "WHERE t.thesaurusType = 'ITERATIVE' " +
       "  AND t.sessionId =\$sessionId " +
       "  AND t.queryId = \$queryId " +
       "RETURN t")
    fun findIterativeTerms(
        @Param("sessionId") sessionId: Long,
        @Param("queryId") queryId: Long
    ): List<TermNode>

    @Query("MATCH (t:Term) " +
            "HERE t.thesaurusType = 'EXTENDED_ITERATIVE' " +
            "AND t.sessionId = \$sessionId " +
            "AND t.queryId = \$queryId " +
            "RETURN t")
    fun findExtendedIterativeTerms(
        @Param("sessionId") sessionId: Long,
        @Param("queryId") queryId: Long
    ): List<TermNode>
}