package com.asavka.quickmessage;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class ContactInfo {
	private int mId;
	private String mNumber;
	private String mLookupKey;
	private String mName;
	private int mTemplateId;
	
	/**
	 * Create empty contact info object
	 */
	public ContactInfo() {
		super();
		
		setId(-1);
	}
	
	/**
	 * Get contact info from ContactsContract by lookup key 
	 * and initialize object using gotten data
	 * @param phoneNumber - contact number for message sending
	 * @param lookupKey - contact lookup key for getting 
	 * 		info from ContactContract
	 * @param context
	 */
	public ContactInfo(final String phoneNumber, final String lookupKey, Context context) {
		super();
				
		/** Clear previous values  */
		setId(-1);
		setContactLookupKey(lookupKey, context);
		setContactNumber(phoneNumber);
	}
	
	/**
	 * Get contact info from ContactsContract by lookup key 
	 * and initialize object using gotten data
	 * @param id - contact info id. Need for saving in db
	 * @param phoneNumber - contact number for message sending
	 * @param lookupKey - contact lookup key for getting 
	 * 		info from ContactContract
	 * @param context
	 */
	public ContactInfo(final int id, final String phoneNumber, final String lookupKey, Context context) {
		super();
				
		/** Clear previous values  */
		setId(id);
		setContactLookupKey(lookupKey, context);
		setContactNumber(phoneNumber);
	}
	
	@Override
	public String toString() {
		return getContactLookupKey() + ":" + getContactNumber();
	}
	
	public boolean isValid() {
		return (getContactNumber() != null && getContactNumber().length() > 0);
	}
	
	/************************************************************************/
	/** Getters/setters														*/
	/************************************************************************/
	public int getId() {
		return mId;
	}
	
	public void setId(final int id) {
		mId = id;
	}
	
	public String getContactName() {
		return (mName != null && mName.length() > 0) ? mName : "Unknown";
	}
	
	public void setContactName(final String name) {
		mName = name;
	}
	
	public String getContactNumber() {
		return mNumber;
	}
	
	public void setContactNumber(final String number) {
		mNumber = number;
	}
	
	public String getContactLookupKey() {
		return mLookupKey;
	}
	
	/**
	 * Set new lookup key value and update contact display name and photo
	 * @param lookupKey
	 * @param context
	 */
	public void setContactLookupKey(final String lookupKey, Context context) {
		if (lookupKey.equals(mLookupKey))
			return;
		
		mLookupKey = lookupKey;
		
		// Update display name and photo
		if (context == null) {
			System.err.print("Can not take ContactsContract information for null ointer context");
			return;
		}
		
		/** Get contact info from ContactContracts*/
		String[] projection = {Phone.DISPLAY_NAME};
		String selection = Phone.LOOKUP_KEY + "= ?";
		String[] whereArgs = {lookupKey};
		Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection, selection, whereArgs, null);
		if (cursor == null) {
			System.err.print("Can not take contact for lookup key '" + lookupKey + "'");
			return;
		}
		
		int nameId = cursor.getColumnIndex(Phone.DISPLAY_NAME);
		if (cursor.moveToFirst()) {
			setContactName(cursor.getString(nameId));
		}
		
		cursor.close();
	}
	
	public int getMessageTemplateId() {
		return mTemplateId;
	}
	
	public void setMessageTemplateId(final int templateId) {
		mTemplateId = templateId;
	}
}
