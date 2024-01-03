package org.openmrs.layout;

import java.util.List;

public abstract class ALayoutSupport {
	protected List<String> specialTokens;
	
	/**
	 * @return Returns the specialTokens.
	 */
	public List<String> getSpecialTokens() {
		return specialTokens;
	}
}
