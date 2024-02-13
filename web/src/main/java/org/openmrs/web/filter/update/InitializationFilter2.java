/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
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
