/**
 * Regex matcher exception, used when merging types together.
 */
package uk.ac.ebi.rdf.linksets.exceptions;

/**
 * @author LeylaJael
 *
 */
public class MatcherExcepction extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public MatcherExcepction() {
		super();
	}

	/**
	 * @param arg0
	 */
	public MatcherExcepction(String arg0) {
		super(arg0);		
	}

	/**
	 * @param arg0
	 */
	public MatcherExcepction(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public MatcherExcepction(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public MatcherExcepction(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
