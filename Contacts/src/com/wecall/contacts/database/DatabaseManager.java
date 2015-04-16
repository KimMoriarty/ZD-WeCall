package com.wecall.contacts.database;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.wecall.contacts.entity.ContactItem;
import android.util.Log;
import com.wecall.contacts.constants.Constants;

/**
 * �������ݿ��࣬����SQL���ϸ�ڣ��ṩ����ɾ����ѯ�ӿ�
 * 
 * @author KM
 */

public class DatabaseManager {
	
	private SQLiteDatabase db = null;
	private DatabaseHelper helper = null;
	// Ϊ��ϵ�˷����Ψһ��ʶ
	private static int id;
	// id���浽preference��
	private static SharedPreferences preferences;
	
	public DatabaseManager(Context context)
	{
		helper = new DatabaseHelper(context);
		// ���������ݿ�Ŀ�д����
		db = helper.getWritableDatabase();
		// ͨ��shared preference��ʼ��idֵ
		preferences = context.getSharedPreferences("database", Context.MODE_PRIVATE);
		id = preferences.getInt("id_count", 1);
	}
	
	/**
	 * �����ݿ�����µ�һ����ϵ��
	 * �������ɹ��᷵�����ݿ��Ӧ��id.
	 */
	public int addContact(ContactItem item)
	{		
		db.beginTransaction();
		try {
			// ����main��
			ContentValues values = new ContentValues();
			values.put(Constants.MAIN_COL_CID, id);
			values.put(Constants.MAIN_COL_NAME, item.getName());
			values.put(Constants.MAIN_COL_FULL_PINYIN, item.getFullPinyin());
			values.put(Constants.MAIN_COL_SIM_PINYIN, item.getSimplePinyin());
			values.put(Constants.MAIN_COL_SORT, item.getSortLetter());
			values.put(Constants.MAIN_COL_NOTE, item.getNote());		
			db.insert(Constants.MAIN_TABLE_NAME, null, values);
			
			// ����tag��
			ArrayList<String> list = item.getLabels();
			if (list != null) {
				for (String l : list) {
					values.clear();
					values.put(Constants.TAG_COL_CID, id);
					values.put(Constants.TAG_COL_TAG, l);
					db.insert(Constants.TAG_TABLE_NAME, null, values);
				}
			}
			
			// ����multi��
			// �绰
			ContentValues phone = new ContentValues();
			phone.put(Constants.MAIN_COL_CID, id);
			phone.put(Constants.MULTI_COL_KEY, Constants.MULTI_KEY_PHONE);
			phone.put(Constants.MULTI_COL_VALUE, item.getPhoneNumber());
			db.insert(Constants.MULTI_TABLE_NAME, null, phone);
			
			// ��ַ
			ContentValues address = new ContentValues();
			address.put(Constants.MULTI_COL_CID, id);
			address.put(Constants.MULTI_COL_KEY, Constants.MULTI_KEY_ADDRESS);
			address.put(Constants.MULTI_COL_VALUE, item.getAddress());
			db.insert(Constants.MULTI_TABLE_NAME, null, address);			
			
			db.setTransactionSuccessful();
			
			id++;
			Editor editor = preferences.edit();
			editor.putInt("id_count", id);
			editor.commit();
			return id;
		} catch (SQLException e) {
			e.printStackTrace();
			Log.i("err", "data insert failed.");
		} finally {
			db.endTransaction();
		}
		return -1;
	}
	
	/**
	 * ����������ϵ��
	 * @deprecated 
	 * @param list
	 */
	public void addContacts(List<ContactItem> list)
	{
		for(ContactItem item: list)
		{
			addContact(item);
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
			values.put("c_id", id);
			values.put("tag", tag);
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
		Cursor main_cursor = null;

		try{
			// ����main���Ѳ�ͬid������
			main_cursor = db.query(Constants.MAIN_TABLE_NAME, new String[] {"*"}, 
					null, null, null, null, null);
			int idIndex = main_cursor.getColumnIndex(Constants.MAIN_COL_CID);
			int nameIndex = main_cursor.getColumnIndex(Constants.MAIN_COL_NAME);
			int noteIndex = main_cursor.getColumnIndex(Constants.MAIN_COL_NOTE);
			try
			{
				while(main_cursor.moveToNext())
				{
					ContactItem it = new ContactItem();
					it.setId(main_cursor.getInt(idIndex));
					it.setName(main_cursor.getString(nameIndex));
					it.setNote(main_cursor.getString(noteIndex));				
					
					it.setLabels(queryTagById(it.getId()));
					
					// ��Ӧÿ��id��multi��
					Cursor multiCursor = db.query(Constants.MULTI_TABLE_NAME,
							new String[] {"*"}, "c_id = ?", new String[] {it.getId()+""}, 
							null, null, null);
					int keyIndex = multiCursor.getColumnIndex(Constants.MULTI_COL_KEY);
					int valueIndex = multiCursor.getColumnIndex(Constants.MULTI_COL_VALUE);
					try 
					{
						while(multiCursor.moveToNext())
						{
							String key = multiCursor.getString(keyIndex);
							String value = multiCursor.getString(valueIndex);
							if(key.equals(Constants.MULTI_KEY_PHONE))
							{
								it.setPhoneNumber(value);
							}
							else if(key.equals(Constants.MULTI_KEY_ADDRESS))
							{
								it.setAddress(value);
							}
							// TODO: ��������
						}
					} finally {
						multiCursor.close();
					}					
					list.add(it);
				}
			} finally{ 
				main_cursor.close();
			}
		} catch (SQLException se) {
			se.printStackTrace();
			Log.i("err", "selectAll failed.");
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
		ContactItem item = new ContactItem();
		try
		{
			Cursor cursor = db.query(Constants.MAIN_TABLE_NAME, new String[] {"*"}, 
					"c_id = ?", new String[] {id+""}, null, null, null);
			int nameIndex = cursor.getColumnIndex(Constants.MAIN_COL_NAME);
			int noteIndex = cursor.getColumnIndex(Constants.MAIN_COL_NOTE);
			int idIndex = cursor.getColumnIndex(Constants.MAIN_COL_CID);
			try
			{
				if(cursor.moveToFirst())
				{
					item.setId(cursor.getInt(idIndex));
					item.setName(cursor.getString(nameIndex));
					item.setNote(cursor.getString(noteIndex));
					Log.i("Contact", item+"");
				}
			} finally {
				cursor.close();
			}
				
			item.setLabels(queryTagById(id));
			
			// ��Ӧÿ��id��multi��
			Cursor multiCursor = db.query(Constants.MULTI_TABLE_NAME,
					new String[] {"*"}, "c_id = ?", new String[] {item.getId()+""}, 
					null, null, null);
			int keyIndex = multiCursor.getColumnIndex(Constants.MULTI_COL_KEY);
			int valueIndex = multiCursor.getColumnIndex(Constants.MULTI_COL_VALUE);
			try
			{
				while(multiCursor.moveToNext())
				{
					String key = multiCursor.getString(keyIndex);
					String value = multiCursor.getString(valueIndex);
					if(key.equals(Constants.MULTI_KEY_PHONE))
					{
						item.setPhoneNumber(value);
					}
					else if(key.equals(Constants.MULTI_KEY_ADDRESS))
					{
						item.setAddress(value);
					}
					// TODO: ��������
				}
			} finally {
				multiCursor.close();
			}

		} catch(Exception e)
		{
			e.printStackTrace();
			Log.i("err", "queryContactById error.");
		}

		return item;
	}
	
	/**
	 * ͨ����ϵ�˱�ʶ��ɾ��һ����ϵ�˼�¼
	 */
	public void deleteContact(int id)
	{			
		if( !isId(id) )
			return;
		try {
			// �����Լ����ȷ������ɾ������˵Android2.2���֧��
			db.execSQL("PRAGMA foreign_keys=ON");
			db.delete(Constants.MAIN_TABLE_NAME, "c_id = ?", new String[]{ id + "" } );
		} catch (SQLException e) {
			e.printStackTrace();
			Log.i("err", "delete based on id failed.");
		}
	}
	
	/**
	 * ����id����tag��
	 */
	public void updateTagById(int id, ArrayList<String> tags)
	{		
		// ����Ƿ���Ϸ�id
		if( !isId(id) || tags == null)
			return;
		// ��ɾ���뵱ǰid����������tag
		db.delete(Constants.TAG_TABLE_NAME, "c_id = ?", new String[] {id + ""});
		// �ٲ���ȫ���µ�tag
		for(String s: tags)
		{
			ContentValues values = new ContentValues();
			values.put("c_id", id + "");
			values.put("tag", s);
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
			
			// ����multi��
			updateMultiById(item);
			
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.i("err", "data update failed.");
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
		db = helper.getWritableDatabase();
		db.execSQL(sql);
	}
	
	/**
	 * �ͷ���Դ��Ӧ��Activity onDestoryʱ����
	 */
	public void close()
	{
		db.close();
		helper.close();
	}
	
	/**
	 * �ж��Ƿ�Ϸ���id
	 */
	private boolean isId(int id)
	{
		if(id <= 0)
		{
			Log.i("err", "ilegal id.");
			return false;
		}
		return true;
	}
	
	/**
	 * ����id��ѯ��Ӧ������tag
	 */
	private ArrayList<String> queryTagById(int id) throws SQLException
	{		
		// ��Ӧÿ��id��tag��
		Cursor tag_cursor = db.query(Constants.TAG_TABLE_NAME, new String[] {"*"}, 
				"c_id=?", new String[] {id + ""}, null, null, null);
		
		ArrayList<String> labels = new ArrayList<String>();
		int tagIndex = tag_cursor.getColumnIndex(Constants.TAG_COL_TAG);
		try {
			while(tag_cursor.moveToNext())
			{
				labels.add(tag_cursor.getString(tagIndex));
			}
		} finally {
			tag_cursor.close();
		}
		return labels;
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
		values.put("name", item.getName());
		values.put("fullPinyin", item.getFullPinyin());
		values.put("simplePinyin", item.getSimplePinyin());
		values.put("sortLetter", item.getSortLetter());
		values.put("note", item.getNote());	
		db.update(Constants.MAIN_TABLE_NAME, values, "c_id=?", 
				new String[]{ item.getId() + "" });
	}
	
	/**
	 * ����id����Multi��
	 */
	private void updateMultiById(ContactItem item) throws SQLException
	{
		// ����Ƿ���Ϸ�id
		if( item == null || !isId(item.getId()))
			return;
		ContentValues phone = new ContentValues();
		phone.put(Constants.MULTI_COL_KEY, Constants.MULTI_KEY_PHONE);
		phone.put(Constants.MULTI_COL_VALUE, item.getPhoneNumber());
		db.update(Constants.MULTI_TABLE_NAME, phone,
				Constants.MULTI_COL_CID + "=?", new String[] {item.getId() + ""});
		
		ContentValues address = new ContentValues();
		address.put(Constants.MULTI_COL_KEY, Constants.MULTI_KEY_ADDRESS);
		address.put(Constants.MULTI_COL_VALUE, item.getAddress());
		db.update(Constants.MULTI_TABLE_NAME, address,
				Constants.MULTI_COL_CID + "=?", new String[] {item.getId() + ""});
	}
}
