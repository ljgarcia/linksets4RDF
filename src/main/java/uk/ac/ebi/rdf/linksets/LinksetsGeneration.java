/**
 * This class implements the linksets extraction process for EBI-RDF. 
 * Although it has only be tested with Atlas Endpoint, it should work with any other dataset where dataset classes are
 * modeled as owl:Class and dataset object properties as owl:ObjectProperty.
 */
package uk.ac.ebi.rdf.linksets;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import uk.ac.ebi.rdf.linksets.config.ModeAndFile;

/**
 * @version 0.1.0
 * @author ljgarcia@ebi.ac.uk
 *
 */
public class LinksetsGeneration {	
	private static Logger logger = Logger.getLogger(LinksetsGeneration.class);
	/**
	 * Main class.
	 * @param args [mode, working directory, endpoint URI, dataset URI, offset], for more information see usage.
	 */
	public static void main(String[] args) {
		long intialTime = new Date().getTime();
		String usage = "Usage: java -jar linksetsGeneration.jar\n"
				+ "-m extract (only extract is currently supported)\n"
				+ "-w <working directory> directory where RDF output will be saved\n"
				+ "-ep <endpoint URI> endpoint for which you are generating linksets\n"
				+ "-ds <dataset URI> dataset URI\n"
				+ "-o <number> offset to process types (in case process drops in the middle)";
		if (args == null) {
			System.out.println(usage);
			System.exit(0);
		}
		PropertyConfigurator.configure("log4j.properties");	

		String runningMode = null, workingDir = null, endpoint = null, dataset = null;
		int offset = 0;
		//boolean addIssuedDate = false;
		for (int i = 0; i < args.length; i++) {
			String str = args[i];
			if (str.equalsIgnoreCase("-m")) {
				runningMode = args[++i];
			} else if (str.equalsIgnoreCase("-w")) {
				workingDir = args[++i];
			} else if (str.equalsIgnoreCase("-ep")) {
				endpoint = args[++i];
			} else if (str.equalsIgnoreCase("-ds")) {
				dataset = args[++i];
			} else if (str.equalsIgnoreCase("-o")) {
				try {
					offset = Integer.parseInt(args[++i]);
				} catch (NumberFormatException nfe) {}				
			} 
		}
		
		if (workingDir == null) {
			System.out.println(usage);
			System.exit(0);
		}
		
		try {
			File dir = new File(workingDir);
			if (!dir.isDirectory()) {
				System.out.println(usage);
				System.exit(0);
			}
		} catch (Exception e) {
			logger.equals("Problem with working directory:" + e.getMessage());
			System.out.println(usage);
			System.exit(0);
		}
		String outputFileName = null;
		boolean parametersOk = true;
		if (runningMode.equalsIgnoreCase(ModeAndFile.EXTRACT.getMode())) {
			if ((endpoint != null) && (dataset != null)) {
				outputFileName = workingDir + "/" + ModeAndFile.EXTRACT.getFileName();
				try {
					LinksetsExtraction linksets = new LinksetsExtraction(dataset, endpoint, outputFileName, offset);
					linksets.extract();
				} catch (IOException e) {
					logger.error("Unxpected error while extracting classes and properties from " + endpoint + "\nCheck the output file at " + outputFileName);
				}
			} else {
				parametersOk = false;
			}
		} else {
			System.out.println(usage);
			System.exit(0);
		}
		
		if (!parametersOk) {
			System.out.println(usage);
			System.exit(0);
		}
		logger.info("Total time: " + (new Date().getTime() - intialTime));
	}

}
