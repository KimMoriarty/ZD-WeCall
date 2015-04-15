package com.wecall.contacts.database;

import java.util.ArrayList;
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
	public void insertContact(ContactItem item)
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
		} catch (SQLException e) {
			e.printStackTrace();
			Log.i("err", "data insert failed.");
		} finally {
			item.setId(id);
			id++;
			db.endTransaction();
		}
		Editor editor = preferences.edit();
		editor.putInt("id_count", id);
		editor.commit();
	}
	
	/*
	 * ���뵥����tag�����뱣֤��ԭ�в��ظ�
	 */
	public void insertTagById(int id, String tag)
	{
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
	public ArrayList<ContactItem> selectAll()
	{
		ArrayList<ContactItem> list = new ArrayList<ContactItem>();
		Cursor main_cursor = null;
		Cursor tag_cursor = null;
		Cursor multi_cursor = null;

		try{
			main_cursor = db.query(Constants.MAIN_TABLE_NAME, new String[] {"*"}, 
					null, null, null, null, null);
			while(main_cursor.moveToNext())
			{
				ContactItem it = new ContactItem();
				it.setId(main_cursor.getInt(Constants.MAIN_COL_CID));
				it.setName(main_cursor.getString(Constants.MAIN_COL_NAME));
				it.setNote(main_cursor.getString(Constants.MAIN_COL_NOTE));				
				
				// ��tag��
				tag_cursor = db.query(Constants.TAG_TABLE_NAME, new String[] {"*"}, 
						"c_id=?", new String[] {it.getId() + ""}, null, null, null);
				
				ArrayList<String> labels = new ArrayList<String>();
				while(tag_cursor.moveToNext())
				{
					labels.add(tag_cursor.getString(Constants.TAG_COL_TAG));
				}
				it.setLabels(labels);
				tag_cursor.close();
				
				// TODO ���Ӷ�ֵ���������Ե�����
//				multi_cursor = db.query(Constants.MULTI_TABLE_NAME, new String[] {"*"}, 
//						"c_id=?", new String[] {it.getId() + ""}, null, null, null);
//				while(tag_cursor.moveToNext())
//				{
//					
//				}
				
				list.add(it);
			}		
			main_cursor.close();
		} catch (SQLException se) {
			se.printStackTrace();
			Log.i("err", "selectAll failed.");
		}
				
		return list;
	}
	
	/*
	 * ͨ����ϵ�˱�ʶ��ɾ��һ����ϵ�˼�¼
	 * @parameter id�� ��ϵ�˱�ʶ��
	 */
	public void deleteById(int id)
	{			
		try {
			// �����Լ����ȷ������ɾ������˵Android2.2���֧��
			db.execSQL("PRAGMA foreign_keys=ON");
			db.delete(Constants.MAIN_TABLE_NAME, "c_id = ?", new String[]{ id + "" } );
		} catch (SQLException e) {
			e.printStackTrace();
			Log.i("err", "delete based on id failed.");
		}
	}
	
	private void updateBasicById(ContactItem item)
	{
		// ����Ƿ���Ϸ�id
		if(item.getId() <= 0)
		{
			Log.i("err", "updateBasicById: id illegal.");
			return;
		}
		try {
			ContentValues values = new ContentValues();
			values.put("name", item.getName());
			values.put("fullPinyin", item.getFullPinyin());
			values.put("simplePinyin", item.getSimplePinyin());
			values.put("sortLetter", item.getSortLetter());
			values.put("note", item.getNote());	
			db.update(Constants.MAIN_TABLE_NAME, values, "c_id=?", 
					new String[]{ item.getId() + "" });
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i("err", "updateBasicById failed.");
		}
	}
	
	public void updateTagById(int id, ArrayList<String> tags)
	{		
		// ����Ƿ���Ϸ�id
		if(id <= 0)
		{
			Log.i("err", "updateTagById: id illegal.");
			return;
		}
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
	 * ͨ����ϵ�˱�ʶ�Ÿ��¼�¼
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
}
