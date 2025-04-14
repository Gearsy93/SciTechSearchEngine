package com.gearsy.scitechsearchengine.db.neo4j.repository

import com.gearsy.scitechsearchengine.db.neo4j.entity.RubricNode
import com.gearsy.scitechsearchengine.db.neo4j.repository.projection.RubricHierarchyProjection
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RubricNeo4jRepository {

    @Query("MERGE (r:Rubric {cipher: \$cipher}) " +
           "SET r.title = \$title, r.embedding = \$embedding, " +
           "r.thesaurusType = \$thesaurusType, " +
           "r.sessionId = \$sessionId, " +
           "r.queryId = \$queryId ")
    fun createOrUpdateRubric(
        @Param("cipher") cipher: String,
        @Param("title") title: String,
        @Param("embedding") embedding: List<Double>,
        @Param("thesaurusType") thesaurusType: String,
        @Param("sessionId") sessionId: Long?,
        @Param("queryId") queryId: Long?
    )

    @Query("MATCH (parent:Rubric {cipher: \$parentCipher}), (child:Rubric {cipher: \$childCipher}) " +
           "MERGE (parent)-[:HAS_CHILD]->(child)")
    fun linkRubrics(
        @Param("parentCipher") parentCipher: String,
        @Param("childCipher") childCipher: String
    )

    @Query("""
        MATCH (r:Rubric)
        WHERE r.thesaurusType = 'TERMINOLOGICAL'
        RETURN r
    """)
    fun findAllTerminologicalRubrics(): List<RubricNode>

    @Query("""
    MATCH (r:Rubric)
    OPTIONAL MATCH (r)-[:HAS_CHILD]->(child:Rubric)
    RETURN r.cipher AS parentCipher,
           r.title AS parentTitle,
           r.embedding AS parentEmbedding,
           collect(child.cipher) AS childCiphers
""")
    fun loadRubricHierarchy(): List<RubricHierarchyProjection>
}
