package org.lucene.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

/**
 * �ִ���ʵ��
 * @author XF
 * 2014-5-3
 */
public class WeCallAnalyzer {
	
	public WeCallAnalyzer(Analyzer a)
	{
		analyzer=a;
	}
	/*
	 * getAnalyzeString������������һ���ַ������ִ�������һ�������ֺôʣ�Ȼ�����ַ���֮���ո�Ȼ�󷵻�һ����ÿո���ַ���
	 * ����str="��������ɽ��ѧ����԰2��612"
	 * �ִʺ�str="��ɽ��   ��ɽ��ѧ   ����    ԰   2   ��    612"
	 * �����������INSERT��UPDATEʱ��ķִ�
	 */
	public String getAnalyzedString(String str)
	{
		String result="";
		try 
		{
			TokenStream stream=analyzer.tokenStream("content", new StringReader(str));
			CharTermAttribute cta=stream.addAttribute(CharTermAttribute.class);
			while(stream.incrementToken())
			{
				result+=cta+" ";
			}
			
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	/*
	 * getTokenList�����᷵��һ��Token����б�����Token���ڼ�¼ÿ���ִʵ�value�Լ�����ԭ�ַ����е�ƫ����
	 * */
	@SuppressWarnings("unused")
	public List<String> getTokenList(String str)
	{
		List<String> list=new ArrayList<String>();
		try 
		{
			TokenStream stream=analyzer.tokenStream("content", new StringReader(str));
			//CharTermAttribute �ַ�ֵ
			CharTermAttribute cta=stream.addAttribute(CharTermAttribute.class);
			//OffsetAttribute���ڼ�¼�ַ�ֵ����ʼλ�ú���ֹλ��
			OffsetAttribute oa=stream.addAttribute(OffsetAttribute.class);
			while(stream.incrementToken())
			{
				//Token token=new Token(cta+"",oa.startOffset(),oa.endOffset());
				list.add(new String(cta + ""));
			}
			
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	private Analyzer analyzer;
}
