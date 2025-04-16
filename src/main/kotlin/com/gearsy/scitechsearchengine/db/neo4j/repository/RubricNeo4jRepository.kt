package com.gearsy.scitechsearchengine.db.neo4j.repository

import com.gearsy.scitechsearchengine.db.neo4j.entity.RubricNode
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RubricNeo4jRepository: Neo4jRepository<RubricNode, UUID> {

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
        RETURN r""")
    fun findAllTerminologicalRubrics(): List<RubricNode>


    @Query(
        "UNWIND \$rubrics AS r " +
        "MERGE (rubric:Rubric {cipher: r.cipher}) " +
        "SET rubric.title = r.title, " +
        "    rubric.embedding = r.embedding, " +
        "    rubric.thesaurusType = r.thesaurusType, " +
        "    rubric.sessionId = r.sessionId, " +
        "    rubric.queryId = r.queryId " +
        "WITH r, rubric " +
        "WHERE r.parentCipher IS NOT NULL " +
        "MATCH (parent:Rubric {cipher: r.parentCipher}) " +
        "MERGE (parent)-[:HAS_CHILD]->(rubric) ")
    fun createRubricHierarchy(@Param("rubrics") rubrics: List<Map<String, Any?>>)
}
