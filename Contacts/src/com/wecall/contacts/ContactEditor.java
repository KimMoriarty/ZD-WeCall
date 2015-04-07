package com.wecall.contacts;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.ImageView;
import android.widget.Toast;

import com.wecall.contacts.constants.Constants;
import com.wecall.contacts.database.DatabaseManager;
import com.wecall.contacts.entity.ContactItem;
import com.wecall.contacts.util.EncodeUtil;
import com.wecall.contacts.util.ImageUtil;
import com.wecall.contacts.view.DetailBar;
import com.wecall.contacts.view.DetailBar.DetailBarClickListener;

/**
 * ��ϵ�˱༭�࣬������ϵ���½������޸��¼�
 * 
 * @author xiaoxin 2015-4-3
 */
public class ContactEditor extends Activity {

	private static final String TAG = "ContactEditor";

	// ��ά��ɨ�밴ť
	private Button scanBtn;
	// ����������
	private DetailBar topbar;
	// ���ֱ༭��
	private EditText nameET, phoneET, addressET, noteET;
	private ImageView photoImg;
	// ���ݿ��������
	private DatabaseManager mManager;

	// ��ǲ������ͣ�1Ϊ�½���2Ϊ�޸�
	private int mType = 1;
	// ��ϵ��id
	private int mCid = -1;

	private static final int ALBUM_REQUEST_CODE = 1;
	private static final int CAMERA_REQUEST_CODE = 2;
	private static final int CROP_REQUEST_CODE = 3;
	private static final int SCAN_REQUEST_CODE = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_editor);
		// �ж����½������޸���ϵ��
		confireType();
		// ��ʼ���ؼ�
		initView();
	}

	// ��ʼ���ؼ�
	private void initView() {
		scanBtn = (Button) findViewById(R.id.btn_scan);
		nameET = (EditText) findViewById(R.id.et_name_add);
		phoneET = (EditText) findViewById(R.id.et_phone_add);
		addressET = (EditText) findViewById(R.id.et_address_add);
		noteET = (EditText) findViewById(R.id.et_note_add);
		topbar = (DetailBar) findViewById(R.id.db_topbar);
		photoImg = (ImageView) findViewById(R.id.img_photo_add);

		mManager = new DatabaseManager(this);

		// ���½����޸Ľ��в�ͬ�ĳ�ʼ��
		if (mType == 1) {
			topbar.setInfo("�½���ϵ��");
		} else if (mType == 2) {
			topbar.setInfo("�༭��ϵ��");
			ContactItem item = mManager.queryContactById(mCid);
			nameET.setText(item.getName());
			phoneET.setText(item.getPhoneNumber());
			addressET.setText(item.getAddress());
			noteET.setText(item.getNote());
			Bitmap bitmap = ImageUtil.getLocalBitmap(Constants.ALBUM_PATH,
					"pic" + mCid + ".jpg");
			if (bitmap == null) {
				photoImg.setImageResource(R.drawable.ic_contact_picture);
			} else {
				photoImg.setImageBitmap(bitmap);
			}
		}

		topbar.setOnDetailBarClickListener(new DetailBarClickListener() {

			@Override
			public void rightClick() {
				String name = nameET.getText().toString();
				if (name.isEmpty()) {
					Toast.makeText(ContactEditor.this, "����д����",
							Toast.LENGTH_SHORT).show();
				} else {
					if (mType == 1) {
						int last = mManager.addContact(getContactFromView());
						Log.v(TAG, "insetid:" + last);
						ImageUtil.renameImage(Constants.ALBUM_PATH
								+ "showpic.jpg", Constants.ALBUM_PATH + "pic"
								+ last + ".jpg");
						setResult(RESULT_OK);
						finish();
					} else if (mType == 2) {
						ContactItem item = getContactFromView();
						item.setId(mCid);
						mManager.updateContact(item);
						ImageUtil.renameImage(Constants.ALBUM_PATH
								+ "showpic.jpg", Constants.ALBUM_PATH + "pic"
								+ mCid + ".jpg");
						setResult(RESULT_OK);
						finish();
					}
				}
			}

			@Override
			public void leftClick() {
				finish();
			}

			@Override
			public void infoClick() {

			}
		});

		scanBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(ContactEditor.this,
						MipcaActivityCapture.class);
				startActivityForResult(intent, SCAN_REQUEST_CODE);
			}
		});

		photoImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showPicDialog();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SCAN_REQUEST_CODE:
				Bundle bundle = data.getExtras();
				String obtained = bundle.getString("result");
				try {
					JSONObject jsonObject = new JSONObject(obtained);
					String name = jsonObject.getString("name");
					String phone = jsonObject.getString("phone");
					nameET.setText(name);
					phoneET.setText(phone);
				} catch (JSONException e) {
					e.printStackTrace();
					try {
						JSONObject jsonObject = new JSONObject(
								EncodeUtil.decrypt(Constants.AESKEY, obtained));
						String name = jsonObject.getString("name");
						String phone = jsonObject.getString("phone");
						nameET.setText(name);
						phoneET.setText(phone);
					} catch (JSONException e1) {
						e1.printStackTrace();
						Toast.makeText(this,
								"��Ч��ϵ�ˣ�" + bundle.getString("result"),
								Toast.LENGTH_LONG).show();
					} catch (Exception e1) {
						e1.printStackTrace();
						Toast.makeText(this,
								"��Ч��ϵ�ˣ�" + bundle.getString("result"),
								Toast.LENGTH_LONG).show();
					}
				}
				break;
			// ����᷵��
			case ALBUM_REQUEST_CODE:
				startPhotoZoom(data.getData());
				break;
			// ���������
			case CAMERA_REQUEST_CODE:
				File tmp = new File(Constants.ALBUM_PATH + "tmppic.jpg");
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

	}

	@Override
	protected void onDestroy() {
		ImageUtil.deleteImage(Constants.ALBUM_PATH, "tmppic.jpg");
		ImageUtil.deleteImage(Constants.ALBUM_PATH, "showpic.jpg");
		super.onDestroy();
	}

	private void confireType() {
		Bundle bundle = getIntent().getExtras();
		mType = bundle.getInt("type");
		if (mType == 2) {
			mCid = bundle.getInt("cid");
		}
	}

	private ContactItem getContactFromView() {
		ContactItem item = new ContactItem();
		item.setName(nameET.getText().toString());
		item.setPhoneNumber(phoneET.getText().toString());
		item.setAddress(addressET.getText().toString());
		item.setNote(noteET.getText().toString());
		return item;
	}

	// ��ʾ�Ի���
	// TODO: ���ú�������
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
										"tmppic.jpg")));
						startActivityForResult(intent, CAMERA_REQUEST_CODE);
					}
				}).show();
	}

	// ��ȡ�õ�ͼƬ���õ��ؼ���
	// TODO��ʵ�ָ���
	private void setPicToView(Intent data) {
		// ȡ�÷��ص�����
		Bundle bundle = data.getExtras();
		// ��Ϊ���򱣴�ͼƬ�����ز����õ��ؼ���
		if (bundle != null) {
			Bitmap picture = bundle.getParcelable("data");
			try {
				ImageUtil.saveImage(picture, Constants.ALBUM_PATH,
						"showpic.jpg");
			} catch (IOException e) {
				e.printStackTrace();
			}
			photoImg.setImageBitmap(picture);
		}
	}

	/**
	 * ��ͼƬ�ü�
	 * 
	 * @param uri
	 *            ͼƬ��uri��ַ
	 */
	// TODO: ʵ�ָ���
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
		// aspectX aspectY �ǿ��ߵı���
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY �ǲü�ͼƬ����
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, CROP_REQUEST_CODE);
	}
}