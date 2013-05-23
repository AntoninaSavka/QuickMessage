package com.asavka.quickmessage;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.asavka.quickmessage.SwipeDetector.Action;

public class MainActivity extends Activity {
	private TemplateListAdapter mTemplateAdapter;
	private QuickMessageDataHandler mDataHandler = null;
	private ArrayList<MessageTemplate> mTemplateList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mDataHandler = new SqlDataHandler(this);
		
		mTemplateList = mDataHandler.getAllMessageTemlates();		
		mTemplateAdapter = new TemplateListAdapter(this,
				R.layout.temlate_row, mTemplateList);
		
		final ListView templateListView = (ListView) findViewById(R.id.template_listview);
		templateListView.setAdapter(mTemplateAdapter);
		
		/** Set row action listener */
		final SwipeDetector swipeDetector = new SwipeDetector();
		templateListView.setOnTouchListener(swipeDetector);
		
		final OnItemClickListener templateOnClickListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adater, View view, int position, long id) {
				if (adater == null) {
					System.err.print("OnClickItem error: adapter is not initialized");
					return;
				}

				MessageTemplate template = (MessageTemplate)adater.getItemAtPosition(position);
				if (swipeDetector.getAction() == Action.LR) {
					sendSms(template);
				} else {
	    			Intent detailViewActivity = new Intent(getApplicationContext(), DetailViewActivity.class);
	    			detailViewActivity.putExtra(QuickMessageConsts.EXTRA_MESSAGE_ID,
							template.getId());
					detailViewActivity.putExtra(QuickMessageConsts.EXTRA_MESSAGE_SUBJECT,
							template.getSubject());
					detailViewActivity.putExtra(QuickMessageConsts.EXTRA_MESSAGE_BODY, 
							template.getMessage());
					startActivityForResult(detailViewActivity, QuickMessageConsts.UPDATE_TEMPLATE);
				}
			}
		};

		templateListView.setOnItemClickListener(templateOnClickListener);
		
		final OnItemLongClickListener templateOnLongClickListener = new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adater, View view, int position, long id) {
				if (adater == null) {
					System.err.print("OnClickItem error: adapter is not initialized");
					return false;
				}

				MessageTemplate template = (MessageTemplate)adater.getItemAtPosition(position);
				if (swipeDetector.getAction() == Action.LR) {
					sendSms(template);
				}
				
				return true;
			}
		};
		
		templateListView.setOnItemLongClickListener(templateOnLongClickListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
      super.onActivityResult(reqCode, resultCode, data);

      switch (reqCode) {
      case QuickMessageConsts.ADD_TEMPLATE:
      case QuickMessageConsts.UPDATE_TEMPLATE:
    	  if (data == null)
    		  break;
    	
	    	int templateId = data.getIntExtra(QuickMessageConsts.EXTRA_MESSAGE_ID, -1);
	    	if (templateId < 0)
	    		break;
	    	
	    	MessageTemplate message = mDataHandler.getMessageTemplate(templateId);
	    	MessageTemplate curMessage = getMessageTemplate(templateId);
	    	if (curMessage == null) {
	    		// add new one
	    		mTemplateAdapter.add(message);
	    	} else {
	    		// update message
	    		int pos = mTemplateAdapter.getPosition(curMessage);
	    		MessageTemplate item = mTemplateAdapter.getItem(pos);
	    		if (item != null) {
	        		item.setSubject(message.getSubject());
	        		item.setMessage(message.getMessage());
	    		}
	    	}
	    	mTemplateAdapter.notifyDataSetChanged();
	    		
	    	break;
      }
	}
	
	/**
	 * Handle user click on add new template button
	 * @param view
	 */
	public void onAddNewTemplate(View view) {
		Intent detailViewActivity = new Intent(getApplicationContext(), DetailViewActivity.class);
		startActivityForResult(detailViewActivity, QuickMessageConsts.ADD_TEMPLATE);
	}
	
	/**
	 * Get message template form list by template id
	 * @param templateId id of needed template
	 * @return template with passed id or null if message with passed id doesn't exist in list 
	 */
	private MessageTemplate getMessageTemplate(final int templateId) {
		for (MessageTemplate message : mTemplateList) {
			if (message.getId() == templateId)
				return message;
		}
		
		return null;
	}
	
	private void sendSms(final MessageTemplate template) {
		if (template.getMessage().equals("")) {
			// TODO show popup
			return;
		}
		
		ArrayList<ContactInfo> contacts = mDataHandler.getContactInfoByTemplateId(template.getId());
		if (contacts.size() < 1) {
			// TODO show popup
			return;
		}
		
		HashSet<String> numbers = new HashSet<String>();
		for (ContactInfo contact : contacts) {
			numbers.add(contact.getContactNumber());
		}
		
		sendSMS(numbers, template.getMessage());
	}
	
	private void sendSMS(HashSet<String> phoneNumbers, String message)
    {        
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
            new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
            new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", 
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;                        
                }
            }
        }, new IntentFilter(DELIVERED));        

        SmsManager sms = SmsManager.getDefault();
        for (String phone : phoneNumbers) {
        	sms.sendTextMessage(phone, null, message, sentPI, deliveredPI);	
		}        
    }
}
