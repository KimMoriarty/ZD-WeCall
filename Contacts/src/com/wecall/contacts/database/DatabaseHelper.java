package com.wecall.contacts.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
import com.wecall.contacts.constants.*;

/*
 * �����������ݿ�
 * ���ݿ����4����main, tag, keyValue 
 * 
 * @author KM
 * 
 */

public class DatabaseHelper extends SQLiteOpenHelper{
	
	// ���ݿ�汾
    private static int VERSION = 1;
    
    /* main���������Ϣ
     * ������Ϊ��c_id, name, fullPinyin, simplePinyin, sortLetter, note
     */    
    private final static 
    String MAIN_TABLE = "CREATE TABLE IF NOT EXISTS main( "
    					+ "c_id INTEGER PRIMARY KEY, "
    					+ "name VARCHAR(100), "
    					+ "fullPinyin VARCHAR(100), "
    					+ "simplePinyin VARCHAR(20), "
    					+ "sortLetter VARCHAR(5), "
    					+ "note VARCHAR(255)"
    					+ ");";
     
    /*
     * tag�����ǩ�����
     * ������Ϊ��c_id, tag
     */
    private final static 
    String TAG_TABLE = "CREATE TABLE IF NOT EXISTS tag( "
    					+ "c_id INTEGER NOT NULL, "
    					+ "tag VARCHAR(50),"
    					+ "PRIMARY KEY(c_id, tag), " 
    					+ "FOREIGN KEY(c_id) REFERENCES main(c_id) ON DELETE CASCADE "
    					+ ");";   
						
    /*
     * multiValue�������ж�ֵԪ��
     * ������Ϊ��c_id, kkey, vvalue
     */
    private final static 
    String MULTI_VALUE_TABLE = "CREATE TABLE IF NOT EXISTS multiValue(" 
    					+ "c_id INTEGER NOT NULL, "
    					+ "kkey VARCHAR(50), "
    					+ "vvalue VARCHAR(100), "
    					+ "PRIMARY KEY(c_id, kkey, vvalue), "
    					+ "FOREIGN KEY(c_id) REFERENCES Main(c_id) ON DELETE CASCADE "
    					+ ");";
    
    // tag���϶�tag������
    private final static
    String MAIN_ID_INDEX = "CREATE INDEX main_cid_index on main(c_id);";
    private final static
    String TAG_ID_INDEX = "CREATE INDEX tag_cid_index on tag(c_id);";
    private final static
    String TAG_TAG_INDEX = "CREATE INDEX tag_tag_index on tag(tag);";
    private final static
    String MULTI_ID_INDEX = "CREATE INDEX multi_cid_index on multiValue(c_id);";
    
    public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context, String name, int version){
        this(context, name, null, version);
    }
    
    public DatabaseHelper(Context context, String name){
        this(context, name, VERSION);
    }
    
    public DatabaseHelper(Context context)
    {
    	this(context, Constants.DATABASE_NAME);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
    	try {
			// ���δ���������
			db.execSQL(MAIN_TABLE);
			db.execSQL(TAG_TABLE);
			db.execSQL(MULTI_VALUE_TABLE);
			//�����������
			db.execSQL(MAIN_ID_INDEX);
			db.execSQL(TAG_ID_INDEX);
			db.execSQL(TAG_TAG_INDEX);
			db.execSQL(MULTI_ID_INDEX);
		} catch (SQLException se) {
			// TODO Auto-generated catch block
			se.printStackTrace();
			Log.i("err", "create table failed.");
		}
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    }
}
