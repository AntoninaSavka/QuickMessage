package com.asavka.quickmessage;

import java.util.HashMap;
import java.util.List;

import com.asavka.quickmessage.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TemplateListAdapter extends ArrayAdapter<MessageTemplate> {
	HashMap<MessageTemplate, Integer> mIdMap = new HashMap<MessageTemplate, Integer>();
	
	private Context mContext;
	private int mLayoutResId;
	
	public TemplateListAdapter(Context context, int layoutResourceId,
			List<MessageTemplate> objects) {
		super(context, layoutResourceId, objects);
		
		for (int i = 0; i < objects.size(); ++i) {
			mIdMap.put(objects.get(i), i);
		}
		
		mContext = context;
		mLayoutResId = layoutResourceId;
	}

	@Override
	public long getItemId(int position) {
		MessageTemplate item = getItem(position);
		return mIdMap.get(item);
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		TemplateRowHolder rowHolder = null;
		
		if (row == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			row = inflater.inflate(mLayoutResId, parent, false);
			
			rowHolder = new TemplateRowHolder();
			rowHolder.setHeader((TextView) row.findViewById(R.id.template_subject));
			
			row.setTag(rowHolder);
		} else {
			rowHolder = (TemplateRowHolder)row.getTag();
		}
		
		MessageTemplate template = getItem(position);
		rowHolder.getHeader().setText(template.getSubject());
		
		return row;
	}
	
	@Override
	public void add(MessageTemplate template) {
		if (mIdMap.containsKey(template))
			return;
		
		super.add(template);
		
		mIdMap.put(template, mIdMap.size());
	}
	
	@Override
	public void clear() {
		super.clear();
		mIdMap.clear();
	}
	
}

