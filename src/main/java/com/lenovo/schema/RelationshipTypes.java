package com.lenovo.schema;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypes implements RelationshipType {
    BELONGS_TO,
    HAS_VALUE,
    HAS_VARIANT,
	HAS_BOM
}
