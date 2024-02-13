/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleUtil2 {
	
	private static final Logger log = LoggerFactory.getLogger(ModuleUtil2.class);
	
	/**
	 * This method is an enhancement of {@link #compareVersion(String, String)} and adds support for
	 * wildcard characters and upperbounds. <br>
	 * <br>
	 * This method calls {@link ModuleUtil#checkRequiredVersion(String, String)} internally. <br>
	 * <br>
	 * The require version number in the config file can be in the following format:
	 * <ul>
	 * <li>1.2.3</li>
	 * <li>1.2.*</li>
	 * <li>1.2.2 - 1.2.3</li>
	 * <li>1.2.* - 1.3.*</li>
	 * </ul>
	 * <p>
	 * Again the possible require version number formats with their interpretation:
	 * <ul>
	 * <li>1.2.3 means 1.2.3 and above</li>
	 * <li>1.2.* means any version of the 1.2.x branch. That is 1.2.0, 1.2.1, 1.2.2,... but not 1.3.0, 1.4.0</li>
	 * <li>1.2.2 - 1.2.3 means 1.2.2 and 1.2.3 (inclusive)</li>
	 * <li>1.2.* - 1.3.* means any version of the 1.2.x and 1.3.x branch</li>
	 * </ul>
	 * </p>
	 *
	 * @param version openmrs version number to be compared
	 * @param versionRange value in the config file for required openmrs version
	 * @return true if the <code>version</code> is within the <code>value</code>
	 * <strong>Should</strong> allow ranged required version
	 * <strong>Should</strong> allow ranged required version with wild card
	 * <strong>Should</strong> allow ranged required version with wild card on one end
	 * <strong>Should</strong> allow single entry for required version
	 * <strong>Should</strong> allow required version with wild card
	 * <strong>Should</strong> allow non numeric character required version
	 * <strong>Should</strong> allow ranged non numeric character required version
	 * <strong>Should</strong> allow ranged non numeric character with wild card
	 * <strong>Should</strong> allow ranged non numeric character with wild card on one end
	 * <strong>Should</strong> return false when openmrs version beyond wild card range
	 * <strong>Should</strong> return false when required version beyond openmrs version
	 * <strong>Should</strong> return false when required version with wild card beyond openmrs version
	 * <strong>Should</strong> return false when required version with wild card on one end beyond openmrs version
	 * <strong>Should</strong> return false when single entry required version beyond openmrs version
	 * <strong>Should</strong> allow release type in the version
	 * <strong>Should</strong> match when revision number is below maximum revision number
	 * <strong>Should</strong> not match when revision number is above maximum revision number
	 * <strong>Should</strong> correctly set upper and lower limit for versionRange with qualifiers and wild card
	 * <strong>Should</strong> match when version has wild card plus qualifier and is within boundary
	 * <strong>Should</strong> not match when version has wild card plus qualifier and is outside boundary
	 * <strong>Should</strong> match when version has wild card and is within boundary
	 * <strong>Should</strong> not match when version has wild card and is outside boundary
	 * <strong>Should</strong> return true when required version is empty
	 */
	public static boolean matchRequiredVersions(String version, String versionRange) {
		// There is a null check so no risk in keeping the literal on the right side
		if (StringUtils.isNotEmpty(versionRange)) {
			String[] ranges = versionRange.split(",");
			for (String range : ranges) {
				// need to externalize this string
				String separator = "-";
				if (range.indexOf("*") > 0 || range.indexOf(separator) > 0 && (!isVersionWithQualifier(range))) {
					// if it contains "*" or "-" then we must separate those two
					// assume it's always going to be two part
					// assign the upper and lower bound
					// if there's no "-" to split lower and upper bound
					// then assign the same value for the lower and upper
					String lowerBound = range;
					String upperBound = range;
					
					int indexOfSeparator = range.indexOf(separator);
					while (indexOfSeparator > 0) {
						lowerBound = range.substring(0, indexOfSeparator);
						upperBound = range.substring(indexOfSeparator + 1);
						if (upperBound.matches("^\\s?\\d+.*")) {
							break;
						}
						indexOfSeparator = range.indexOf(separator, indexOfSeparator + 1);
					}
					
					// only preserve part of the string that match the following format:
					// - xx.yy.*
					// - xx.yy.zz*
					lowerBound = StringUtils.remove(lowerBound, lowerBound.replaceAll("^\\s?\\d+[\\.\\d+\\*?|\\.\\*]+", ""));
					upperBound = StringUtils.remove(upperBound, upperBound.replaceAll("^\\s?\\d+[\\.\\d+\\*?|\\.\\*]+", ""));
					
					// if the lower contains "*" then change it to zero
					if (lowerBound.indexOf("*") > 0) {
						lowerBound = lowerBound.replaceAll("\\*", "0");
					}
					
					// if the upper contains "*" then change it to maxRevisionNumber
					if (upperBound.indexOf("*") > 0) {
						upperBound = upperBound.replaceAll("\\*", Integer.toString(Integer.MAX_VALUE));
					}
					
					int lowerReturn = compareVersion(version, lowerBound);
					
					int upperReturn = compareVersion(version, upperBound);
					
					if (lowerReturn < 0 || upperReturn > 0) {
						log.debug("Version " + version + " is not between " + lowerBound + " and " + upperBound);
					} else {
						return true;
					}
				} else {
					if (compareVersion(version, range) < 0) {
						log.debug("Version " + version + " is below " + range);
					} else {
						return true;
					}
				}
			}
		}
		else {
			//no version checking if required version is not specified
			return true;
		}
		
		return false;
	}
	
	/**
	 * Compares <code>version</code> to <code>value</code> version and value are strings like
	 * 1.9.2.0 Returns <code>0</code> if either <code>version</code> or <code>value</code> is null.
	 *
	 * @param version String like 1.9.2.0
	 * @param value String like 1.9.2.0
	 * @return the value <code>0</code> if <code>version</code> is equal to the argument
	 *         <code>value</code>; a value less than <code>0</code> if <code>version</code> is
	 *         numerically less than the argument <code>value</code>; and a value greater than
	 *         <code>0</code> if <code>version</code> is numerically greater than the argument
	 *         <code>value</code>
	 * <strong>Should</strong> correctly comparing two version numbers
	 * <strong>Should</strong> treat SNAPSHOT as earliest version
	 */
	public static int compareVersion(String version, String value) {
		try {
			if (version == null || value == null) {
				return 0;
			}
			
			List<String> versions = new ArrayList<>();
			List<String> values = new ArrayList<>();
			String separator = "-";
			
			// strip off any qualifier e.g. "-SNAPSHOT"
			int qualifierIndex = version.indexOf(separator);
			if (qualifierIndex != -1) {
				version = version.substring(0, qualifierIndex);
			}
			
			qualifierIndex = value.indexOf(separator);
			if (qualifierIndex != -1) {
				value = value.substring(0, qualifierIndex);
			}
			
			Collections.addAll(versions, version.split("\\."));
			Collections.addAll(values, value.split("\\."));
			
			// match the sizes of the lists
			while (versions.size() < values.size()) {
				versions.add("0");
			}
			while (values.size() < versions.size()) {
				values.add("0");
			}
			
			for (int x = 0; x < versions.size(); x++) {
				String verNum = versions.get(x).trim();
				String valNum = values.get(x).trim();
				Long ver = NumberUtils.toLong(verNum, 0);
				Long val = NumberUtils.toLong(valNum, 0);
				
				int ret = ver.compareTo(val);
				if (ret != 0) {
					return ret;
				}
			}
		}
		catch (NumberFormatException e) {
			log.error("Error while converting a version/value to an integer: " + version + "/" + value, e);
		}
		
		// default return value if an error occurs or elements are equal
		return 0;
	}
	
	/**
	 * Checks for qualifier version (i.e "-SNAPSHOT", "-ALPHA" etc. after maven version conventions)
	 *
	 * @param version String like 1.9.2-SNAPSHOT
	 * @return true if version contains qualifier
	 */
	public static boolean isVersionWithQualifier(String version) {
		Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.(\\d+))?(\\-([A-Za-z]+))").matcher(version);
		return matcher.matches();
	}
}
