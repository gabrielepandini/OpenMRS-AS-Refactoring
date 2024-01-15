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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openmrs.BaseChangeableOpenmrsMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the metadata for a task that can be scheduled.
 */
public class TaskDefinition extends BaseChangeableOpenmrsMetadata {
	
	private static final Logger log = LoggerFactory.getLogger(TaskDefinition.class);
	
	// Task metadata
	private Integer id;
	
	// This class must implement the schedulable interface or it will fail to start
	private String taskClass;
	
	private ITask taskInstance = null;
	
	// Scheduling metadata
	private Date startTime;
	
	private Date lastExecutionTime;
	
	private Long repeatInterval; // NOW in seconds to give us ability to
	
	// support longer intervals (years, decades,
	// milleniums)
	
	private Boolean startOnStartup;
	
	private String startTimePattern;
	
	private Boolean started;
	
	// Relationships
	private Map<String, String> properties;
	
	/**
	 * Default no-arg public constructor
	 */
	public TaskDefinition() {
		this.started = Boolean.FALSE; // default
		this.startTime = new Date(); // makes it easier during task creation
		// as we have a default date populated
		this.properties = new HashMap<>();
	}
	
	/**
	 * Public constructor
	 */
	public TaskDefinition(Integer id, String name, String description, String taskClass) {
		this();
		log.debug("Creating taskconfig: " + id);
		this.id = id;
		setName(name);
		setDescription(description);
		this.taskClass = taskClass;
	}

	/**
	 * Gets the next execution time based on the initial start time (possibly years ago, depending
	 * on when the task was configured in OpenMRS) and the repeat interval of execution. We need to
	 * calculate the "next execution time" because the scheduled time is most likely in the past and
	 * the JDK timer will run the task X number of times from the start time until now in order to
	 * catch up. The assumption is that this is not the desired behavior -- we just want to execute
	 * the task on its next execution time. For instance, say we had a scheduled task that ran every
	 * 24 hours at midnight. In the database, the task would likely have a past start date (e.g.
	 * 04/01/2006 12:00am). If we scheduled the task using the JDK Timer
	 * scheduleAtFixedRate(TimerTask task, Date startDate, int interval) method and passed in the
	 * start date above, the JDK Timer would execute this task once for every day between the start
	 * date and today, which would lead to hundreds of unnecessary (and likely expensive)
	 * executions.
	 * 
	 * @see java.util.Timer
	 * @param taskDefinition the task definition to be executed
	 * @return the next "future" execution time for the given task
	 * <strong>Should</strong> get the correct repeat interval
	 */
	public static Date getNextExecution(TaskDefinition taskDefinition) {
		Calendar nextTime = Calendar.getInstance();
		
		try {
			Date firstTime = taskDefinition.getStartTime();
			
			if (firstTime != null) {
				
				// Right now
				Date currentTime = new Date();
				
				// If the first time is actually in the future, then we use that date/time
				if (firstTime.after(currentTime)) {
					return firstTime;
				}
				
				// The time between successive runs (e.g. 24 hours)
				long repeatInterval = taskDefinition.getRepeatInterval();
				if (repeatInterval == 0) {
					// task is one-shot so just return the start time
					return firstTime;
				}
				
				// Calculate time between the first time the process was run and right now (e.g. 3 days, 15 hours)
				long betweenTime = currentTime.getTime() - firstTime.getTime();
				
				// Calculate the last time the task was run   (e.g. 15 hours ago)
				long lastTime = (betweenTime % (repeatInterval * 1000));
				
				// Calculate the time to add to the current time (e.g. 24 hours - 15 hours = 9 hours)
				long additional = ((repeatInterval * 1000) - lastTime);
				
				nextTime.setTime(new Date(currentTime.getTime() + additional));
				
				log.debug("The task " + taskDefinition.getName() + " will start at " + nextTime.getTime());
			}
		}
		catch (Exception e) {
			log.error("Failed to get next execution time for " + taskDefinition.getName(), e);
		}
		
		return nextTime.getTime();
	}

	/**
	 * Get the task identifier.
	 * 
	 * @return <code>Integer</code> identifier of the task
	 */
	@Override
	public Integer getId() {
		return this.id;
	}
	
	/**
	 * Set the task identifier.
	 * 
	 * @param id
	 */
	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * Get the data map used to provide the task with runtime data.
	 * 
	 * @return the data map
	 */
	public Map<String, String> getProperties() {
		return this.properties;
	}
	
	/**
	 * Set the properties of the task. This overrides any properties previously set with the
	 * setProperty(String, String) method.
	 * 
	 * @param properties <code>Map&lt;String, String&gt;</code> of the properties to set
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	/**
	 * Get the schedulable object to be executed.
	 * 
	 * @return the schedulable object
	 */
	public String getTaskClass() {
		return this.taskClass;
	}
	
	/**
	 * Set the schedulable object to be executed.
	 * 
	 * @param taskClass <code>String</code> taskClass of a schedulable object
	 */
	public void setTaskClass(String taskClass) {
		this.taskClass = taskClass;
	}
	
	/**
	 * Get the start time for when the task should be executed.
	 * 
	 * @return long start time
	 */
	public Date getStartTime() {
		return startTime;
	}
	
	/**
	 * Set the start time for when the task should be executed. For instance, use "new Date()", if
	 * you want it to start now.
	 * 
	 * @param startTime start time for the task
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * Get the time the task was last executed.
	 * 
	 * @return long last execution time
	 */
	public Date getLastExecutionTime() {
		return lastExecutionTime;
	}
	
	/**
	 * Set the time the task was last executed
	 * 
	 * @param lastExecutionTime last execution time
	 */
	public void setLastExecutionTime(Date lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}
	
	/**
	 * Gets the number of seconds until task is executed again.
	 * 
	 * @return long number of seconds.
	 */
	public Long getRepeatInterval() {
		return repeatInterval;
	}
	
	/**
	 * Sets the number of seconds until task is executed again.
	 * 
	 * @param repeatInterval number of seconds, or 0 to indicate to repetition
	 */
	public void setRepeatInterval(Long repeatInterval) {
		this.repeatInterval = repeatInterval;
	}
	
	/**
	 * Get the date format used to set the start time.
	 */
	public String getStartTimePattern() {
		return this.startTimePattern;
	}
	
	/**
	 * Sets the date format used to set the start time.
	 */
	public void setStartTimePattern(String pattern) {
		this.startTimePattern = pattern;
	}
	
	/**
	 * Gets the flag that indicates whether the task should startup as soon as the scheduler starts.
	 */
	public Boolean getStartOnStartup() {
		return this.startOnStartup;
	}
	
	/**
	 * Sets the flag that indicates whether the task should startup as soon as the scheduler starts.
	 */
	public void setStartOnStartup(Boolean startOnStartup) {
		this.startOnStartup = startOnStartup;
	}
	
	/**
	 * Gets the flag that indicates whether the task has been started.
	 */
	public Boolean getStarted() {
		return this.started;
	}
	
	/**
	 * Sets the flag that indicates whether the task has been started.
	 */
	public void setStarted(Boolean started) {
		this.started = started;
	}
	
	/**
	 * Get task configuration property.
	 * 
	 * @param key the <code>String</code> key of the property to get
	 * @return the <code>String</code> value for the given key
	 */
	public String getProperty(String key) {
		return this.properties.get(key);
	}
	
	/**
	 * Set task configuration property. Only supports strings at the moment.
	 * 
	 * @param key the <code>String</code> key of the property to set
	 * @param value the <code>String</code> value of the property to set
	 */
	public void setProperty(String key, String value) {
		this.properties.put(key, value);
	}
	
	/**
	 * Convenience method that asks SchedulerUtil for it's next execution time.
	 * 
	 * @return the <code>Date</code> of the next execution
	 */
	public Date getNextExecutionTime() {
		return getNextExecution(this);
	}
	
	/**
	 * Convenience method to calculate the seconds until the next execution time.
	 * 
	 * @return the number of seconds until the next execution
	 */
	public long getSecondsUntilNextExecutionTime() {
		return (getNextExecutionTime().getTime() - System.currentTimeMillis()) / 1000;
		
	}
	
	// ==================================   Metadata ============================
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[TaskDefinition " + " id=" + getId() + " name=" + getName() + " class=" + getTaskClass() + " startTime="
		        + getStartTime() + " repeatInterval=" + this.getRepeatInterval() + " secondsUntilNext="
		        + this.getSecondsUntilNextExecutionTime() + "]";
	}
	
	/**
	 * Gets the runnable task instance associated with this definition.
	 *
	 * @return related task, or null if none instantiated (definition hasn't been scheduled)
	 */
	public ITask getTaskInstance() {
		return taskInstance;
	}
	
	/**
	 * Sets the runnable task instance associated with this definition. This should be set by the
	 * scheduler which instantiates the task.
	 * 
	 * @param taskInstance
	 */
	public void setTaskInstance(ITask taskInstance) {
		this.taskInstance = taskInstance;
	}
}
