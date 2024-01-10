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
