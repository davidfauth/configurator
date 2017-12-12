# Lenovo Proof of Concept
POC for Lenovo

This project requires Neo4j 3.2.x

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/procedures-1.0-SNAPSHOT.jar`,
that can be copied to the `plugin` directory of your Neo4j instance.

    cp target/procedures-0.8-SNAPSHOT.jar neo4j-enterprise-3.2.6/plugins/.


Edit your Neo4j/conf/neo4j.conf file by adding this line:

    dbms.security.procedures.unrestricted=com.lenovo.*    
    
(Re)start Neo4j

Create the schema:

    CALL com.lenovo.schema.generate;

Import the data:

Use the Cypher/DecLoadScript scripts to load the data.

In the future we will load the data as such:

	CALL com.lenovo.imports.materials("/Users/maxdemarzi/Projects/lenovo_poc/imports/C_OD.csv");
    CALL com.lenovo.imports.variants("/Users/maxdemarzi/Projects/lenovo_poc/imports/C_VARIANT_TABLE.csv");
    CALL com.lenovo.imports.characteristics("/Users/maxdemarzi/Projects/lenovo_poc/imports/C_CHARACTERISTIC_DESC.csv");
    CALL com.lenovo.imports.values("/Users/maxdemarzi/Projects/lenovo_poc/imports/C_CHARACTERISTIC_VALUE_DESC.csv");    
    
If using Windows you must escape the slashes like so:

    CALL com.lenovo.imports.characteristics('C:\\Users\\maxdemarzi\\Projects\\lenovo_poc\\imports\\C_CHARACTERISTIC_DESC.csv');

Model:

    (Material)-[:HAS_BOM]->(CharCharValue)-[:HAS_VARIANT]->(VarTabSeq)

Queries:

    // Get initial valid options
    CALL com.lenovo.options('70SQCTO1WW',{})

    // Get list of options
	call com.lenovo.options('30ATCTO1WW',{EPCOUNTRY:'LUXEMBOURG',EPCPU:'INTEL XEON E3_1220V5 3.0GHZ',EPUSBADAPTER:'USB 3.0 ADD_IN CARD HP',EPWIFI:'NO_WIFI',EPPRELOAD_OS:'WIN7_P64'});
	
	//To Do
	Accuracy check on components.
	
 