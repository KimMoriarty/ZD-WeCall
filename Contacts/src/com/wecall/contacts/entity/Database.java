package com.wecall.contacts.entity;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/*
 * �������ݿ��࣬����SQL���ϸ�ڣ��ṩ����ɾ����ѯ�ӿ�
 * 
 * @author KM
 */

public class Database {
	private SQLiteDatabase db = null;
	private DatabaseHelper helper;
	private final static String MAIN_TABLE = "main";
	private final static String ADDRESS_TABLE = "address";
	private final static String TAG_TABLE = "tag";
	private final static String PHONE_TABLE = "phoneNumber";
	// Ϊ��ϵ�˷����Ψһ��ʶ
	private static int id;
	// id���浽preference��
	private static SharedPreferences preferences;
	
	public Database(Context context)
	{
		helper = new DatabaseHelper(context, "Contacts.db");
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
	public void insert(ContactItem item)
	{		
		ContentValues values = new ContentValues();
		db.beginTransaction();
		try {
			// ����main��
			values.put("c_id", id);
			values.put("name", item.getName());
			values.put("fullPinyin", item.getFullPinyin());
			values.put("simplePinyin", item.getSimplePinyin());
			values.put("sortLetter", item.getSortLetter());
			values.put("note", item.getNote());		
			db.insert(MAIN_TABLE, null, values);
			
			// ����phoneNum��
			// TODO: ���Number
			values.clear();
			values.put("c_id", id);
			values.put("phoneNumber", item.getPhoneNumber());
			db.insert(PHONE_TABLE, null, values);
			
			// ����address��
			// TODO: ���ܶ��address
			values.clear();
			values.put("c_id", id);
			values.put("address", item.getAddress());
			db.insert(ADDRESS_TABLE, null, values);
			
			// ����tag��
			ArrayList<String> list = item.getLabels();
			if (list != null) {
				for (String l : list) {
					values.clear();
					values.put("c_id", id);
					values.put("tag", l);
					db.insert(TAG_TABLE, null, values);
				}
			}
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
	 * ͨ����ϵ�˱�ʶ��ɾ��һ����ϵ�˼�¼
	 * @parameter id�� ��ϵ�˱�ʶ��
	 */
	public void delete(int id)
	{			
		try {
			// �����Լ����ȷ������ɾ������˵Android2.2���֧��
			db.execSQL("PRAGMA foreign_keys=ON");
			db.delete(MAIN_TABLE, "c_id = ?", new String[]{ Integer.toString(id) } );
		} catch (SQLException e) {
			e.printStackTrace();
			Log.i("err", "delete based on id failed.");
		}
	}
	
	/*
	 * ͨ����ϵ�˱�ʶ�Ÿ��¼�¼
	 * ͨ�������item.idʶ����Ҫɾ������
	 * FIXME: Ŀǰֻ��ȫ����һ����£�����Ч�ʵͣ���Ҫ���ظ�������İ汾��ָ����Ŀ����
	 * FIXME: �绰����Ҳ��Ҫ����id������֪���ɺ����Ƿ�Ҫɾ��
	 */
	public void update(ContactItem item)
	{		
		ContentValues values = new ContentValues();
		db.beginTransaction();
		try {
			// ����main��
			values.put("name", item.getName());
			values.put("fullPinyin", item.getFullPinyin());
			values.put("simplePinyin", item.getSimplePinyin());
			values.put("sortLetter", item.getSortLetter());
			values.put("note", item.getNote());	
			db.update(MAIN_TABLE, values, "c_id = ?", 
					new String[]{Integer.toString(item.getId())});
			
			// ����phoneNum�����ǲ�������
			values.clear();
			values.put("c_id", item.getId());
			values.put("phoneNumber", item.getPhoneNumber());
			db.insert(PHONE_TABLE, null, values);
			
			// ����address��
			values.clear();
			values.put("c_id", item.getId());
			values.put("address", item.getAddress());
			db.update(ADDRESS_TABLE, values, "c_id = ?", 
					new String[]{Integer.toString(item.getId())});
			
			// ����tag��
			ArrayList<String> list = item.getLabels();
			if (list != null) {
				for (String l : list) {
					values.clear();
					values.put("c_id", item.getId());
					values.put("tag", l);
					db.insert(TAG_TABLE, null, values);
				}
			}
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
	 * ��������������
	 * @TODO���ṩ���������ӿ�
	 */
	public ArrayList<ContactItem> query(String name)
	{
		ArrayList<ContactItem> list = null;
		try {
			String table = MAIN_TABLE + " left outer join " + PHONE_TABLE + 
					" on " + MAIN_TABLE + ".c_id = " + PHONE_TABLE + ".c_id" +
					" left outer join " + ADDRESS_TABLE + 
					" on " + MAIN_TABLE + ".c_id = " + ADDRESS_TABLE + ".c_id" +
					" left outer join " + TAG_TABLE +
					" on " + MAIN_TABLE + ".c_id = " + TAG_TABLE + ".c_id";
			Cursor cursor = db.query(table, new String[] {"*"}, "name=?", 
					new String[] {name}, null, null, null);
			Log.i("Cursor", Integer.toString(cursor.getCount()));
			list = convertCursorToItem(cursor);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.i("err", "query by name failed.");
		}		
		return list;
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
	
	/*
	 * ����ѯ���cursorת��Ϊ�б��������
	 */	
	private ArrayList<ContactItem> convertCursorToItem(Cursor cursor)
	{
		ArrayList<ContactItem> list = new ArrayList<ContactItem>();
		
		while(cursor.moveToNext())
		{
			ContactItem it = new ContactItem();
			it.setId(cursor.getInt(0));
			it.setName(cursor.getString(1));
			
			list.add(it);
		}
				
		return list;
	}
}