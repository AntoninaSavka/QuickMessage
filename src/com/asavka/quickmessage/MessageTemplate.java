package com.asavka.quickmessage;

import android.content.Context;

public class MessageTemplate {
	private int mId;
	private String mSubject;
	private String mMessage;
	
	public MessageTemplate(final Context context) {
		super();
	}
	
	public MessageTemplate(final Context context, final int id, final String subject, final String message) {
		super();
		
		setId(id);
		setSubject(subject);
		setMessage(message);
	}
	
	/**
	 * Get message template unique id. Need for storing in db
	 * @return message template id
	 */
	public int getId() {
		return mId;
	}
	
	/**
	 * Set message template unique id. Need for storing in db
	 * @param id - new message template id
	 */
	public void setId(final int id) {
		mId = id;
	}
	
	/**
	 * Get message template subject
	 * @return message template subject
	 */
	public String getSubject() {
		return mSubject;
	}
	
	/**
	 * Set message template subject
	 * @param subject - message template subject
	 */
	public void setSubject(final String subject) {
		mSubject = subject;
	}
	
	/**
	 * Get message template body
	 * @return message template body
	 */
	public String getMessage() {
		return mMessage;
	}
	
	/**
	 * Set message template body
	 * @param message - message template body
	 */
	public void setMessage(final String message) {
		mMessage = message;
	}
}
