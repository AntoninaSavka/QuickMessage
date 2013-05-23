package com.asavka.quickmessage;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactRowHolder {
	private CheckBox mContactCheckBox;
	private ImageView mContactPicture;
	private TextView mContactName;
	private TextView mContactNumber;
	
	/**
	 * Get row contact checkBox
	 * @return
	 */
	public CheckBox getContactCheckBox() {
		return mContactCheckBox;
	}
	
	/**
	 * Set row contact check box
	 * @param checkBox
	 */
	public void setContactCheckBox(final CheckBox checkBox) {
		mContactCheckBox = checkBox;
	}
	
	/**
	 * Get row contact picture view
	 * @return
	 */
	public ImageView getContactPictureView() {
		return mContactPicture;
	}
	
	/**
	 * Set row contact picture view
	 * @param pictureView
	 */
	public void setContactPictureView(final ImageView pictureView) {
		mContactPicture = pictureView;
	}
	
	/**
	 * Get row contact name view
	 * @return
	 */
	public TextView getContactNameView() {
		return mContactName;
	}
	
	/**
	 * Set row contact name view
	 * @param nameView
	 */
	public void setContactNameView(final TextView nameView) {
		mContactName = nameView;
	}
	
	/**
	 * Get row contact phone number view
	 * @return
	 */
	public TextView getContactNumberView() {
		return mContactNumber;
	}
	
	/**
	 * Set row contact phone number view
	 * @param numberView
	 */
	public void setContactNumberView(final TextView numberView) {
		mContactNumber = numberView;
	}
}
