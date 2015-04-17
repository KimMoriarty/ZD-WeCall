package com.wecall.contacts.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * ͼƬ��������
 * 
 * @author xiaoxin
 */
public class ImageUtil {

	/**
	 * ���ı�ת��Ϊ��ά��
	 * 
	 * @param content
	 *            ������ַ���
	 * @return ���ɵĶ�ά��
	 * @throws WriterException
	 */
	public static Bitmap CreateQRCode(String content,int size) throws WriterException {
		// ���ɶ�ά����,����ʱָ����С,��Ҫ������ͼƬ�Ժ��ٽ�������,������ģ������ʶ��ʧ��
		BitMatrix matrix = new MultiFormatWriter().encode(content,
				BarcodeFormat.QR_CODE, size, size);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		// ��ά����תΪһά��������,Ҳ����һֱ��������
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = 0xff000000;
				}
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		// ͨ��������������bitmap,����ο�api
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
	/**
	 * ��ȡ���ص�ͼƬ
	 * @param path ͼƬ����·��
	 * @param filename ͼƬ��
	 * @return ͼƬ��bitmap����
	 */
	public static Bitmap getLocalBitmap(String path,String filename){
		Bitmap bitmap = null;  
	    try  
	    {  
	        File file = new File(path+filename);  
	        if(file.exists())  
	        {  
	            bitmap = BitmapFactory.decodeFile(path+filename);  
	        }  
	    } catch (Exception e)  
	    {  
	        e.printStackTrace();
	    }  
	    
	    return bitmap;  
	}

	/**
	 * ����ͼƬ
	 * @param bitmap Ҫ�����bitmapͼƬ
	 * @param path ·��
	 * @param fileName �ļ���
	 * @throws IOException
	 */
	public static void saveImage(Bitmap bitmap, String path, String fileName)
			throws IOException {
		File dirFile = new File(path);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		File myCaptureFile = new File(path + fileName);
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(myCaptureFile));
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		bos.flush();
		bos.close();
	}

	/**
	 * ����ͼƬ
	 * @param bitmap Ҫ�����bitmapͼƬ
	 * @param path ·��
	 * @param fileName �ļ���
	 * @param ratio ͼƬѹ������
	 * @throws IOException
	 */
	public static void saveImage(Bitmap bitmap, String path, String fileName,
			int ratio) throws IOException {
		File dirFile = new File(path);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File myCaptureFile = new File(path + fileName);
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(myCaptureFile));
		bitmap.compress(Bitmap.CompressFormat.JPEG, ratio, bos);
		bos.flush();
		bos.close();
	}
	
	/**
	 * ɾ��ͼƬ
	 * @param path ����·��
	 * @param fileName ͼƬ��
	 */
	public static void deleteImage(String path,String fileName){
		File file = new File(path+fileName);
		if(file.exists()){
			if(file.isFile()){
				file.delete();
			}
		}
	}
	
	/**
	 * ������ͼƬ
	 * @param oldpath ��·��
	 * @param newpath ��·��
	 */
	public static void renameImage(String oldpath,String newpath){
		File file = new File(oldpath);
		File newFile = new File(newpath);
		if(file.exists()){  
            file.renameTo(newFile);  
        } 
	}
	
}
