package com.wecall.contacts.entity;

import java.util.ArrayList;

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

	public ContactItem(int id, String name, String phoneNumber, String address,
			String note, ArrayList<String> labels, String sortLetter,
			String fullPinyin, String simplePinyin) {
		super();
		this.id = id;
		this.name = name;
		this.sortLetter = sortLetter;
		this.phoneNumber = phoneNumber;
		this.note = note;
		this.address = address;
		this.labels = labels;
		this.fullPinyin = fullPinyin;
		this.simplePinyin = simplePinyin;
	}

	public ContactItem(String name, String sortLetter, String phoneNumber,
			String note, String address, ArrayList<String> labels) {
		super();
		this.name = name;
		this.sortLetter = sortLetter;
		this.phoneNumber = phoneNumber;
		this.note = note;
		this.address = address;
		this.labels = labels;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		fullPinyin = PinYin.getPinYin(this.name);
		simplePinyin = PinYin.getSimplePinYin(this.name);
	}

	public void setSortLetter(String sortLetter) {
		this.sortLetter = sortLetter;
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
		this.phoneNumber = phoneNumber;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public ArrayList<String> getLabels() {
		return labels;
	}

	public void setLabels(ArrayList<String> labels) {
		this.labels = labels;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int compareTo(Object arg0) {
		ContactItem tmpItem = (ContactItem) arg0;
		if(tmpItem.getSortLetter().equals("#")&&!getSortLetter().equals("#")){
			return -1;
		}else if(!tmpItem.getSortLetter().equals("#")&&getSortLetter().equals("#")){
			return 1;
		}
		return getFullPinyin().compareTo(tmpItem.getFullPinyin());
	}

}
