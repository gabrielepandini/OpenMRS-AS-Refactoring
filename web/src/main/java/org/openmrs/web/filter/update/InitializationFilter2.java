package org.openmrs.web.filter.update;

public class InitializationFilter2 {
	
	/**
	 * Variable set at the end of the wizard when spring is being restarted
	 */
	static boolean initializationComplete = false;
	
	/**
	 * Public method that returns true if database+runtime properties initialization is required
	 *
	 * @return true if this initialization wizard needs to run
	 */
	public static boolean initializationRequired() {
		return !isInitializationComplete();
	}
	
	/**
	 * Convenience variable to know if this wizard has completed successfully and that this wizard does
	 * not need to be executed again
	 *
	 * @return true if this has been run already
	 */
	static synchronized boolean isInitializationComplete() {
		return initializationComplete;
	}
}
