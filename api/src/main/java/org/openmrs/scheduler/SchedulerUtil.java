/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerUtil {
	
	private SchedulerUtil() {
	}
	
	private static final Logger log = LoggerFactory.getLogger(SchedulerUtil.class);
	
	/**
	 * Start the scheduler given the following start up properties.
	 * 
	 * @param p properties used to start the service
	 */
	public static void startup(Properties p) {
		// Override the Scheduler constants if specified by the user
		
		String val = p.getProperty("scheduler.username", null);
		if (val != null) {
			SchedulerConstants.SCHEDULER_DEFAULT_USERNAME = val;
			log.warn("Deprecated runtime property: scheduler.username. Value set in global_property in database now.");
		}
		
		val = p.getProperty("scheduler.password", null);
		if (val != null) {
			SchedulerConstants.SCHEDULER_DEFAULT_PASSWORD = val;
			log.warn("Deprecated runtime property: scheduler.username. Value set in global_property in database now.");
		}
		
		// TODO: do this for all services
		try {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
			SchedulerService schedulerService;
			try {
				schedulerService = Context.getSchedulerService();
			}
			catch (APIException e) {
				log.warn("Could not notify the scheduler service about startup", e);
				return;
			}
			
			schedulerService.onStartup();
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
		}
	}
	
	/**
	 * Shutdown the scheduler service that is statically associated with the Context class.
	 */
	public static void shutdown() {
		SchedulerService service = null;
		
		// ignores errors while getting the scheduler service 
		try {
			service = Context.getSchedulerService();
		}
		catch (Exception e) {
			// pass
		}
		
		// TODO: Do this for all services
		try {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
			// doesn't attempt shutdown if there was an error getting the scheduler service
			if (service != null) {
				service.onShutdown();
			}
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
		}
		
	}
	
	/**
	 * Sends an email with system information and the given exception
	 * 
	 * @param throwable
	 */
	public static void sendSchedulerError(Throwable throwable) {
		try {
			Context.openSession();
			
			Boolean emailIsEnabled = Boolean.valueOf(Context.getAdministrationService().getGlobalProperty(
			    SchedulerConstants.SCHEDULER_ADMIN_EMAIL_ENABLED_PROPERTY));
			
			if (emailIsEnabled) {
				// Email addresses seperated by commas
				String recipients = Context.getAdministrationService().getGlobalProperty(
				    SchedulerConstants.SCHEDULER_ADMIN_EMAIL_PROPERTY);
				
				// Send message if 
				if (recipients != null) {
					
					// TODO need to use the default sender for the application 
					String sender = SchedulerConstants.SCHEDULER_DEFAULT_FROM;
					String subject = SchedulerConstants.SCHEDULER_DEFAULT_SUBJECT + " : " + throwable.getClass().getName();
					StringBuilder message = new StringBuilder();
					message.append("\n\nStacktrace\n============================================\n");
					message.append(SchedulerUtil.getExceptionAsString(throwable));
					message.append("\n\nSystem Variables\n============================================\n");
					for (Map.Entry<String, String> entry : Context.getAdministrationService().getSystemVariables()
					        .entrySet()) {
						message.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
					}
					
					// TODO need to the send the IP information for the server instance that is running this task
					
					log.debug("Sending scheduler error email to [" + recipients + "] from [" + sender + "] with subject ["
					        + subject + "]:\n" + message);
					Context.getMessageService().sendMessage(recipients, sender, subject, message.toString());
				}
				
			}
			
		}
		catch (Exception e) {
			// Log, but otherwise suppress errors
			log.warn("Could not send scheduler error email: ", e);
		}
		finally {
			Context.closeSession();
		}
	}
	
	/**
	 * @param t
	 * @return <code>String</code> representation of the given exception
	 */
	public static String getExceptionAsString(Throwable t) {
		return ExceptionUtils.getStackTrace(t);
	}
	
}
