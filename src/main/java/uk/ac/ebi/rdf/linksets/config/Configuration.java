/**
 * Configuration manager, see also config.properties.
 */
package uk.ac.ebi.rdf.linksets.config;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ebi.rdf.linksets.exceptions.MatcherExcepction;

import org.apache.log4j.Logger;

/**
 * @author LeylaJael
 *
 */
public class Configuration {
	public static final String RDF_TYPE_PROP = Prefix.RDF.getAddress() + "type";
	private static Logger logger = Logger.getLogger(Configuration.class);
	private static ResourceBundle res = ResourceBundle.getBundle("config");
	private static List<String> mergeKeys = null;
	
	/**
	 * Retrieves a configuration property for a given key, if any problem arises "" will be return.
	 * Use this method if you want to avoid default values used in the other more key specific methods in this class.
	 * @param prop
	 * @return
	 */
	public static String getProperty(String key) {
        try {
            return res.getString(key);
        } catch (Exception e) {
        	logger.warn("---WARNING configuration---: " + e.getMessage());
            return ("");
        }
    }
	
	/**
	 * True if a given type (most specifically URI type) should be excluded.
	 * @param string URI type.
	 * @return
	 */
	public static boolean shouldBeExcluded(String type) {
		String[] exclusions = {"http://www.ifomis.org/","http://www.w3.org/2002/","http://www.w3.org/ns/sparql-service-description/","http://rdfs.org/ns/void/"};
		try {
			exclusions = res.getString("types.exclude").split(",");			
		} catch (Exception e) {}
		for (String exclude: exclusions){
			if (type.startsWith(exclude)) {
				return true;
			}
		}
		return false;
	}
	
	private static void initMergeKeys() {
		if (Configuration.mergeKeys == null) {
			Configuration.mergeKeys = new ArrayList<String>();
			for (String key: res.keySet()) {
				if (key.startsWith("type.merge")) {
					mergeKeys.add(key);
				}
			}
		}
	}
	
	public static String merge(String type) throws MatcherExcepction {
		Configuration.initMergeKeys();
		for (String mergeKey: Configuration.mergeKeys) {
			String[] merge;
			String regex;
			try {
				merge = res.getString(mergeKey).split(",");
				regex = merge[0];
			} catch (Exception e) {
				continue;
			}
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(type);
			if (matcher.find()) {
				StringBuilder result = new StringBuilder();
				for (int i = 1; i< merge.length; i++) {
					if (merge[i].isEmpty()) {
						continue;
					}
					if (merge[i].startsWith("$")) {
						try {
							int group = Integer.parseInt(merge[i].substring(1));
							result.append(matcher.group(group));
						} catch (NumberFormatException nfe) {
							throw new uk.ac.ebi.rdf.linksets.exceptions.MatcherExcepction ("Merge was not possible for type " + type);
						}
					} else {
						result.append(merge[i]);
					}					
				}
				return result.toString();
			}
		}
		return null;
	}
}
