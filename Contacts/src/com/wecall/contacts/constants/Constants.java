package com.wecall.contacts.constants;

import android.os.Environment;

/**
 * ����������й���������Ҫ�õ���һЩ����
 * @author xiaoxin
 *	2015-4-2
 */
public class Constants {

	//���ݿ���
	public static final String DATABASE_NAME = "test1.db";
	//������
	public static final String MAIN_TABLE_NAME = "contact";
	//���ݿ�汾
	public static final int DATABASE_VERSION = 1;
	//����ͼƬ��Ŀ¼
	public static final String ALBUM_PATH = Environment.getExternalStorageDirectory()
			+ "/wecall/picture/";
	
	
	//��ҳ�໬�˵�������Ļ��
	public static final int INIT_MENU_PADDING = 100;
}
