package com.wecall.contacts.util;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

/**
 * �ַ��������࣬�������ַ������в���
 * 
 * @author xiaoxin
 *
 */
public class StringUtil {

	/**
	 * ���ַ�����һ���ָı���ɫ
	 * @param str �����ַ���
	 * @param begin ��ʼ�±�
	 * @param end �����±�
	 * @param color Ҫ��ɵ�����
	 * @return ��ɫ����ַ���
	 */
	public static SpannableStringBuilder colorString(String str, int begin,
			int end, int color) {
		SpannableStringBuilder styled = new SpannableStringBuilder(str);
		styled.setSpan(new ForegroundColorSpan(color), begin, end,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return styled;
	}
	
	/**
	 * ���ַ������и�ʽ����Ϊ�գ����ء��ޡ�����Ϊ�գ�����ԭ�ַ���
	 * @param str
	 * @return
	 */
	public static String formatString(String str){
		if(str==null||str.equals("")){
			return "��";
		}else {
			return str;
		}
	}
}
