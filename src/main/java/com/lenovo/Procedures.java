package com.lenovo;

import com.lenovo.results.MapResult;
import com.lenovo.schema.Labels;
import com.lenovo.schema.RelationshipTypes;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory; 
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.io.FileWriter;
import java.io.StringWriter; 
import java.io.Writer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.Map.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class Procedures {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    // Step 1: Create the Vector Nodes
    @Description("CALL com.lenovo.validate(material, options)")
    @Procedure(name = "com.lenovo.validate", mode = Mode.READ)
    public Stream<MapResult> Validate(@Name("material") String materialId, @Name("options") Map<String, Object> options) {
        Node material = db.findNode(Labels.Material, "id", materialId);
        HashMap<String, Object> results = new HashMap<>();
        HashMap<String, Object> validValues = new HashMap<>();
        HashMap<String, Object> invalidValues = new HashMap<>();

        // Verify material exists or return error.
        if (material == null) {
            return Stream.of(new MapResult(new HashMap<String, Object>() {{
                put("error", "Material: " + materialId + " not found");
            }}));
        } else {
            // Collect the valid options
            Set<String> valid = new HashSet<>();
            for (Relationship r : material.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_VARIANT)) {
                Node variant = r.getEndNode();
                for (Relationship r2 : variant.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_VALUE)){
                    boolean active = (boolean) r2.getProperty("active", true);
                    if (active) {
                        Node value = r2.getEndNode();
                        String valueId = (String) value.getProperty("id");
                    }
                }
            }
            // Separate valid and invalid entries
            for (Map.Entry<String, Object> entry : options.entrySet()) {
                String check = entry.getKey() + "-" + entry.getValue().toString();
                if (valid.contains(check)) {
                    validValues.put(entry.getKey(), entry.getValue().toString());
                } else {
                    invalidValues.put(entry.getKey(), entry.getValue().toString());
                }
            }

            results.put("valid", validValues);
            results.put("invalid", invalidValues);
        }

        // Return results
        return Stream.of(new MapResult(results));
    }


 
    @Description("CALL com.lenovo.options(material, options)")
    @Procedure(name = "com.lenovo.options", mode = Mode.READ)
    public Stream<MapResult> Options(@Name("material") String materialId, @Name("options") Map<String, String> options) {
		JsonFactory jsonfactory = new JsonFactory();
		Writer writer = new StringWriter();
		String json = null;
	
        Node material = db.findNode(Labels.Material, "id", materialId);
        HashMap<String, Object> results = new HashMap<>();
		String materialID = (String)  material.getProperty("id");
		HashMap<String, Set<String>> result;
		Set<String> list;
		Set<String> list2;
		HashMap<String, Object> configurationOptions = new HashMap<>();
		HashMap<String, String> charsToVisit = new HashMap<>();
		
		
		String tmpCharValue = "";
        // Verify material exists or return error.
        if (material == null) {
            return Stream.of(new MapResult(results));
        } else {
            // Collect the valid options
			int i = 0;
			result = new HashMap<>();
			// Get list of possible Char Values
            for (Relationship r : material.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_BOM)) {
                Node variant = r.getEndNode();	
				String strCurrentValue = (String) variant.getProperty("char");				
				configurationOptions.put(strCurrentValue,variant);
				charsToVisit.put(strCurrentValue,strCurrentValue);
			}
			
			for (Entry<String,Object> pair : configurationOptions.entrySet()){
			            //iterate over the pairs
				Node tmpNode = (Node)pair.getValue();
				String strCurrentChar = pair.getKey();
				list  = new HashSet<>();
				list2 = new HashSet<>();
				
				System.out.println(strCurrentChar);
				
					// If the value is passed in, add it to the result.
					// However, I think we need to traverse from the result here and then remove it from the list of options.
					// Need to gather proper char
				// list of chars to visit	
				if (charsToVisit.get(strCurrentChar) !=null){
					
					if (options.get(strCurrentChar) !=null){
						tmpCharValue = options.get(strCurrentChar);
						list.add((String)(tmpCharValue));
						result.put(strCurrentChar, list);
						charsToVisit.remove(strCurrentChar);

						
						for (Relationship rHasValue : tmpNode.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_VARIANT)) {
							String targetChar = "";
							Node varTabNode = rHasValue.getEndNode();
							boolean active = (boolean) varTabNode.getProperty("active", true);
	                    	if (active) {
								for (Relationship rVarTabNodeRel : varTabNode.getRelationships(Direction.INCOMING, RelationshipTypes.HAS_VARIANT)) {
									Node varTabValue = rVarTabNodeRel.getStartNode();
									if (options.get(varTabValue.getProperty("char")) !=null){
//											list2.add(((String) varTabValue.getProperty("charValue")));
//											charsToVisit.remove(((String) varTabValue.getProperty("char")));
										}else{

											if (!varTabValue.getProperty("char").equals(strCurrentChar)){
												targetChar = (String) varTabValue.getProperty("char");
												list2.add(((String) varTabValue.getProperty("charValue")));
												charsToVisit.remove(((String) varTabValue.getProperty("char")));
											}
									}
								}
								result.put(targetChar,list2);
							}
						}
						
						
						
						
						
					
					}else{
						tmpCharValue = strCurrentChar;
						if (strCurrentChar.equals("EPMEMORY_SELECTION")) {
							System.out.println("EPMEMORY_SELECTION");
						}
						
						for (Relationship rHasValue : tmpNode.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_VARIANT)) {
							Node varTabNode = rHasValue.getEndNode();
							boolean active = (boolean) varTabNode.getProperty("active", true);
	                    	if (active) {
								for (Relationship rVarTabNodeRel : varTabNode.getRelationships(Direction.INCOMING, RelationshipTypes.HAS_VARIANT)) {
									Node varTabValue = rVarTabNodeRel.getStartNode();
									if (options.get(varTabValue.getProperty("char")) !=null){
											list.add(((String) varTabValue.getProperty("charValue")));
											charsToVisit.remove(((String) varTabValue.getProperty("charValue")));
										}else{
											if (!varTabValue.getProperty("char").equals(tmpCharValue)){	
												list.add(((String) varTabValue.getProperty("charValue")));
											}
									}
								}
							}
						}
						result.put(tmpCharValue, list);
//						System.out.println(tmpString);
				}
				}
            }
			results.put(materialID, result);
		
			
        }



        // Return results
        return Stream.of(new MapResult(results));
    }



   // Step 1: Create the Vector Nodes
    @Description("CALL com.lenovo.oldoptions(material, options)")
    @Procedure(name = "com.lenovo.oldoptions", mode = Mode.READ)
    public Stream<StringResult> oldOptions(@Name("material") String materialId, @Name("options") Map<String, String> options) {
		JsonFactory jsonfactory = new JsonFactory();
		Writer writer = new StringWriter();
		String json = null;

        Node material = db.findNode(Labels.Material, "id", materialId);
        HashMap<String, Object> results = new HashMap<>();
		String materialID = (String)  material.getProperty("id");
		HashMap<String, List<String>> variantMap = new HashMap<String, List<String>>();
		HashMap<String, String> variantValueMap = new HashMap<String, String>();
		String tmpString = "";
        // Verify material exists or return error.
        if (material == null) {
            return Stream.of(new StringResult(json));
        } else {
            // Collect the valid options
			List<String> arraylist1 = new ArrayList<String>();
			List<String> variantValueList = new ArrayList<String>();

			int i = 0;
            for (Relationship r : material.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_BOM)) {
                Node variant = r.getEndNode();

				arraylist1.add(((String) variant.getProperty("id")));
				String strCurrentValue = (String) variant.getProperty("id");

				if (options.get(strCurrentValue) !=null){
					tmpString = options.get(strCurrentValue);
				}else{
					for (Relationship rHasValue : variant.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_VARIANT)) {
						boolean active = (boolean) rHasValue.getProperty("active", true);
	                    if (active) {
							Node variantValue = rHasValue.getEndNode();
							variantValueList.add(((String) variantValue.getProperty("id")));
							tmpString = tmpString + "\"" + (String) variantValue.getProperty("id") + ",\"";
						}
					}
				}
				variantValueMap.put((String) variant.getProperty("id"),tmpString);
				tmpString = "";
				variantValueList.clear();
            }
			arraylist1.sort(String::compareToIgnoreCase);
			variantMap.put(materialID,arraylist1);

        }
		Iterator<String> countryIterator = options.keySet().iterator();
		while (countryIterator.hasNext()) {
		    String code = countryIterator.next();
		    System.out.println(code);
		}

		try {

		        JsonGenerator jsonGenerator = jsonfactory.createJsonGenerator(writer);
		        jsonGenerator.writeStartObject();
		        jsonGenerator.writeArrayFieldStart("Material");
		        jsonGenerator.writeStartObject();
				jsonGenerator.writeArrayFieldStart("Options");
				for (Map.Entry<String, String> entry : variantValueMap.entrySet()) {
					String check = "\"" + entry.getKey() + "\":" + entry.getValue().toString();
					jsonGenerator.writeString(check);
				}
		        jsonGenerator.writeEndArray();
		        jsonGenerator.writeEndObject();

				jsonGenerator.writeEndArray();
		        jsonGenerator.writeEndObject();
		        jsonGenerator.close();
		        json = writer.toString();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
        // Return results
        return Stream.of(new StringResult(json));
    }

  public static Object getKeyFromValue(Map hm, Object value) {
    for (Object o : hm.keySet()) {
      if (hm.get(o).equals(value)) {
        return o;
      }
    }
    return null;
  }
    }
