package com.gearsy.scitechsearchengine.db.neo4j.repository

import com.gearsy.scitechsearchengine.db.neo4j.entity.RubricNode
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RubricNeo4jRepository: Neo4jRepository<RubricNode, UUID> {

    @Query(
        "UNWIND \$rubrics AS r " +
        "CREATE (rubric:Rubric {cipher: r.cipher}) " +
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
