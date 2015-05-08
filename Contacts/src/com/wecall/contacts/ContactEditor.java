package com.wecall.contacts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wecall.contacts.constants.Constants;
import com.wecall.contacts.database.DatabaseManager;
import com.wecall.contacts.entity.ContactItem;
import com.wecall.contacts.util.AESUtil;
import com.wecall.contacts.util.HttpConnectionUtils;
import com.wecall.contacts.util.HttpHandler;
import com.wecall.contacts.util.ImageUtil;
import com.wecall.contacts.util.SPUtil;
import com.wecall.contacts.view.FlowLayout;

/**
 * ��ϵ�˱༭�࣬������ϵ���½������޸��¼�
 * 
 * @author xiaoxin 2015-4-3
 */
public class ContactEditor extends Activity {

	private static final String TAG = "ContactEditor";

	// ���ֱ༭��
	private EditText nameET, phoneET, addressET, noteET;
	private ImageView photoImg;
	private FlowLayout labelLayout;
	private ActionBar actionBar;
	private ImageButton addLabelButton;
	// ���ݿ�������
	private DatabaseManager mManager;

	// ��ǲ������ͣ�1Ϊ�½���2Ϊ�޸�
	private int mType = 1;
	// ��ϵ��id
	private int mCid = -1;
	private String mName;
	private String mPhone;
	// �����ʼ��ǩ��Ϣ
	private Set<String> preLabel = new HashSet<String>();
	private Set<String> curLabel = new HashSet<String>();

	private static final int ALBUM_REQUEST_CODE = 1;
	private static final int CAMERA_REQUEST_CODE = 2;
	private static final int CROP_REQUEST_CODE = 3;
	private static final int SCAN_REQUEST_CODE = 4;

	private static final int LABEL_EDIT_REQUEST_CODE = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_editor);
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		// �ж����½������޸���ϵ��
		confireType();
		// ��ʼ���ؼ�
		initView();
	}

	// ��ʼ���ؼ�
	private void initView() {
		nameET = (EditText) findViewById(R.id.et_name_add);
		phoneET = (EditText) findViewById(R.id.et_phone_add);
		addressET = (EditText) findViewById(R.id.et_address_add);
		noteET = (EditText) findViewById(R.id.et_note_add);
		photoImg = (ImageView) findViewById(R.id.img_photo_add);
		labelLayout = (FlowLayout) findViewById(R.id.fl_editor_label);
		addLabelButton = (ImageButton) findViewById(R.id.ibtn_label_add);

		mManager = new DatabaseManager(this);

		// ���½����޸Ľ��в�ͬ�ĳ�ʼ��
		if (mType == 1) {
			actionBar.setTitle("�½���ϵ��");
			if (mName != null && !mName.isEmpty()) {
				nameET.setText(mName);
			}
			if (mPhone != null && !mPhone.isEmpty()) {
				phoneET.setText(mPhone);
			}
		} else if (mType == 2) {
			actionBar.setTitle("�༭��ϵ��");
			ContactItem item = mManager.queryContactById(mCid);
			nameET.setText(item.getName());
			Set<String> phoneSet = item.getPhoneNumber();
			for (String str : phoneSet) {
				phoneET.setText(str);
			}

			addressET.setText(item.getAddress());
			noteET.setText(item.getNote());
			Bitmap bitmap = ImageUtil.getLocalBitmap(Constants.ALBUM_PATH,
					"pic" + mCid + ".jpg");
			if (bitmap == null) {
				photoImg.setImageResource(R.drawable.ic_contact_picture);
			} else {
				photoImg.setImageBitmap(bitmap);
			}
			preLabel = mManager.queryTagsByContactId(mCid);
			curLabel.addAll(preLabel);
			setLabels();
		}

		photoImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showPicDialog();
			}
		});

		addLabelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ContactEditor.this,
						ContactLabelEditor.class);
				intent.putExtra("cid", mCid);
				startActivityForResult(intent, LABEL_EDIT_REQUEST_CODE);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contact_editor_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			showReturnDialog();
			break;
		case R.id.action_editor_scan:
			Intent intent = new Intent(ContactEditor.this,
					MipcaActivityCapture.class);
			startActivityForResult(intent, SCAN_REQUEST_CODE);
			break;
		case R.id.action_editor_save:
			saveContact();
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void saveContact() {
		String name = nameET.getText().toString();
		if (name.isEmpty()) {
			Toast.makeText(ContactEditor.this, "����д����", Toast.LENGTH_SHORT)
					.show();
		} else {
			if (mType == 1) {
				int last = mManager.addContact(getContactFromView());
				Log.v(TAG, "insetid:" + last);
				ImageUtil.renameImage(Constants.ALBUM_PATH + "showpic.jpg",
						Constants.ALBUM_PATH + "pic" + last + ".jpg");
				setResult(RESULT_OK);
				finish();
			} else if (mType == 2) {
				ContactItem item = getContactFromView();
				item.setId(mCid);
				mManager.updateContact(item);
				ImageUtil.renameImage(Constants.ALBUM_PATH + "showpic.jpg",
						Constants.ALBUM_PATH + "pic" + mCid + ".jpg");
				setResult(RESULT_OK);
				finish();
			}
		}
	}

	private void showReturnDialog() {
		new AlertDialog.Builder(this).setTitle("�˳��˴α༭��")
				.setPositiveButton("��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (mType == 2) {
							mManager.updateContactTags(mCid, preLabel);
						}
						finish();
					}
				})
				.setNegativeButton("��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			showReturnDialog();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SCAN_REQUEST_CODE:
				Bundle bundle = data.getExtras();
				dealScanResult(bundle);
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
			case LABEL_EDIT_REQUEST_CODE:
				String[] labels = data.getStringArrayExtra("labels");
				Log.v(TAG, "labels:" + labels.length);
				curLabel.clear();
				curLabel.addAll(Arrays.asList(labels));
				setLabels();
				Toast.makeText(this, "��ǩ�༭�ɹ�", Toast.LENGTH_SHORT).show();
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
		if (mType == 0) {
			mType = Integer.parseInt(bundle.getString("type"));
		}
		if (mType == 1) {
			mName = bundle.getString("name");
			mPhone = bundle.getString("phone");
		}
		if (mType == 2) {
			mCid = bundle.getInt("cid");

		}
	}

	private ContactItem getContactFromView() {
		ContactItem item = new ContactItem();
		item.setName(nameET.getText().toString());
		Set<String> phoneSet = new HashSet<String>();
		phoneSet.add(phoneET.getText().toString());
		item.setPhoneNumber(phoneSet);
		item.setAddress(addressET.getText().toString());
		item.setNote(noteET.getText().toString());
		item.setLabels(curLabel);
		return item;
	}

	private void setLabels() {
		labelLayout.removeAllViews();
		for (String str : curLabel) {
			TextView tv = new TextView(this);
			MarginLayoutParams lp = new MarginLayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lp.setMargins(7, 10, 0, 0);
			tv.setText(str);
			tv.setBackgroundResource(R.drawable.label_bg_selected);
			tv.setTextSize(15);
			labelLayout.addView(tv, lp);

			Log.v("labels", str);
		}
	}

	/**
	 * ����ɨ����
	 * 
	 * @param bundle
	 */
	private void dealScanResult(Bundle bundle) {
		String obtained = bundle.getString("result");
		String aesKey = (String) SPUtil.get(ContactEditor.this, "aid",
				Constants.DEFAULT_AESKEY);
		try {
			JSONObject jsonObject = new JSONObject(obtained);
			int did = jsonObject.getInt("did");
			Log.v(TAG, "did:" + did);
			if (did == -1) {
				aesKey = Constants.DEFAULT_AESKEY;
				String data = "";
				try {
					data = AESUtil
							.decrypt(aesKey, jsonObject.getString("data"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				JSONObject jsonObject2 = new JSONObject(data);
				String name = jsonObject2.getString("name");
				String phone = jsonObject2.getString("phone");
				nameET.setText(name);
				phoneET.setText(phone);
			} else {
				getQRDataFromServer(obtained);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			try {
				JSONObject jsonObject = new JSONObject(AESUtil.decrypt(aesKey,
						obtained));
				String name = jsonObject.getString("name");
				String phone = jsonObject.getString("phone");
				nameET.setText(name);
				phoneET.setText(phone);
			} catch (JSONException e1) {
				e1.printStackTrace();
				Toast.makeText(this, "��Ч��ϵ�ˣ�" + bundle.getString("result"),
						Toast.LENGTH_LONG).show();
			} catch (Exception e1) {
				e1.printStackTrace();
				Toast.makeText(this, "��Ч��ϵ�ˣ�" + bundle.getString("result"),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void getQRDataFromServer(String str) {
		String url = Constants.SERVER_URL + "/analyseqrdata.php";
		int did = (Integer) SPUtil.get(this, "did", -1);
		String aesKey = (String) SPUtil.get(this, "aid",
				Constants.DEFAULT_AESKEY);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("did", String.valueOf(did)));
		try {
			list.add(new BasicNameValuePair("data", AESUtil
					.encrypt(aesKey, str)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Handler handler = new HttpHandler(this) {

			@Override
			protected void succeed(JSONObject jObject) {
				super.succeed(jObject);
				success(jObject);
			}

		};
		new HttpConnectionUtils(handler).post(url, list);
	}

	protected void success(JSONObject jObject) {
		// 200��ʾ�ɹ���400��ʾʧ��
		int state = 400;
		String keyStr = (String) SPUtil.get(this, "aid",
				Constants.DEFAULT_AESKEY);
		Log.v(TAG, "keyStr:" + keyStr);
		try {
			state = jObject.getInt("state");
			if (state == 200) {
				String data = AESUtil.decrypt(keyStr,
						jObject.getJSONObject("data").getString("data"));
				JSONObject jsonObject = new JSONObject(data);
				int did = jsonObject.getInt("did");
				mName = jsonObject.getString("name");
				mPhone = jsonObject.getString("phone");
				nameET.setText(mName);
				phoneET.setText(mPhone);
				Log.v(TAG, data);
				if (did != -1) {
					showNotifyDialog(did);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("HandlerLeak")
	private void showNotifyDialog(final int did) {
		final int pdid = (Integer) SPUtil.get(this, "did", -1);
		final String name = (String) SPUtil.get(this, "name", "");
		final String phone = (String) SPUtil.get(this, "phone", "");
		final String aesKey = (String) SPUtil.get(this, "aid",
				Constants.DEFAULT_AESKEY);
		final String url = Constants.SERVER_URL + "/notifybyid.php";
		new AlertDialog.Builder(this)
				.setTitle("�Ƿ�֪ͨ�Է�����Լ�Ϊ��ϵ��?")
				.setPositiveButton("��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						JSONObject jsonObject = new JSONObject();
						List<NameValuePair> list = new ArrayList<NameValuePair>();
						try {
							jsonObject.put("did", did);
							jsonObject.put("name", name);
							jsonObject.put("phone", phone);
							String data = AESUtil.encrypt(aesKey,
									jsonObject.toString());
							list.add(new BasicNameValuePair("did", String
									.valueOf(pdid)));
							list.add(new BasicNameValuePair("data", data));
						} catch (JSONException e) {
							e.printStackTrace();
						}
						Handler handler = new Handler() {

							@Override
							public void handleMessage(Message msg) {
								switch (msg.what) {
								case HttpConnectionUtils.DID_SUCCEED:
									Log.v(TAG, (String) msg.obj);
									Toast.makeText(ContactEditor.this, "��֪ͨ�Է�",
											Toast.LENGTH_SHORT).show();
									break;

								default:
									break;
								}
								super.handleMessage(msg);
							}

						};
						new HttpConnectionUtils(handler).post(url, list);
					}

				})
				.setNegativeButton("��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
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
		// aspectX aspectY �ǿ�ߵı���
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY �ǲü�ͼƬ���
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, CROP_REQUEST_CODE);
	}
}
