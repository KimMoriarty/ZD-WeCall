package com.wecall.contacts.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

/*
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
	
	/*
	 * �����ݿ�����µ�һ����ϵ��
	 * �������ɹ���ͬʱ����item�����id.
	 */
	public int addContact(ContactItem item)
	{		
		db.beginTransaction();
		try {
			// ����main��
			ContentValues values = new ContentValues();
			values.put("c_id", id);
			values.put("name", item.getName());
			values.put("fullPinyin", item.getFullPinyin());
			values.put("simplePinyin", item.getSimplePinyin());
			values.put("sortLetter", item.getSortLetter());
			values.put("note", item.getNote());		
			db.insert(Constants.MAIN_TABLE_NAME, null, values);
			
			// ����tag��
			ArrayList<String> list = item.getLabels();
			if (list != null) {
				for (String l : list) {
					values.clear();
					values.put("c_id", id);
					values.put("tag", l);
					db.insert(Constants.TAG_TABLE_NAME, null, values);
				}
			}
			
			// TODO:����multi��
			
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
	
	public void addContacts(List<ContactItem> list)
	{
		for(ContactItem item: list)
		{
			addContact(item);
		}
	}
	
	/*
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

	/*
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
			while(main_cursor.moveToNext())
			{
				ContactItem it = new ContactItem();
				it.setId(main_cursor.getInt(Constants.MAIN_COL_CID));
				it.setName(main_cursor.getString(Constants.MAIN_COL_NAME));
				it.setNote(main_cursor.getString(Constants.MAIN_COL_NOTE));				
				
				it.setLabels(queryTagById(it.getId()));
				
				// TODO ��Ӧÿ��id��multi��
				
				list.add(it);
			}		
			main_cursor.close();
		} catch (SQLException se) {
			se.printStackTrace();
			Log.i("err", "selectAll failed.");
		}
				
		return list;
	}
	
	public ContactItem queryContactById(int id)
	{
		ContactItem item = new ContactItem();
		try
		{
			Cursor cursor = db.query(Constants.MAIN_TABLE_NAME, new String[] {"*"}, 
					"c_id = ?", new String[] {id+""}, null, null, null);
			item.setId(id);
			item.setName(cursor.getString(Constants.MAIN_COL_CID));
			item.setName(cursor.getString(Constants.MAIN_COL_NOTE));
			cursor.close();
			
			item.setLabels(queryTagById(id));
			
			// TODO ��Ӧÿ��id��multi��
		} catch(Exception e)
		{
			e.printStackTrace();
			Log.i("err", "queryContactById error.");
		}

		return item;
	}
	
	/*
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
	
	/*
	 * ����id����tag��
	 */
	public void updateTagById(int id, ArrayList<String> tags)
	{		
		// ����Ƿ���Ϸ�id
		if( !isId(id) )
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
	
	/*
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
			
			// TODO ����multi��
			
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.i("err", "data update failed.");
		} finally
		{
			db.endTransaction();
		}
	}

	/*
	 * �ṩֱ��ִ��sql���Ľӿڣ�����������
	 * @Deprecated
	 */	
	public void execSQL(String sql)
	{
		db = helper.getWritableDatabase();
		db.execSQL(sql);
	}
	
	/*
	 * �ͷ���Դ��Ӧ��Activity onDestoryʱ����
	 */
	public void close()
	{
		db.close();
		helper.close();
	}
	
	public void printToLog()
	{
		ArrayList<ContactItem> list = queryAllContact();
		
	}
	
	/*
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
	
	/*
	 * ����id��ѯ��Ӧ������tag
	 */
	private ArrayList<String> queryTagById(int id) throws SQLException
	{		
		// ��Ӧÿ��id��tag��
		Cursor tag_cursor = db.query(Constants.TAG_TABLE_NAME, new String[] {"*"}, 
				"c_id=?", new String[] {id + ""}, null, null, null);
		
		ArrayList<String> labels = new ArrayList<String>();
		while(tag_cursor.moveToNext())
		{
			labels.add(tag_cursor.getString(Constants.TAG_COL_TAG));
		}
		tag_cursor.close();
		return labels;
	}
	
	/*
	 * ����id����Main��
	 */
	private void updateBasicById(ContactItem item) throws SQLException
	{
		// ����Ƿ���Ϸ�id
		if( !isId(item.getId()) )
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
}
