package com.gearsy.scitechsearchengine.db.neo4j.repository

import com.gearsy.scitechsearchengine.db.neo4j.entity.CSCSTIRubricNeo4j
import org.springframework.data.neo4j.repository.Neo4jRepository

interface CSCSTIRubricNeo4jRepository : Neo4jRepository<CSCSTIRubricNeo4j, String> {
    override fun findAll(): List<CSCSTIRubricNeo4j>
}