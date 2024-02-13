/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype;

import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;

public class CustomDatatypeUtil2 {
	public static final String DEFAULT_CUSTOM_DATATYPE = FreeTextDatatype.class.getName();

	/**
	 * @param descriptor
	 * @return a configured datatype appropriate for descriptor
	 */
	public static CustomDatatype<?> getDatatype(CustomValueDescriptor descriptor) {
		return getDatatype(descriptor.getDatatypeClassname(), descriptor.getDatatypeConfig());
	}

	/**
	 * @param datatypeClassname
	 * @param datatypeConfig
	 * @return a configured datatype with the given classname and configuration
	 */
	public static CustomDatatype<?> getDatatype(String datatypeClassname, String datatypeConfig) {
		try {
			Class dtClass = Context.loadClass(datatypeClassname);
			CustomDatatype<?> ret = (CustomDatatype<?>) Context.getDatatypeService().getDatatype(dtClass, datatypeConfig);
			if (ret == null) {
				throw new CustomDatatypeException("Can't find datatype: " + datatypeClassname);
			}
			return ret;
		}
		catch (Exception ex) {
			throw new CustomDatatypeException("Error loading " + datatypeClassname + " and configuring it with "
			        + datatypeConfig, ex);
		}
	}

	/**
	 * @param descriptor
	 * @return a configured datatype appropriate for descriptor
	 */
	public static CustomDatatype<?> getDatatypeOrDefault(CustomValueDescriptor descriptor) {
		try {
			return getDatatype(descriptor);
		}
		catch (CustomDatatypeException ex) {
			return getDatatype(DEFAULT_CUSTOM_DATATYPE, null);
		}
		
	}
}
