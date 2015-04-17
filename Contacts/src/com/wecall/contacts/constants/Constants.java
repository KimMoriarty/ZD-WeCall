package com.wecall.contacts.constants;

import android.os.Environment;

/**
 * ����������й���������Ҫ�õ���һЩ����
 * @author xiaoxin
 *	2015-4-2
 */
public class Constants {

	//���ݿ���
	public static final String DATABASE_NAME = "contact.db";
	//������
	public static final String MAIN_TABLE_NAME = "main";
	//���� cid����
	public static final String MAIN_COL_CID = "c_id";
	//���� name����
	public static final String MAIN_COL_NAME = "name";
	//����fullPinyin����
	public static final String MAIN_COL_FULL_PINYIN = "fullPinyin";
	//����simplePinyin����
	public static final String MAIN_COL_SIM_PINYIN = "simplePinyin";
	//����note����
	public static final String MAIN_COL_NOTE = "note";
	//����phoneNumber����
	public static final String MAIN_COl_PHONE = "phoneNumber";
	//����address����
	public static final String MAIN_COL_ADDRESS = "address";
	
	//��ǩ����
	public static final String TAG_TABLE_NAME = "tag";
	//��ǩ��cid����
	public static final String TAG_COL_CID = "c_id";
	//��ǩ��tag����
	public static final String TAG_COL_TAG = "tag";
	
	//���ݿ�汾
	public static final int DATABASE_VERSION = 1;
	//����ͼƬ��Ŀ¼
	public static final String ALBUM_PATH = Environment.getExternalStorageDirectory()
			+ "/wecall/picture/";
	//AES��Կ
	public static final String AESKEY = "xiaoxin";
	
	//��ҳ�໬�˵�������Ļ��
	public static final int INIT_MENU_PADDING = 100;
}
