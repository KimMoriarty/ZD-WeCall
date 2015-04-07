package com.wecall.contacts;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.wecall.contacts.constants.Constants;
import com.wecall.contacts.util.ImageUtil;
import com.wecall.contacts.util.SPUtil;
import com.wecall.contacts.view.DetailBar;
import com.wecall.contacts.view.DetailBar.DetailBarClickListener;

/**
 * ����ҳActivity
 * 
 * @author xiaoxin 2015-4-5
 */
public class Setting extends Activity {

	private static final String TAG = "Setting";
	// ��ͬ�����������
	private static final int ALBUM_REQUEST_CODE = 1;
	private static final int CAMERA_REQUEST_CODE = 2;
	private static final int CROP_REQUEST_CODE = 3;

	// Activity�ϵĿؼ���
	private EditText nameET, phoneET;
	private Button confireBTN;
	private DetailBar topBar;
	private LinearLayout aboutLayout;
	private ImageButton pictureIBTN;

	// �û����͵绰
	private String mName;
	private String mPhone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		// ��ʼ���ؼ�
		initView();
		// ��ʼ������
		initData();
	}

	// ��ʼ���ؼ�
	private void initView() {
		nameET = (EditText) findViewById(R.id.et_setting_name);
		phoneET = (EditText) findViewById(R.id.et_setting_phone);
		confireBTN = (Button) findViewById(R.id.btn_setting_confire);
		topBar = (DetailBar) findViewById(R.id.db_setting_topbar);
		aboutLayout = (LinearLayout) findViewById(R.id.ll_setting_about);
		pictureIBTN = (ImageButton) findViewById(R.id.ibtn_setting_photo);
		confireBTN.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// �������ļ���д��������Ϣ
				SPUtil.put(Setting.this, "name", nameET.getText().toString());
				SPUtil.put(Setting.this, "phone", phoneET.getText().toString());
				ImageUtil.renameImage(Constants.ALBUM_PATH + "showuser.jpg",
						Constants.ALBUM_PATH + "user.jpg");
				// ���óɹ���������
				setResult(RESULT_OK);
				finish();
			}
		});

		// ʵ��topBar���Զ������¼�
		topBar.setOnDetailBarClickListener(new DetailBarClickListener() {

			@Override
			public void rightClick() {

			}

			@Override
			public void leftClick() {
				finish();
			}

			@Override
			public void infoClick() {

			}
		});

		// ������ڣ���ת�����ڽ���
		aboutLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(Setting.this, AboutActivity.class));
			}
		});

		// ������ҵ��û��趨��ͷ����ͷ������Ϊ�û��Զ����ͷ�񣬷�������ΪĬ��ͼƬ
		Bitmap userPhoto = ImageUtil.getLocalBitmap(Constants.ALBUM_PATH,
				"user.jpg");
		if (userPhoto == null) {
			pictureIBTN.setImageResource(R.drawable.ic_contact_picture);
		} else {
			pictureIBTN.setImageBitmap(userPhoto);
		}

		// ͷ��ĵ���¼�
		pictureIBTN.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// ��ʾѡ��ͼƬ��Դ�ĶԻ���
				showPicDialog();
			}
		});
	}

	// ���������Activity���ص�����
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(TAG, "requestCode:" + requestCode);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			// ����᷵��
			case ALBUM_REQUEST_CODE:
				startPhotoZoom(data.getData());
				break;
			// ���������
			case CAMERA_REQUEST_CODE:
				File tmp = new File(Constants.ALBUM_PATH + "tmpuser.jpg");
				startPhotoZoom(Uri.fromFile(tmp));
				break;
			// �Ӳü��󷵻�
			case CROP_REQUEST_CODE:
				if (data != null) {
					setPicToView(data);
				}
				break;

			default:
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * ��ͼƬ�ü�
	 * 
	 * @param uri
	 *            ͼƬ��uri��ַ
	 */
	private void startPhotoZoom(Uri uri) {
		Log.v(TAG, "Zoom:" + uri.toString());
		/*
		 * �����������Intent��ACTION����ô֪���ģ���ҿ��Կ����Լ�·���µ�������ҳ
		 * yourself_sdk_path/docs/reference/android/content/Intent.html
		 */
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// �������crop=true�������ڿ�����Intent��������ʾ��VIEW�ɲü�
		intent.putExtra("crop", "true");
		// aspectX aspectY �ǿ�ߵı���
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY �ǲü�ͼƬ���
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, CROP_REQUEST_CODE);
	}

	// ��ȡ�õ�ͼƬ���õ��ؼ���
	private void setPicToView(Intent data) {
		// ȡ�÷��ص�����
		Bundle bundle = data.getExtras();
		// ��Ϊ���򱣴�ͼƬ�����ز����õ��ؼ���
		if (bundle != null) {
			Bitmap picture = bundle.getParcelable("data");
			try {
				ImageUtil.saveImage(picture, Constants.ALBUM_PATH,
						"showuser.jpg");
			} catch (IOException e) {
				e.printStackTrace();
			}
			pictureIBTN.setImageBitmap(picture);
		}
	}

	@Override
	protected void onDestroy() {
		ImageUtil.deleteImage(Constants.ALBUM_PATH, "tmpuser.jpg");
		ImageUtil.deleteImage(Constants.ALBUM_PATH, "showuser.jpg");
		super.onDestroy();
	}

	// ��ʼ������
	private void initData() {
		Bundle bundle = getIntent().getExtras();
		mName = bundle.getString("name");
		mPhone = bundle.getString("phone");
		nameET.setText(mName);
		phoneET.setText(mPhone);
	}

	// ��ʾ�Ի���
	private void showPicDialog() {
		new AlertDialog.Builder(this)
				.setTitle("����ͷ��")
				.setNegativeButton("���", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// �öԻ�����ʧ
						dialog.dismiss();
						// ACTION_PICK�������ݼ�����ѡ��һ�����أ��ٷ��ĵ���������
						// Activity Action:
						// Pick an item from the data, returning what was
						// selected.
						Intent intent = new Intent(Intent.ACTION_PICK, null);
						// ����������Դ������
						intent.setDataAndType(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								"image/*");
						startActivityForResult(intent, ALBUM_REQUEST_CODE);
					}
				})
				.setPositiveButton("����", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
						/**
						 * ������仹�������ӣ����ÿ������չ��ܣ�����Ϊʲô�п������գ���ҿ��Բο����¹ٷ�
						 * �ĵ���you_sdk_path/docs/guide/topics/media/camera.html
						 */
						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						// ��ͼƬ����Ŀ¼�������Ŀ¼�����ڣ��򴴽���Ŀ¼
						File dirFile = new File(Constants.ALBUM_PATH);
						if (!dirFile.exists()) {
							dirFile.mkdirs();
						}
						// ��ͼƬ���浽��Ŀ¼��
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
								.fromFile(new File(Constants.ALBUM_PATH,
										"tmpuser.jpg")));
						startActivityForResult(intent, CAMERA_REQUEST_CODE);
					}
				}).show();
	}
}
