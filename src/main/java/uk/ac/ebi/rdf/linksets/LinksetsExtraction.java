/**
 * This class constructs the linksets triples for all types in a dataset.
 * Types taken into acoutn should be an owl:Class and be either the subject or object of an owl:ObjectProperty relation.
 * Some types are filtered out such as http://www.ifomis.org/,http://www.w3.org/2002/, some others are merged together like http://www.ebi.ac.uk/efo/*;
 * for more information about filtering and merging take a look at config.properties.
 * 
 * Initially the total number of classes is calculated but only those beyond the OFFSET are retrieved. Every 100 processed types, a linkset temporal RDF is written 
 * and the total number of types processed is informed. In such a way, if the process drops for any reason, it is possible to run it again with a new OFFSET. 
 * 
 * Linksets are written in the working directory specified as running option. If not all types are processed, the extension will be rdf.temp, otherwise will be just rdf.
 */
package uk.ac.ebi.rdf.linksets;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import uk.ac.ebi.rdf.linksets.config.Configuration;
import uk.ac.ebi.rdf.linksets.config.Prefix;
import uk.ac.ebi.rdf.linksets.config.VoidVocabulary;
import uk.ac.ebi.rdf.linksets.exceptions.MatcherExcepction;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * @author LeylaJael
 *
 */
public class LinksetsExtraction {
	/**
	 * Logger.
	 */
	private Logger logger = Logger.getLogger(LinksetsExtraction.class);
	/**
	 * Constants.
	 */
	private final static String SUBJECT_TYPE = "subjectType";
	private final static String OBJECT_TYPE = "objectType";
	private final static String PREDICATE = "predicate";
	private final static String ENTITY_TYPE = "entityType";
	private final static String COUNTER = "count";
	private final static int LIMIT = 100;
	/**
	 * Attributes.
	 */
	private String endpoint;
	private String output;
	private int offset;
	private ResultSet allTypesResultset;
	private Model model;
	private Resource dataset;
	private Property type;
	private Property classPartitionProp;
	private Property classProp;
	private Property linkPredicateProp;
	private Property subjectsTargetProp;
	private Property objectsTargetProp;
	private Property subsetProp;
	
	public LinksetsExtraction(String datasetURI, String endpoint, String output, int offset) {
		this.endpoint = endpoint;
		this.output = output;
		this.offset = offset;
		this.model = ModelFactory.createDefaultModel();
		this.model.setNsPrefixes(Prefix.getPrefixes());
		this.type = model.getProperty(Configuration.RDF_TYPE_PROP);
		this.dataset = model.createResource(datasetURI).addProperty(type, model.createResource(VoidVocabulary.CLASS_DATASET.getEntityURI()));
		this.classPartitionProp = model.getProperty(VoidVocabulary.PROP_CLASS_PARTITION.getEntityURI());
		this.classProp = model.getProperty(VoidVocabulary.PROP_CLASS.getEntityURI());
		this.linkPredicateProp = model.getProperty(VoidVocabulary.PROP_LINK_PREDICATE.getEntityURI());
		this.subjectsTargetProp = model.getProperty(VoidVocabulary.PROP_SUBJECTS_TARGET.getEntityURI());
		this.objectsTargetProp = model.getProperty(VoidVocabulary.PROP_OBJECTS_TARGET.getEntityURI());
		this.subsetProp = model.getProperty(VoidVocabulary.PROP_SUBSET.getEntityURI());
	}
	/**
	 * Query to retrieve how many distinct types (owL:Class) exist in the dataset.
	 */
	private static String queryHowManytypes = 
			Prefix.RDF.getSparqlPrefix() +
			Prefix.OWL.getSparqlPrefix() + 
			"SELECT (COUNT(DISTINCT ?" + LinksetsExtraction.ENTITY_TYPE + ") AS ?" + LinksetsExtraction.COUNTER + ")" + " " + 
			"WHERE { " + 
			"  ?entity rdf:type ?" + LinksetsExtraction.ENTITY_TYPE + " . " + 
			"  ?" + LinksetsExtraction.ENTITY_TYPE + " rdf:type owl:Class . " +
			"}" ;
	
	/**
	 * Constructs the query to retrieve all types from the dataset.
	 * @return
	 */
	private String getQueryAllTypes() {  
		String query =
			Prefix.RDF.getSparqlPrefix() + " " +
			Prefix.OWL.getSparqlPrefix() + " " + 
			"SELECT DISTINCT ?" + LinksetsExtraction.ENTITY_TYPE + " " + 
			"WHERE { " + 
			"  ?entity rdf:type ?" + LinksetsExtraction.ENTITY_TYPE + " . " + 
			"  ?" + LinksetsExtraction.ENTITY_TYPE + " rdf:type owl:Class . " +
			"} ORDER BY ?" + LinksetsExtraction.ENTITY_TYPE + " OFFSET " + this.offset;
		return query;
	}		
		
	/**
	 * Constructs the query to retrieve the (type, p, o) for a given type.
	 * @param type
	 * @return
	 */
	private String getQueryAType(String type) {
		String queryAType = 
			Prefix.RDF.getSparqlPrefix() + " " +
			Prefix.OWL.getSparqlPrefix() + " " + 
			"SELECT DISTINCT ?" + LinksetsExtraction.SUBJECT_TYPE + " ?" + LinksetsExtraction.PREDICATE + " ?" + LinksetsExtraction.OBJECT_TYPE + " " +
			"WHERE { " + 
			"  VALUES ?" + LinksetsExtraction.SUBJECT_TYPE + " {<" + type + ">} " +  
			"  ?subject ?" +  LinksetsExtraction.PREDICATE + " ?object .  " + 
			"  ?subject rdf:type ?" +LinksetsExtraction.SUBJECT_TYPE + " . " + 
			"  ?" + LinksetsExtraction.SUBJECT_TYPE + " rdf:type owl:Class . " + 
			"  ?object rdf:type ?" + LinksetsExtraction.OBJECT_TYPE + " . " + 
			"  ?" + LinksetsExtraction.OBJECT_TYPE + " rdf:type owl:Class . " + 
			"  ?" + LinksetsExtraction.PREDICATE + " rdf:type owl:ObjectProperty ." + 
			"}";
		return queryAType;
	}

	/**
	 * Returns the total number of types in the Dataset.
	 * @return
	 */
	private int getTypesNumber() {
		Query query = QueryFactory.create(LinksetsExtraction.queryHowManytypes, Syntax.syntaxARQ);
		QueryEngineHTTP httpQuery = new QueryEngineHTTP(this.endpoint, query);
		ResultSet results = httpQuery.execSelect();
		if (results.hasNext()) {
			QuerySolution solution = results.next();
			try {
				int count = solution.get(LinksetsExtraction.COUNTER).asLiteral().getInt();
				httpQuery.close();
				return count;
			} catch (Exception nfe) {
				logger.warn("It was not possible to parse the total number of types in the dataset");
			}
		} else {
			logger.warn("It was not possible to retrieve the total number of types in the dataset");
		}
		httpQuery.close();
		return 0;
	}
	
	/**
	 * Retrieve all types 
	 * @return
	 */
	private QueryEngineHTTP retrieveAllTypes()  {
		Query query = QueryFactory.create(this.getQueryAllTypes(), Syntax.syntaxARQ);
		QueryEngineHTTP httpQuery = new QueryEngineHTTP(this.endpoint, query);
		this.allTypesResultset = httpQuery.execSelect();
		//ResultSetRewindable results = ResultSetFactory.copyResults(this.allTypesResultset);
		//OutputStream temp = new FileOutputStream(outputName + ".temp");
		//ResultSetFormatter.outputAsTSV(temp, results);
		//httpQuery.close();
	    return httpQuery;	
	}
	
	/**
	 * Retrieves (s, p, o) triples for all types in the dataset.
	 * @return Number of types effectively processed.
	 * @throws Exception 
	 */
	private int retrieveOneByOne() throws Exception {
		int processed = 0;
		int toLinkset = 0;
		String type = null;
		try {
			//Query one by one except those to be filtered out. If the one to be filtered out is an object, filter out that triple from results.
			while (this.allTypesResultset.hasNext()) {
				type = this.allTypesResultset.next().get(LinksetsExtraction.ENTITY_TYPE).toString();
				if (!Configuration.shouldBeExcluded(type)) {
					Query query = QueryFactory.create(this.getQueryAType(type), Syntax.syntaxARQ);
					QueryEngineHTTP httpQuery = new QueryEngineHTTP(this.endpoint, query);
					ResultSet results = httpQuery.execSelect();				
					while (results.hasNext()) {
						//System.out.println("has results");
						QuerySolution solution = results.next();
						String subjectNode = solution.get(LinksetsExtraction.SUBJECT_TYPE).toString();
						String predicateNode = solution.get(LinksetsExtraction.PREDICATE).toString();
						String objectNode = solution.get(LinksetsExtraction.OBJECT_TYPE).toString();
						//System.out.println("To process: (" + subjectNode + ", " + predicateNode + ", " + objectNode + ")");
						if (Configuration.shouldBeExcluded(objectNode)) {
							continue;
						}
						try {
							String mergedSub = Configuration.merge(subjectNode);
							mergedSub = mergedSub == null ? subjectNode : mergedSub;
							String mergedObj = Configuration.merge(objectNode);
							mergedObj = mergedObj == null ? objectNode : mergedObj;
							this.addLinksetToModel(mergedSub, predicateNode, mergedObj);
							toLinkset++;
						} catch (MatcherExcepction e) {
							logger.warn("WARN - Ommitted triple (" + subjectNode + ", " + predicateNode + ", " + objectNode + "): " + e.getMessage());
						}					
					}
					httpQuery.close();				
				}
				processed++;
				if (processed % LinksetsExtraction.LIMIT == 0) {
					OutputStream output = new FileOutputStream(this.output + ".temp");
					this.model.write(output, "RDF/XML-ABBREV");
					logger.info("Processed so far " + processed + " types, from which only " + toLinkset + " are suitable for linksets.");
				}
			}
		} catch(Exception e) {
			logger.error("FATAL unexpected error, only " + processed + " types where actually processed. Type being processed was " + type + ".\n"
				+ "Last tried query was " + this.getQueryAType(type));
			OutputStream output = new FileOutputStream(this.output + ".temp");
			this.model.write(output, "RDF/XML-ABBREV");
			logger.info("Processed so far " + processed + " types, from which only " + toLinkset + " are suitable for linksets.");
			throw e;
		} 
		logger.info("All types processed, a total of " + processed + ", from which only " + toLinkset + " are suitable for linksets.");
		return processed;
	}
	
	/**
	 * Adds a linkset for a give (s, p, o) to the RDF model.
	 * @param sub
	 * @param pred
	 * @param obj
	 */
	private void addLinksetToModel(String sub, String pred, String obj) {
		//subject class partition
		Resource subRes = model.createResource(sub);
		Resource subResNode = model.createResource().addProperty(this.classProp, subRes);
		dataset.addProperty(this.classPartitionProp, subResNode);
		//object class partition
		Resource objRes = model.createResource(obj);
		Resource objResNode = model.createResource().addProperty(this.classProp, objRes);
		dataset.addProperty(this.classPartitionProp, objResNode);
		//link predicate
		Resource linkset = model.createResource().addProperty(this.type, model.createResource(VoidVocabulary.CLASS_LINKSET.getEntityURI()));
		linkset.addProperty(this.linkPredicateProp, model.createResource(pred));
		linkset.addProperty(this.subjectsTargetProp, subResNode);
		linkset.addProperty(this.objectsTargetProp, objResNode);
		linkset.addProperty(this.subsetProp, this.dataset);
	}
	
	/**
	 * Extracts the types and creates the linksets for a dataset.
	 * @throws Exception 
	 */
	public void extract() throws Exception {
		logger.info("Total number of types in the dataset: " + this.getTypesNumber() + ". Process starting at " + this.offset);
		logger.info("A temp file will be written every " + LinksetsExtraction.LIMIT + " types");
		QueryEngineHTTP httpQuery = this.retrieveAllTypes();
		this.retrieveOneByOne();
		httpQuery.close();
		OutputStream output = new FileOutputStream(this.output);
		this.model.write(output, "RDF/XML-ABBREV");
	}
}
