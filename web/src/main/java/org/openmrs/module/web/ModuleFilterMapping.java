/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.openmrs.module.Module;
import org.openmrs.module.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents the mapping of a Filter to a collection of Servlets and URLs
 */
public class ModuleFilterMapping implements Serializable {
	
	public static final long serialVersionUID = 1;
	
	private static final Logger log = LoggerFactory.getLogger(ModuleFilterMapping.class);
	
	private static final Deque<ModuleFilterMapping> EMPTY_DEQUE = new ArrayDeque<>(0);
	
	// Properties
	private Module module;
	
	private String filterName;
	
	private List<String> servletNames = new ArrayList<>();
	
	private List<String> urlPatterns = new ArrayList<>();
	
	/**
	 * Default constructor, requires a Module
	 * 
	 * @param module - the module to use to construct this ModuleFilterMapping
	 */
	public ModuleFilterMapping(Module module) {
		this.module = module;
	}
	
	/**
	 * @return - the {@link Module} that registered this FilterDefinition
	 */
	public Module getModule() {
		return module;
	}
	
	/**
	 * @param module the {@link Module}
	 */
	public void setModule(Module module) {
		this.module = module;
	}
	
	/**
	 * @return the name of the Filter
	 */
	public String getFilterName() {
		return filterName;
	}
	
	/**
	 * @param filterName the name of the Filter
	 */
	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}
	
	/**
	 * @return a List of all Servlet Names mapped to this Filter
	 */
	public List<String> getServletNames() {
		return servletNames;
	}
	
	/**
	 * @param servletNames a List of all Servlet Names mapped to this filter
	 */
	public void setServletNames(List<String> servletNames) {
		this.servletNames = servletNames;
	}
	
	/**
	 * Adds a Servlet name to the List of those mapped to this filter
	 * 
	 * @param servletName - The servlet name to add
	 */
	public void addServletName(String servletName) {
		this.servletNames.add(servletName);
	}
	
	/**
	 * @return - a List of all Url Patterns mapped to this filter
	 */
	public List<String> getUrlPatterns() {
		return urlPatterns;
	}
	
	/**
	 * @param urlPatterns a List of all Url Patterns mapped to this filter
	 */
	public void setUrlPatterns(List<String> urlPatterns) {
		this.urlPatterns = urlPatterns;
	}
	
	/**
	 * Adds a Url pattern to the List of those mapped to this filter
	 * 
	 * @param urlPattern - The urlPattern to add
	 */
	public void addUrlPattern(String urlPattern) {
		this.urlPatterns.add(urlPattern);
	}
	
	/**
	 * Return <code>true</code> if the passed Filter passes one or more filter mappings otherwise,
	 * return <code>false</code>.
	 * 
	 * @param filterMapping - The {@link ModuleFilterMapping} to check for matching servlets and url
	 *            patterns
	 * @param requestPath - The URI of the request to check against the {@link ModuleFilterMapping},
	 * 	      with the context path already removed (since module filter mappings are relative to the
	 *        context path).
	 * @return - true if the given {@link ModuleFilterMapping} matches the passed requestPath For
	 *         example: Passing a ModuleFilterMapping containing a urlPattern of "*" would return
	 *         true for any requestPath Passing a ModuleFilterMapping containing a urlPattern of
	 *         "*.jsp" would return true for any requestPath ending in ".jsp"
	 * <strong>Should</strong> return false if the requestPath is null
	 * <strong>Should</strong> return true if the ModuleFilterMapping contains any matching urlPatterns for this
	 *         requestPath
	 * <strong>Should</strong> return true if the ModuleFilterMapping contains any matching servletNames for this
	 *         requestPath
	 * <strong>Should</strong> return false if no matches are found for this requestPath
	 */
	public static boolean filterMappingPasses(ModuleFilterMapping filterMapping, String requestPath) {
		
		// Return false if url is null
		if (requestPath == null) {
			return false;
		}
		
		for (String patternToCheck : filterMapping.getUrlPatterns()) {
			if (urlPatternMatches(patternToCheck, requestPath)) {
				return true;
			}
		}
		for (String patternToCheck : filterMapping.getServletNames()) {
			if (servletNameMatches(patternToCheck, requestPath)) {
				return true;
			}
		}
		
		// If none found, return false
		return false;
	}
	
	/**
	 * Return <code>true</code> if the context-relative request path matches the patternToCheck
	 * otherwise, return <code>false</code>.
	 * 
	 * @param patternToCheck String pattern to check
	 * @param requestPath to check
	 * <strong>Should</strong> return false if the patternToCheck is null
	 * <strong>Should</strong> return true if the pattern is *
	 * <strong>Should</strong> return true if the pattern is /*
	 * <strong>Should</strong> return true if the pattern matches the requestPath exactly
	 * <strong>Should</strong> return true if the pattern matches everything up to a suffix of /*
	 * <strong>Should</strong> return true if the pattern matches by extension
	 * <strong>Should</strong> return false if no pattern matches
	 */
	public static boolean urlPatternMatches(String patternToCheck, String requestPath) {
		
		// Return false if patternToCheck is null
		if (patternToCheck == null) {
			return false;
		}
		
		log.debug("Checking URL <" + requestPath + "> against pattern <" + patternToCheck + ">");
		
		// Match exact or full wildcard
		if ("*".equals(patternToCheck) || "/*".equals(patternToCheck) || patternToCheck.equals(requestPath)) {
			return true;
		}
		
		// Match wildcard
		if (patternToCheck.endsWith("/*")) {
			int patternLength = patternToCheck.length() - 2;
			if (patternToCheck.regionMatches(0, requestPath, 0, patternLength)) {
				return requestPath.length() == patternLength || '/' == requestPath.charAt(patternLength);
			}
			return false;
		}
		
		// Case 3 - Extension Match
		if (patternToCheck.startsWith("*.")) {
			int slash = requestPath.lastIndexOf('/');
			int period = requestPath.lastIndexOf('.');
			int reqLen = requestPath.length();
			int patLen = patternToCheck.length();
			
			if (slash >= 0 && period > slash && period != reqLen - 1 && reqLen - period == patLen - 1) {
				return (patternToCheck.regionMatches(2, requestPath, period + 1, patLen - 2));
			}
		}
		
		// If no match found by here, return false
		return false;
	}
	
	/**
	 * Return <code>true</code> if the specified servlet name matches the filterMapping otherwise
	 * return <code>false</code>.
	 * 
	 * @param patternToCheck String pattern to check
	 * @param servletName Servlet Name to check
	 * <strong>Should</strong> return false if the patternToCheck is null
	 * <strong>Should</strong> return true if the pattern is *
	 * <strong>Should</strong> return true if the pattern matches the servlet name exactly
	 * <strong>Should</strong> return false if no pattern matches
	 */
	public static boolean servletNameMatches(String patternToCheck, String servletName) {
		
		// Return false if servletName is null
		if (servletName == null) {
			return false;
		}
		
		log.debug("Checking servlet <" + servletName + "> against pattern <" + patternToCheck + ">");
		
		// Match exact or full wildcard
		return ("*").equals(patternToCheck) || servletName.equals(patternToCheck);
		
		// If none found, return false
	}
	
	/**
	 * Static method to parse through a Module's configuration file and return a List of
	 * ModuleFilterMapping objects for which there are configuration elements. Expected XML Format:
	 * 
	 * <pre>
	 * 	&lt;filter-mapping&gt;
	 * 		&lt;filter-name&gt;MyFilterName&lt;/filter-name&gt;
	 * 		&lt;url-pattern&gt;The pattern of URLs to match&lt;/filter-class&gt;
	 * 	&lt;/filter-mapping&gt;
	 * or
	 * 	&lt;filter-mapping&gt;
	 * 		&lt;filter-name&gt;MyFilterName&lt;/filter-name&gt;
	 * 		&lt;servlet-name&gt;The servlet name to match&lt;/servlet-name&gt;
	 * 	&lt;/filter-mapping&gt;
	 * </pre>
	 * 
	 * @param module - The {@link Module} for which you want to retrieve the defined
	 *            {@link ModuleFilterMapping}s
	 * @return - a {@link Deque} of {@link ModuleFilterMapping}s that are defined for the passed
	 *         {@link Module}
	 */
	public static Deque<ModuleFilterMapping> retrieveFilterMappings(Module module){
		Deque<ModuleFilterMapping> mappings;
		
		try {
			Element rootNode = module.getConfig().getDocumentElement();
			NodeList mappingNodes = rootNode.getElementsByTagName("filter-mapping");
			if (mappingNodes.getLength() > 0) {
				mappings = new ArrayDeque<>(mappingNodes.getLength());
				for (int i = 0; i < mappingNodes.getLength(); i++) {
					ModuleFilterMapping mapping = new ModuleFilterMapping(module);
					Node node = mappingNodes.item(i);
					NodeList configNodes = node.getChildNodes();
					for (int j = 0; j < configNodes.getLength(); j++) {
						Node configNode = configNodes.item(j);
						switch (configNode.getNodeName()) {
							case "filter-name":
								mapping.setFilterName(configNode.getTextContent());
								break;
							case "url-pattern":
								mapping.addUrlPattern(configNode.getTextContent());
								break;
							case "servlet-name":
								mapping.addServletName(configNode.getTextContent());
								break;
						}
					}
					mappings.add(mapping);
				}
				
				log.debug("Retrieved {} filter-mappings for {}: {}", mappings.size(), module, mappings);
				return mappings;
			}
		}
		catch (Exception e) {
			throw new ModuleException("Unable to parse filters in module configuration.", e);
		}
		
		return EMPTY_DEQUE;
	}
}
