package com.asavka.quickmessage;

import java.util.ArrayList;

public interface QuickMessageDataHandler {
	/** Message templates table*/
	public int addTemplate(final MessageTemplate template);
	public MessageTemplate getMessageTemplate(final int id);
	public ArrayList<MessageTemplate> getAllMessageTemlates();
	public int getMessageTemlatesCount();
	public int updateMessageTemplate(final MessageTemplate template);
	public int deleteMessageTemlate(final int id);
	
	/** Contact info table */
	public int addContactInfo(final ContactInfo contactInfo);
	public ContactInfo getContactInfo(final int id);
	public ArrayList<ContactInfo> getAllContactInfo();
	public ArrayList<ContactInfo> getContactInfoByTemplateId(final int templateId);
	public int getContactInfoCount();
	public int updateContactInfo(final ContactInfo contactInfo);
	public int deleteContactInfo(final int id);
}
