/**
 * Possible running modes... so far only one supported...
 */
package uk.ac.ebi.rdf.linksets.config;

/**
 * @author LeylaJael
 *
 */
public enum ModeAndFile {
	EXTRACT("extract", "linksets.rdf");
	
	private String mode, fileName;
	
	public String getMode() {
		return mode;
	}

	public String getFileName() {
		return fileName;
	}

	private ModeAndFile(String mode, String fileName) {
		this.mode = mode;
		this.fileName = fileName;
	}
}
