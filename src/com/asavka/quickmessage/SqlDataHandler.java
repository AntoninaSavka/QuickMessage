package com.asavka.quickmessage;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class SqlDataHandler extends SQLiteOpenHelper implements QuickMessageDataHandler, BaseColumns {
	
	private static final int DB_VERSION = 1;
	
	private static final String DB_NAME = "QuickMessage";
	
	/** Tables */
	private static final String TABLE_TEMPLATES = "message_temlate";
	private static final String TABLE_CONTACT_INFO = "contact_info";
	
	/** Template table columns name */
	private static final String KEY_TEMPLATE_SUBJECT = "subject";
	private static final String KEY_TEMPLATE_MESSAGE = "message";
	
	/** Contact Info table columns name */
	private static final String KEY_CONTACT_INFO_NUMBER = "phone_number";
	private static final String KEY_CONTACT_INFO_LOOKUP_KEY = "lookup_key";
	private static final String KEY_CONTACT_INFO_TEMPLATE_ID = "template_id";
	
	private Context mContext = null;

	public SqlDataHandler(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE_TEMPLATE = "CREATE TABLE " + TABLE_TEMPLATES + "(" +
				_ID + " INTEGER primary key autoincrement," + KEY_TEMPLATE_SUBJECT +
				" TEXT UNIQUE NOT NULL," + KEY_TEMPLATE_MESSAGE + " TEXT" + ")";
		db.execSQL(CREATE_TABLE_TEMPLATE);

		String CREATE_CONTACT_INFO_TABLE = "CREATE TABLE " + TABLE_CONTACT_INFO + "(" +
				_ID + " INTEGER primary key autoincrement," + KEY_CONTACT_INFO_NUMBER + 
				" TEXT UNIQUE NOT NULL," + KEY_CONTACT_INFO_LOOKUP_KEY + " TEXT NOT NULL," + KEY_CONTACT_INFO_TEMPLATE_ID +
				" INTEGER"+ ")";
		db.execSQL(CREATE_CONTACT_INFO_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older tables if exist
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT_INFO);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEMPLATES);
		
		// Create new version tables
		onCreate(db);
	}

	/************************************************************************/
	/** TABLE_TEMPLATE queries												*/
	/************************************************************************/
	/**
	 * Add new message template into template table
	 * @param template - object that describes message template
	 * @return last insert row id
	 */
	@Override
	public int addTemplate(final MessageTemplate template) {
		SQLiteDatabase db = getWritableDatabase();
		
		if (db == null) {
			System.err.print("Can not add new message template: null pointer database");
			return -1;
		}
		
		ContentValues values = new ContentValues();
		values.put(KEY_TEMPLATE_SUBJECT, template.getSubject());
		values.put(KEY_TEMPLATE_MESSAGE, template.getMessage());
		
		// Insert raw
		db.insert(TABLE_TEMPLATES, null, values);
		
		int insertId = -1;
		Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
		if (cursor == null || !cursor.moveToFirst()) {
			System.err.print("Can not get last insert row id");
		} else {
			insertId = cursor.getInt(0);
		}
		
		cursor.close();
		db.close();
		return insertId;
	}
	
	/**
	 * Get message template from db by passed template id
	 * @param id - message template id
	 * @return message template if selection by id was succeed, null - if fail 
	 */
	@Override
	public MessageTemplate getMessageTemplate(final int id) {
		SQLiteDatabase db = getReadableDatabase();
		
		if (db == null) {
			System.err.print("Can not get message template: null pointer database");
			return null;
		}
		
		String[] projection = {KEY_TEMPLATE_SUBJECT, KEY_TEMPLATE_MESSAGE};
		String selection = _ID + " = ?";
		String[] selectArgs ={String.valueOf(id)};
		Cursor cursor = db.query(TABLE_TEMPLATES, projection, selection, selectArgs, null, null, null);
		if (cursor == null || !cursor.moveToFirst()) {
			System.err.print("Can not get message template: select query return null");
			db.close();
			return null;
		}
		
		final int subjectColumnId = cursor.getColumnIndex(KEY_TEMPLATE_SUBJECT);
		final int messageColumnId = cursor.getColumnIndex(KEY_TEMPLATE_MESSAGE);
		
		MessageTemplate template = new MessageTemplate(mContext);
		template.setId(id);
		template.setSubject(cursor.getString(subjectColumnId));
		template.setMessage(cursor.getString(messageColumnId));
		
		cursor.close();
		db.close();
		return template;
	}
	
	/**
	 * Get all stored message templates
	 * @return message template collection
	 */
	@Override
	public ArrayList<MessageTemplate> getAllMessageTemlates() {
		ArrayList<MessageTemplate> templateList = new ArrayList<MessageTemplate>();
		
		SQLiteDatabase db = getReadableDatabase();
		
		if (db == null) {
			System.err.print("Can not get message template: null pointer database");
			return templateList;
		}
		
		String[] projection = {_ID, KEY_TEMPLATE_SUBJECT, KEY_TEMPLATE_MESSAGE};
		Cursor cursor = db.query(TABLE_TEMPLATES, projection, null, null, null, null, null);
		if (cursor == null || !cursor.moveToFirst()) {
			System.err.print("Can not get message template: select query return null");
			db.close();
			return templateList;
		}
		
		final int idColumnId = cursor.getColumnIndex(_ID);
		final int subjectColumnId = cursor.getColumnIndex(KEY_TEMPLATE_SUBJECT);
		final int messageColumnId = cursor.getColumnIndex(KEY_TEMPLATE_MESSAGE);
		
		do {
			MessageTemplate template = new MessageTemplate(mContext);
			template.setId(cursor.getInt(idColumnId));
			template.setSubject(cursor.getString(subjectColumnId));
			template.setMessage(cursor.getString(messageColumnId));
			
			templateList.add(template);
		} while (cursor.moveToNext());
		
		cursor.close();
		db.close();
		return templateList;
	}
	
	/**
	 * Get message templates count
	 * @return message templates count in db, -1 if error was occurred
	 */
	@Override
	public int getMessageTemlatesCount() {
		SQLiteDatabase db = getReadableDatabase();
		
		if (db == null) {
			System.err.print("Can not get message templates count: null pointer database");
			return -1;
		}
		
		final String query = "SELECT * FROM " + TABLE_TEMPLATES;
		Cursor cursor = db.rawQuery(query, null);
		if (cursor == null) {
			System.err.print("Can not get message templates count: query return null");
			db.close();
			return -1;
		}
		cursor.close();
		db.close();
		return cursor.getCount();
	}
	
	/**
	 * Update template message
	 * @param template - template message with updated values
	 * @return updated records count
	 */
	@Override
	public int updateMessageTemplate(final MessageTemplate template) {
		SQLiteDatabase db = getWritableDatabase();
		
		if (db == null) {
			System.err.print("Can not udate message template: null pointer database");
			return 0;
		}
		
		ContentValues values = new ContentValues();
		values.put(KEY_TEMPLATE_SUBJECT, template.getSubject());
		values.put(KEY_TEMPLATE_MESSAGE, template.getMessage());
		
		// Update raw(s)
		String where = _ID + " = ?";
		String[] whereArgs = {String.valueOf(template.getId())};
		int updated = db.update(TABLE_TEMPLATES, values, where, whereArgs);
		db.close();
		
		return updated;
	}
	
	/**
	 * Delete template message with passed id
	 * @param id - id for template that should be deleted
	 * @return number of deleted rows
	 */
	@Override
	public int deleteMessageTemlate(final int id) {
		SQLiteDatabase db = getWritableDatabase();
		
		if (db == null) {
			System.err.print("Can not delete message template: null pointer database");
			return 0;
		}
				
		// Delete raw(s)
		String where = _ID + " = ?";
		String[] whereArgs = {String.valueOf(id)};
		int deleted = db.delete(TABLE_TEMPLATES, where, whereArgs);
		db.close();
		
		return deleted;
	}
	
	/************************************************************************/
	/** TABLE_CONTACT_INFO queries												*/
	/************************************************************************/
	/**
	 * Add new message template into template table
	 * @param contactInfo - object that describes contact info
	 */
	@Override
	public int addContactInfo(final ContactInfo contactInfo) {
		SQLiteDatabase db = getWritableDatabase();
		
		if (db == null) {
			System.err.print("Can not add new message template: null pointer database");
			return -1;
		}
		
		ContentValues values = new ContentValues();
		values.put(KEY_CONTACT_INFO_NUMBER, contactInfo.getContactNumber());
		values.put(KEY_CONTACT_INFO_LOOKUP_KEY, contactInfo.getContactLookupKey());
		values.put(KEY_CONTACT_INFO_TEMPLATE_ID, contactInfo.getMessageTemplateId());
		
		// Insert raw
		db.insert(TABLE_CONTACT_INFO, null, values);
		
		int insertId = -1;
		Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
		if (cursor == null || !cursor.moveToFirst()) {
			System.err.print("Can not get last insert row id");
		} else {
			insertId = cursor.getInt(0);
		}
		
		cursor.close();
		db.close();
		return insertId;
	}
	
	/**
	 * Get contact info from db by passed id
	 * @param id - contact info id
	 * @return contact info if selection by id was succeed, null - if fail 
	 */
	@Override
	public ContactInfo getContactInfo(final int id) {
		SQLiteDatabase db = getReadableDatabase();
		
		if (db == null) {
			System.err.print("Can not get contact info: null pointer database");
			return null;
		}
		
		String[] projection = {KEY_CONTACT_INFO_NUMBER, KEY_CONTACT_INFO_LOOKUP_KEY, KEY_CONTACT_INFO_TEMPLATE_ID};
		String selection = _ID + " = ?";
		String[] selectArgs ={String.valueOf(id)};
		Cursor cursor = db.query(TABLE_CONTACT_INFO, projection, selection, selectArgs, null, null, null);
		if (cursor == null || !cursor.moveToFirst()) {
			System.err.print("Can not get contact info: select query return null");
			db.close();
			return null;
		}
		
		final int numberColumnId = cursor.getColumnIndex(KEY_CONTACT_INFO_NUMBER);
		final int lookupColumnId = cursor.getColumnIndex(KEY_CONTACT_INFO_LOOKUP_KEY);
		final int templateIdColumnId = cursor.getColumnIndex(KEY_CONTACT_INFO_TEMPLATE_ID);
		
		ContactInfo contactInfo = new ContactInfo();
		contactInfo.setId(id);
		contactInfo.setContactNumber(cursor.getString(numberColumnId));
		contactInfo.setContactLookupKey(cursor.getString(lookupColumnId), mContext);
		contactInfo.setMessageTemplateId(cursor.getInt(templateIdColumnId));
		
		cursor.close();
		db.close();
		return contactInfo;
	}
	
	/**
	 * Get all stored contact info
	 * @return contact info collection
	 */
	@Override
	public ArrayList<ContactInfo> getAllContactInfo() {
		ArrayList<ContactInfo> contactInfoList = new ArrayList<ContactInfo>();
		
		SQLiteDatabase db = getReadableDatabase();
		
		if (db == null) {
			System.err.print("Can not get contact info: null pointer database");
			return contactInfoList;
		}
		
		String[] projection = {_ID, KEY_CONTACT_INFO_NUMBER, KEY_CONTACT_INFO_LOOKUP_KEY, KEY_CONTACT_INFO_TEMPLATE_ID};
		Cursor cursor = db.query(TABLE_CONTACT_INFO, projection, null, null, null, null, null);
		if (cursor == null || !cursor.moveToFirst()) {
			System.err.print("Can not get contact info: select query return null");
			db.close();
			return contactInfoList;
		}
		
		final int idColumnId = cursor.getColumnIndex(_ID);
		final int numberColumnId = cursor.getColumnIndex(KEY_CONTACT_INFO_NUMBER);
		final int lookupColumnId = cursor.getColumnIndex(KEY_CONTACT_INFO_LOOKUP_KEY);
		final int templateIdColumnId = cursor.getColumnIndex(KEY_CONTACT_INFO_TEMPLATE_ID);
		
		do {
			ContactInfo contactInfo = new ContactInfo();
			contactInfo.setId(cursor.getInt(idColumnId));
			contactInfo.setContactNumber(cursor.getString(numberColumnId));
			contactInfo.setContactLookupKey(cursor.getString(lookupColumnId), mContext);
			contactInfo.setMessageTemplateId(cursor.getInt(templateIdColumnId));
			
			contactInfoList.add(contactInfo);
		} while (cursor.moveToNext());
		
		cursor.close();
		db.close();
		return contactInfoList;
	}
	
	/**
	 * Get VALID contact info related with message template by passed id
	 * @return contact info collection
	 */
	@Override
	public ArrayList<ContactInfo> getContactInfoByTemplateId(final int templateId) {
		ArrayList<ContactInfo> contactInfoList = new ArrayList<ContactInfo>();
		
		SQLiteDatabase db = getReadableDatabase();
		
		if (db == null) {
			System.err.print("Can not get contact info: null pointer database");
			return contactInfoList;
		}
		
		String[] projection = {_ID, KEY_CONTACT_INFO_NUMBER, KEY_CONTACT_INFO_LOOKUP_KEY, KEY_CONTACT_INFO_TEMPLATE_ID};
		String selection = KEY_CONTACT_INFO_TEMPLATE_ID + " = ?";
		String[] selectionArgs = {String.valueOf(templateId)};
		Cursor cursor = db.query(TABLE_CONTACT_INFO, projection, selection, selectionArgs, null, null, null);
		if (cursor == null || !cursor.moveToFirst()) {
			System.err.print("Can not get contact info: select query return null");
			db.close();
			return contactInfoList;
		}
		
		final int idColumnId = cursor.getColumnIndex(_ID);
		final int numberColumnId = cursor.getColumnIndex(KEY_CONTACT_INFO_NUMBER);
		final int lookupColumnId = cursor.getColumnIndex(KEY_CONTACT_INFO_LOOKUP_KEY);
		final int templateIdColumnId = cursor.getColumnIndex(KEY_CONTACT_INFO_TEMPLATE_ID);
		
		do {
			ContactInfo contactInfo = new ContactInfo();
			contactInfo.setId(cursor.getInt(idColumnId));
			contactInfo.setContactNumber(cursor.getString(numberColumnId));
			contactInfo.setContactLookupKey(cursor.getString(lookupColumnId), mContext);
			contactInfo.setMessageTemplateId(cursor.getInt(templateIdColumnId));
			
			if (contactInfo.isValid()) {
				contactInfoList.add(contactInfo);
			}
		} while (cursor.moveToNext());
		
		cursor.close();
		db.close();
		return contactInfoList;
	}
	
	/**
	 * Get contact info records count
	 * @return contact info records count in db, -1 if error was occurred
	 */
	@Override
	public int getContactInfoCount() {
		SQLiteDatabase db = getReadableDatabase();
		
		if (db == null) {
			System.err.print("Can not get contact info count: null pointer database");
			return -1;
		}
		
		final String query = "SELECT * FROM " + TABLE_CONTACT_INFO;
		Cursor cursor = db.rawQuery(query, null);
		if (cursor == null) {
			System.err.print("Can not get contact info count: query return null");
			db.close();
			return -1;
		}
		cursor.close();
		db.close();
		return cursor.getCount();
	}
	
	/**
	 * Update contact info
	 * @param template - contact info with updated values
	 * @return updated records count
	 */
	@Override
	public int updateContactInfo(final ContactInfo contactInfo) {
		SQLiteDatabase db = getWritableDatabase();
		
		if (db == null) {
			System.err.print("Can not update message template: null pointer database");
			return 0;
		}
		
		ContentValues values = new ContentValues();
		values.put(KEY_CONTACT_INFO_NUMBER, contactInfo.getContactNumber());
		values.put(KEY_CONTACT_INFO_LOOKUP_KEY, contactInfo.getContactLookupKey());
		values.put(KEY_CONTACT_INFO_TEMPLATE_ID, contactInfo.getMessageTemplateId());
		
		// Update raw(s)
		String where = _ID + " = ?";
		String[] whereArgs = {String.valueOf(contactInfo.getId())};
		int updated = db.update(TABLE_CONTACT_INFO, values, where, whereArgs);
		db.close();
		
		return updated;
	}
	
	/**
	 * Delete contact info with passed id
	 * @param id - id for contact info that should be deleted
	 * @return number of deleted rows
	 */
	@Override
	public int deleteContactInfo(final int id) {
		SQLiteDatabase db = getWritableDatabase();
		
		if (db == null) {
			System.err.print("Can not delete contact info: null pointer database");
			return 0;
		}
				
		// Delete raw(s)
		String where = _ID + " = ?";
		String[] whereArgs = {String.valueOf(id)};
		int deleted = db.delete(TABLE_CONTACT_INFO, where, whereArgs);
		db.close();
		
		return deleted;
	}
 }
