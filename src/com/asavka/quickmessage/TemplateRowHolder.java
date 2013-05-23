package com.asavka.quickmessage;

import android.widget.TextView;

public class TemplateRowHolder {
	private TextView mHeader;
	
	/**
	 * Get row header
	 * @return
	 */
	public TextView getHeader() {
		return mHeader;
	}
	
	/**
	 * Set row header
	 * @param header row header text
	 */
	public void setHeader(final TextView header) {
		mHeader = header;
	}
}
