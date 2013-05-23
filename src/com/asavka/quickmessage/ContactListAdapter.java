package com.asavka.quickmessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactListAdapter extends ArrayAdapter<ContactInfo> {
	HashMap<ContactInfo, Integer> mIdMap = new HashMap<ContactInfo, Integer>();
	
	private Context mContext;
	private int mLayoutResId;
	private ArrayList<ContactInfo> mObjects;
	
	public ContactListAdapter(Context context, int layoutResourceId,
			List<ContactInfo> objects) {
		super(context, layoutResourceId, objects);
		
		for (int i = 0; i < objects.size(); ++i) {
			mIdMap.put(objects.get(i), i);
		}
		
		mContext = context;
		mLayoutResId = layoutResourceId;
		if (mObjects != null) {
			mObjects.clear();
		}
		mObjects = new ArrayList<ContactInfo>(objects);
	}

	@Override
	public long getItemId(int position) {
		ContactInfo item = getItem(position);
		return mIdMap.get(item);
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ContactRowHolder rowHolder = null;
		
		if (row == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			row = inflater.inflate(mLayoutResId, parent, false);
			
			rowHolder = new ContactRowHolder();
			rowHolder.setContactCheckBox((CheckBox) row.findViewById(R.id.contact_remove_checkBox));
			rowHolder.setContactNameView((TextView) row.findViewById(R.id.contact_name));
			rowHolder.setContactNumberView((TextView) row.findViewById(R.id.contact_number));
			rowHolder.setContactPictureView((ImageView) row.findViewById(R.id.contact_picture));
			
			row.setTag(rowHolder);
		} else {
			rowHolder = (ContactRowHolder)row.getTag();
		}
		
		ContactInfo contactInfo = getItem(position);
		fillContactRow(contactInfo, rowHolder);
		return row;
	}
	
	@Override
	public void add(ContactInfo contactInfo) {
		if (mIdMap.containsKey(contactInfo))
			return;
		
		super.add(contactInfo);
		
		mIdMap.put(contactInfo, mIdMap.size());
	}
	
	@Override
	public void clear() {
		super.clear();
		mIdMap.clear();
	}
	
	/**
     * Found contact by lookup key and create raw using found data
     * @param lookupKey - contact lookup key for each data should be shown
     */
    private void fillContactRow(final ContactInfo contactInfo, ContactRowHolder row) {
    	if (row == null) {
    		System.err.print("Can not fill contact info row: row is null");
    		return;
    	}
    	
		row.getContactNameView().setText(contactInfo.getContactName());
		row.getContactNumberView().setText(contactInfo.getContactNumber());
    }
}
