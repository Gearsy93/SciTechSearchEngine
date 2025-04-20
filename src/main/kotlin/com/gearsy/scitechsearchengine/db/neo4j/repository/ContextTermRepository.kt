package com.gearsy.scitechsearchengine.db.neo4j.repository

import com.gearsy.scitechsearchengine.db.neo4j.entity.ContextTermNode
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ContextTermRepository : Neo4jRepository<ContextTermNode, UUID> {

}