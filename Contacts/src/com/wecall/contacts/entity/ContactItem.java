package com.wecall.contacts.entity;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import com.wecall.contacts.util.PinYin;

/**
 * ��ϵ�˵�ʵ����
 * 
 * @author xiaoxin
 * 
 */
@SuppressWarnings("rawtypes")
public class ContactItem implements Comparable {

	// ��ϵ��id
	private int id;
	// ��ϵ������
	private String name;
	// ����ĸ
	private String sortLetter;
	// �绰����
	private String phoneNumber;
	// ��ע
	private String note;
	// ��ַ
	private String address;
	// ��ǩ
	private ArrayList<String> labels;
	// ����ȫƴ
	private String fullPinyin;
	// ��������ĸ���
	private String simplePinyin;

	public ContactItem() {

	}

	public ContactItem(String name, String phoneNumber, String address,
			String note, ArrayList<String> labels) {
		super();
		setName(name);
		setPhoneNumber(phoneNumber);
		setAddress(address);
		setNote(note);
		setLabels(labels);

		fullPinyin = PinYin.getPinYin(this.name);
		simplePinyin = PinYin.getSimplePinYin(this.name);
		setSortLetter(fullPinyin);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null)
			this.name = null;
		else
		{
			this.name = new String(name);
			fullPinyin = PinYin.getPinYin(this.name);
			simplePinyin = PinYin.getSimplePinYin(this.name);
			setSortLetter(fullPinyin);		
		}
	}

	public String getSortLetter() {
		return sortLetter;
	}

	public String getFullPinyin() {
		return fullPinyin;
	}

	public String getSimplePinyin() {
		return simplePinyin;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		if(phoneNumber == null)
			this.phoneNumber = null;
		else
			this.phoneNumber = new String(phoneNumber);
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		if (note == null)
			this.note = null;
		else
			this.note = new String(note);
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		if (address == null)
			this.address = null;
		else
			this.address = new String(address);
	}

	public ArrayList<String> getLabels() {
		return labels;
	}

	public void setLabels(ArrayList<String> labels) {
		if (labels == null)
			this.labels = null;
		else
			this.labels = new ArrayList<String>(labels);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "ContactItem [id=" + id + ", name=" + name + ", sortLetter="
				+ sortLetter + ", phoneNumber=" + phoneNumber + ", note="
				+ note + ", address=" + address + ", labels=" + labels
				+ ", fullPinyin=" + fullPinyin + ", simplePinyin="
				+ simplePinyin + "]";
	}

	@Override
	public int compareTo(Object arg0) {
		ContactItem tmpItem = (ContactItem) arg0;
		if (tmpItem.getSortLetter().equals("#") && !getSortLetter().equals("#")) {
			return -1;
		} else if (!tmpItem.getSortLetter().equals("#")
				&& getSortLetter().equals("#")) {
			return 1;
		}
		return getFullPinyin().compareTo(tmpItem.getFullPinyin());
	}
	
	@SuppressLint("DefaultLocale") 
	private void setSortLetter(String inputString){
		if(inputString.isEmpty()){
			this.sortLetter = "#";
			return;
		}
		String sortString = inputString.substring(0, 1).toUpperCase();
		// ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ
		if (sortString.matches("[A-Z]")) {
			this.sortLetter = sortString;
		} else {
			this.sortLetter = "#";
		}
	}

}
