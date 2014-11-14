/**
 * VoID vocabulary classes and properties used in this project.
 */
package uk.ac.ebi.rdf.linksets.config;

/**
 * @author LeylaJael
 *
 */
public enum VoidVocabulary {
	CLASS_DATASET("Dataset"),
	CLASS_LINKSET("Linkset"),
	PROP_CLASS_PARTITION("classPartition"),
	PROP_CLASS("class"),
	PROP_LINK_PREDICATE("linkPredicate"),
	PROP_SUBJECTS_TARGET("subjectsTarget"),
	PROP_OBJECTS_TARGET("objectsTarget"),
	PROP_SUBSET("subset")
	;
	
	String entity;
	
	private VoidVocabulary(String entity) {
		this.entity = entity;
	}
	
	public String getEntityURI() {
		return Prefix.VOID.getAddress() + this.entity;
	}
}
