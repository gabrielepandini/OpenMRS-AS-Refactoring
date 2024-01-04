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

	/**
	 * @param specialTokens The specialTokens to set.
	 */
	public void setSpecialTokens(List<String> specialTokens) {
		this.specialTokens = specialTokens;
	}

}
