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
	public static final int MAIN_COL_CID = 0;
	//���� name����
	public static final int MAIN_COL_NAME = 1;
	//����fullPinyin����
	public static final int MAIN_COL_FULL_PINYIN = 2;
	//����simplePinyin����
	public static final int MAIN_COL_SIM_PINYIN = 3;
	//����sortLetter����
	public static final int MAIN_COL_SORT = 4;
	//����note����
	public static final int MAIN_COL_NOTE = 5;
	
	//��ǩ����
	public static final String TAG_TABLE_NAME = "tag";
	//��ǩ��cid����
	public static final int TAG_COL_CID = 0;
	//��ǩ��tag����
	public static final int TAG_COL_TAG = 1;
	
	//��ֵ����
	public static final String MULTI_TABLE_NAME = "multiValue";
	//��ֵ��cid����
	public static final int MULTI_COL_CID = 0;
	//��ֵ��key����
	public static final int MULTI_COL_KEY = 1;
	//��ֵ��value����
	public static final int MULTI_COL_VALUE = 2;
	
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
