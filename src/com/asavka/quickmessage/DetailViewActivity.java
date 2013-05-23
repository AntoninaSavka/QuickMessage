package com.asavka.quickmessage;

import java.util.ArrayList;
import java.util.HashSet;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class DetailViewActivity extends Activity {
	
	public final int PICK_CONTACT = 1;
	
	private ArrayList<ContactInfo> mContacts = null;
	private HashSet<Integer> mDeleteIdList = null;
	ContactListAdapter mAdapter = null;
	private ListView mContactListView = null;
	private int mTemplateId = -1;
	private QuickMessageDataHandler mDataHandler = null; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template_detail_view);
        // Show the Up button in the action bar.
        setupActionBar();
        
        mDeleteIdList = new HashSet<Integer>();
        mDataHandler = new SqlDataHandler(this);
        
        mContacts = new ArrayList<ContactInfo>();
        mAdapter = new ContactListAdapter(this, R.layout.contact_row, mContacts);
        mContactListView = (ListView) findViewById(R.id.message_detail_contact_list);
        mContactListView.setAdapter(mAdapter);
        
        /** Fill details from extra */
        Intent intent = getIntent();

        mTemplateId = intent.getIntExtra(QuickMessageConsts.EXTRA_MESSAGE_ID, -1);
        if (mTemplateId != -1) {
	        String msgSubject = intent.getStringExtra(QuickMessageConsts.EXTRA_MESSAGE_SUBJECT);
	        String msgBody = intent.getStringExtra(QuickMessageConsts.EXTRA_MESSAGE_BODY);
	        fillDataFields(msgSubject, msgBody, mTemplateId);
        }
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_view, menu);
        return true;
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
      super.onActivityResult(reqCode, resultCode, data);

      switch (reqCode) {
        case (PICK_CONTACT) :
          if (resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            ArrayList<String> pathSegments = new ArrayList<String>(contactData.getPathSegments());
            String dataId = pathSegments.get(pathSegments.size() - 1);
            if (dataId == null || dataId.length() < 0)
            	break;
            
            String[] projection = {Phone.LOOKUP_KEY, Phone.NUMBER};
            String selection = Phone._ID + "= ?";
            String[] selectArgs = {dataId};
            Cursor c =  getContentResolver().query(Phone.CONTENT_URI, projection, 
            		selection, selectArgs, null);
            if (c.moveToFirst()) {
            	/** 
            	 * Check if contact has any phone number. 
            	 * If not - info user about that
            	 */
        		String lookupKey = c.getString(c.getColumnIndex(Phone.LOOKUP_KEY));
        		String number = c.getString(c.getColumnIndex(Phone.NUMBER));
        		
        		if (!mContacts.contains(number)) {
        			addContact(lookupKey, number);
        		}
            }
            c.close();
          }
          break;
      }
    }
    
    @SuppressLint("NewApi")
	public void onAddContact(View view) {
    	Intent contactsIntent = new Intent(Intent.ACTION_PICK, Phone.CONTENT_URI);
    	startActivityForResult(contactsIntent, PICK_CONTACT);
    }
    
    public void onSaveChanges(View v) {
    	String subject = ((TextView) findViewById(R.id.message_detail_subject_edit)).getText().toString();
    	if (subject == null || subject.length() == 0) {
    		// TODO show popup message
    		return;
    	}
    	
    	String message = ((TextView) findViewById(R.id.message_detail_body)).getText().toString();
    	
		// if current template message exists
    	if (mTemplateId != -1) {
    		// all contacts with id=-1 should be updated with mTemplateId and insert into db
    		addNewContactsInfoInDb(mTemplateId);
    		deleteContatsInfoFromDb();
    		
    		MessageTemplate template = new MessageTemplate(this, mTemplateId, subject, message);
    		mDataHandler.updateMessageTemplate(template);
    	} else { 
    		// add new template message
    		MessageTemplate template = new MessageTemplate(this);
    		template.setSubject(subject);
    		template.setMessage(message);
    		
    		mTemplateId = mDataHandler.addTemplate(template);
    		if (mTemplateId == -1) {
    			// TODO show popup
    			return;
    		}
    		
    		addNewContactsInfoInDb(mTemplateId);
    	}
    	
    	Intent resultData = new Intent();
    	resultData.putExtra(QuickMessageConsts.EXTRA_MESSAGE_ID, mTemplateId);
    	setResult(Activity.RESULT_OK, resultData);
    	finish();
	}

    /**
     * Fill activity fields with message template data
     * @param msgSubject - message template name
     * @param msgBody - message template
     * @param contactsInfo - contacts' lookup keys list to whom message template will be sent
     */
    private void fillDataFields(final String msgSubject, final String msgBody, final int templateId) {
    	if (msgSubject != null && msgSubject.length() > 0) {
			TextView subjectView = (TextView) findViewById(R.id.message_detail_subject_edit);
			if (subjectView != null) {
				subjectView.setText(msgSubject);
			}
    	}
		
		if (msgBody != null && msgBody.length() > 0) {
			EditText bodyView = (EditText) findViewById(R.id.message_detail_body);
			if (bodyView != null) {
				bodyView.setText(msgBody);
			}
		}

		// Get contact info from DB and fill list
		mContacts = mDataHandler.getContactInfoByTemplateId(mTemplateId);
		mAdapter = new ContactListAdapter(this, R.layout.contact_row, mContacts);
		mContactListView.setAdapter(mAdapter);
	}
        
    /**
     * Add new contact into selected contact list
     * @param lookuKey - added contact lookup key
     */
    private void addContact(final String lookupKey, final String number) {
    	if (mAdapter == null) {
    		System.err.print("Can not update contact list: null pointer adapter");
    		return;
    	}
    	
    	ContactInfo contactInfo = new ContactInfo(number, lookupKey, this);
		if (contactInfo.isValid()) {
			mAdapter.add(contactInfo);
			mAdapter.notifyDataSetChanged();
		}
    }
    
    /**
     * Update all contacts info template id and insert them into db
     * @param templateId - message template id 
     */
    private void addNewContactsInfoInDb(final int templateId) {
    	for (ContactInfo contactInfo : mContacts) {
			if (contactInfo.getId() == -1) {
				contactInfo.setMessageTemplateId(templateId);
				mDataHandler.addContactInfo(contactInfo);
			}
		}
    }

    /**
     * Delete all contacts info form db present in delete id list
     */
    private void deleteContatsInfoFromDb() {
		if (mDeleteIdList != null) {
			for (Integer id : mDeleteIdList) {
				mDataHandler.deleteContactInfo(id);
			}
		}
    }
}
