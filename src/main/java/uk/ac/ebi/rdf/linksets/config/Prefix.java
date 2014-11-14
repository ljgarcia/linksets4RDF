/**
 * RDF Prefixes
 */
package uk.ac.ebi.rdf.linksets.config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LeylaJael
 *
 */
public enum Prefix {
	RDF("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
	OWL("owl", "http://www.w3.org/2002/07/owl#"),
	VOID("void", "http://rdfs.org/ns/void#");
	
	private String namespace, address;
	private static Map<String, String> map = null;
	
	public String getNamespace() {
		return namespace;
	}

	public String getAddress() {
		return address;
	}
	
	public String getSparqlPrefix() {
		return "PREFIX " + this.getNamespace() + ": <" + this.getAddress() + "> ";
	}

	private Prefix(String namespace, String address) {
		this.namespace = namespace;
		this.address = address;
	}
	
	public static Map<String, String> getPrefixes() {
		if (Prefix.map == null) {
			Prefix.map = new HashMap<String, String>();
			for (Prefix pre: Prefix.values()) {
				Prefix.map.put(pre.getNamespace(), pre.getAddress());
			}
		}		
		return Prefix.map;
	}
}
