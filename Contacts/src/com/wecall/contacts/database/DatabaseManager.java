package com.wecall.contacts.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.wecall.contacts.entity.ContactItem;
import com.wecall.contacts.entity.Label;

import android.util.Log;
import com.wecall.contacts.constants.Constants;

/**
 * �������ݿ��࣬����SQL���ϸ�ڣ��ṩ����ɾ����ѯ�ӿ�
 * 
 * @author KM
 */

public class DatabaseManager {
	
	private SQLiteDatabase db = null;
	private DatabaseHelper mHelper = null;
	// Ϊ��ϵ�˷����Ψһ��ʶ
	private static int id;
	// id���浽preference��
	private static SharedPreferences preferences;
	
	public DatabaseManager(Context context)
	{
		mHelper = new DatabaseHelper(context);
		db = mHelper.getWritableDatabase();
		// ͨ��shared preference��ʼ��idֵ
		preferences = context.getSharedPreferences("database", Context.MODE_PRIVATE);
		id = preferences.getInt("id_count", 1);
		
//		// �����������
//		ArrayList<ContactItem> list = queryAllContact();
//		Log.i("databaseManager", list.size() + "");
//		if (list.size() == 0)
//		{
//			list = new ArrayList<ContactItem>();
//			
//			ContactItem item = new ContactItem();
//			item.setName("xiaoxinla");
//			item.setPhoneNumber("123123123");
//			item.setNote("Group leader");
//			item.setAddress("China GD GZ");
//			ArrayList<Label> labels = new ArrayList<Label>();
//			labels.add(new Label("Tall"));
//			labels.add(new Label("Rich"));
//			labels.add(new Label("Handsome"));		
//			item.setLabels(labels);
//			for(int i = 0; i < 500; i++)
//				list.add(item);
//			
//			addContacts(list);
//		}
	}
	
	/**
	 * �����ݿ�����µ�һ����ϵ��
	 * �������ɹ��᷵�����ݿ��Ӧ��id.
	 */
	public int addContact(ContactItem item)
	{		
		try {
			// ����main��
			ContentValues values = new ContentValues();
			values.put(Constants.MAIN_COL_CID, id);
			values.put(Constants.MAIN_COL_NAME, item.getName());
			values.put(Constants.MAIN_COL_FULL_PINYIN, item.getFullPinyin());
			values.put(Constants.MAIN_COL_SIM_PINYIN, item.getSimplePinyin());
			values.put(Constants.MAIN_COL_NOTE, item.getNote());
			values.put(Constants.MAIN_COl_PHONE, item.getPhoneNumber());
			values.put(Constants.MAIN_COL_ADDRESS, item.getAddress());
			db.insert(Constants.MAIN_TABLE_NAME, null, values);
			
			// ����tag��
			ArrayList<Label> list = item.getLabels();
			if (list != null) {
				for (Label label : list) {
					values.clear();
					values.put(Constants.TAG_COL_CID, id);
					values.put(Constants.TAG_COL_TAG, label.getLname());
					values.put(Constants.TAG_COL_TAG_FULLPY, label.getLabelFullPinyin());
					values.put(Constants.TAG_COL_TAG_SIM_PINYIN, label.getLabelSimplePinyin());
					db.insert(Constants.TAG_TABLE_NAME, null, values);
				}
			}		
			
			id++;
			Editor editor = preferences.edit();
			editor.putInt("id_count", id);
			editor.commit();
			return id;
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("err", "data insert failed.");
		}
		return -1;
	}
	
	/**
	 * ����������ϵ��
	 * @param list
	 */
	public void addContacts(List<ContactItem> list)
	{
		db.beginTransaction();
		try
		{
			for(ContactItem item: list)
			{
				addContact(item);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	/**
	 * ���뵥����tag�����뱣֤��ԭ�в��ظ�
	 */
	public void addTagById(int id, String tag)
	{
		if( !isId(id) )
			return;
		try
		{
			ContentValues values = new ContentValues();
			values.put(Constants.TAG_COL_CID, id);
			values.put(Constants.TAG_COL_TAG, tag);
			db.insert(Constants.TAG_TABLE_NAME, null, values);
		} catch(SQLException se) {
			se.printStackTrace();
			Log.i("err", "insertTagById failed.");
		}
	}

	/**
	 * ȡ���ݿ�ȫ������
	 */
	public ArrayList<ContactItem> queryAllContact()
	{
		ArrayList<ContactItem> list = new ArrayList<ContactItem>();
		Cursor cursor = null;

		try{
			// ����main���Ѳ�ͬid������
			cursor = db.query(Constants.MAIN_TABLE_NAME, new String[] {"*"}, 
					null, null, null, null, null);
			int idIndex = cursor.getColumnIndex(Constants.MAIN_COL_CID);
			int nameIndex = cursor.getColumnIndex(Constants.MAIN_COL_NAME);
			int noteIndex = cursor.getColumnIndex(Constants.MAIN_COL_NOTE);
			int phoneIndex = cursor.getColumnIndex(Constants.MAIN_COl_PHONE);
			int addressIndex = cursor.getColumnIndex(Constants.MAIN_COL_ADDRESS);
			try
			{
				while(cursor.moveToNext())
				{
					ContactItem item = new ContactItem();
					item.setId(cursor.getInt(idIndex));
					item.setName(cursor.getString(nameIndex));
					item.setNote(cursor.getString(noteIndex));	
					item.setPhoneNumber(cursor.getString(phoneIndex));
					item.setAddress(cursor.getString(addressIndex));
					
					item.setLabels(queryTagById(item.getId()));
					
					list.add(item);
				}
			} finally{ 
				cursor.close();
			}
		} catch (SQLException se) {
			se.printStackTrace();
			Log.e("err", "selectAll failed.");
		}	
		return list;
	}
	
	/**
	 * ͨ��id�ѵ�����ϵ��
	 * @param id
	 * @return
	 */
	public ContactItem queryContactById(int id)
	{
		if ( !isId(id) )
			return null;
		ContactItem item = new ContactItem();
		try
		{
			Cursor cursor = db.query(Constants.MAIN_TABLE_NAME, new String[] {"*"}, 
					Constants.MAIN_COL_CID + "=?", new String[] {id+""}, null, null, null);
			int nameIndex = cursor.getColumnIndex(Constants.MAIN_COL_NAME);
			int noteIndex = cursor.getColumnIndex(Constants.MAIN_COL_NOTE);
			int idIndex = cursor.getColumnIndex(Constants.MAIN_COL_CID);
			int phoneIndex = cursor.getColumnIndex(Constants.MAIN_COl_PHONE);
			int addressIndex = cursor.getColumnIndex(Constants.MAIN_COL_ADDRESS);
			try
			{
				if(cursor.moveToFirst())
				{
					item.setId(cursor.getInt(idIndex));
					item.setName(cursor.getString(nameIndex));
					item.setNote(cursor.getString(noteIndex));
					item.setPhoneNumber(cursor.getString(phoneIndex));
					item.setAddress(cursor.getString(addressIndex));
					Log.e("Contact", item+"");
				}
			} finally {
				cursor.close();
			}
				
			item.setLabels(queryTagById(id));
			
		} catch(Exception e)
		{
			e.printStackTrace();
			Log.e("err", "queryContactById error.");
		}

		return item;
	}
	
	/**
	 * ͨ��tag��������ϵ��
	 */
	public ArrayList<ContactItem> queryContactByTag(String tagName)
	{
		ArrayList<ContactItem> list = new ArrayList<ContactItem>();
		
		Cursor cursor = db.query(true, Constants.TAG_TABLE_NAME, new String[] {"*"},
				Constants.TAG_COL_TAG + "=?", new String[] {tagName}, null, null, null, null);
		int idIndex = cursor.getColumnIndex(Constants.TAG_COL_CID);
		
		try
		{
			while(cursor.moveToNext())
			{
				int id = cursor.getInt(idIndex);
				ContactItem item = queryContactById(id);
				list.add(item);
			}
		} finally
		{
			cursor.close();
		}
		
		return list;
	}
	
	/**
	 * ͨ����ϵ�˱�ʶ��ɾ��һ����ϵ�˼�¼
	 */
	public void deleteContactById(int id)
	{			
		if( !isId(id) )
			return;
		try {
			// �����Լ����ȷ������ɾ������˵Android2.2���֧��
			db.execSQL("PRAGMA foreign_keys=ON");
			db.delete(Constants.MAIN_TABLE_NAME, Constants.MAIN_COL_CID + " = ?", new String[]{ id + "" } );
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("err", "delete based on id failed.");
		}
	}
	
	/**
	 * ����id����tag��
	 */
	public void updateTagById(int id, ArrayList<Label> labels)
	{		
		// �������Ƿ�Ϸ�
		if( !isId(id) || labels == null)
			return;
		// ��ɾ���뵱ǰid����������tag
		db.delete(Constants.TAG_TABLE_NAME, Constants.TAG_COL_CID + "= ?", new String[] {id + ""});
		// �ٲ���ȫ���µ�tag
		for(Label s: labels)
		{
			ContentValues values = new ContentValues();
			values.put(Constants.TAG_COL_CID, id + "");
			values.put(Constants.TAG_COL_TAG, s.getLname());
			values.put(Constants.TAG_COL_TAG_FULLPY, s.getLabelFullPinyin());
			values.put(Constants.TAG_COL_TAG_SIM_PINYIN, s.getLabelSimplePinyin());
			db.insert(Constants.TAG_TABLE_NAME, null, values);
		}
	}
	
	/**
	 * ����id����һ����ϵ�˵�ȫ����¼
	 */
	public void updateContact(ContactItem item)
	{		
		db.beginTransaction();
		try {
			// ����main��
			updateBasicById(item);
						
			// ����tag��
			updateTagById(item.getId(), item.getLabels());
						
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("err", "data update failed.");
		} finally
		{
			db.endTransaction();
		}
	}

	/**
	 * �ṩֱ��ִ��sql���Ľӿڣ�����������
	 * @Deprecated
	 */	
	public void execSQL(String sql)
	{
		db = mHelper.getWritableDatabase();
		db.execSQL(sql);
	}
	
	/**
	 * �ͷ���Դ��Ӧ��Activity onDestoryʱ����
	 */
	public void close()
	{
		if (mHelper != null)
			mHelper.close();
		if (db != null)
			db.close();
	}
	
	/**
	 * �ж��Ƿ�Ϸ���id
	 */
	private boolean isId(int id)
	{
		if(id <= 0)
		{
			Log.e("err", "ilegal id.");
			return false;
		}
		return true;
	}
	
	/**
	 * ����id��ѯ��Ӧ������tag
	 */
	public ArrayList<Label> queryTagById(int id) throws SQLException
	{		
		// ��Ӧÿ��id��tag��
		Cursor tag_cursor = db.query(Constants.TAG_TABLE_NAME, new String[] {"*"}, 
				Constants.MAIN_COL_CID + "=?", new String[] {id + ""}, null, null, null);
		
		ArrayList<Label> labels = new ArrayList<Label>();
		int tagIndex = tag_cursor.getColumnIndex(Constants.TAG_COL_TAG);
		try {
			while(tag_cursor.moveToNext())
			{
				Label label = new Label(tag_cursor.getString(tagIndex));				
				labels.add(label);
			}
		} finally {
			tag_cursor.close();
		}
		return labels;
	}
	
	/**
	 * ��ȡ�����ִ��ǩ
	 */
	public ArrayList<Label> queryAllTags()
	{
		ArrayList<Label> labels = new ArrayList<Label>();
		
		Cursor cursor = db.query(true, Constants.TAG_TABLE_NAME, new String[] {"*"}, 
				null, null, null, null, null, null);
		int tagNameIndex = cursor.getColumnIndex(Constants.TAG_COL_TAG);
		
		try {
			while(cursor.moveToNext())
			{
				Label label = new Label();
				label.setLname(cursor.getString(tagNameIndex));
				labels.add(label);
			}
		} finally {
			cursor.close();
		}
		
		return labels;
	}
	
	/**
	 * ɾ��������ϵ�˵�ĳ����ǩ
	 * @param tagName
	 */
	public void deleteTagForAllContact(String tagName)
	{
		try {
			db.delete(Constants.TAG_TABLE_NAME, Constants.TAG_COL_TAG + "=?", 
					new String[] {tagName});
		} catch (SQLException se) {
			se.printStackTrace();
			Log.e("err", "deleteTagForAllContact failed.");
		}
	}

	
	/**
	 * ����id����Main��
	 */
	private void updateBasicById(ContactItem item) throws SQLException
	{
		// ����Ƿ���Ϸ�id
		if( item == null || !isId(item.getId()) )
			return;
		ContentValues values = new ContentValues();
		values.put(Constants.MAIN_COL_NAME, item.getName());
		values.put(Constants.MAIN_COL_FULL_PINYIN, item.getFullPinyin());
		values.put(Constants.MAIN_COL_SIM_PINYIN, item.getSimplePinyin());
		values.put(Constants.MAIN_COL_NOTE, item.getNote());
		values.put(Constants.MAIN_COl_PHONE, item.getPhoneNumber());
		values.put(Constants.MAIN_COL_ADDRESS, item.getAddress());
		db.update(Constants.MAIN_TABLE_NAME, values, "c_id=?", 
				new String[]{ item.getId() + "" });
	}
}
