package com.wecall.contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.wecall.contacts.adapter.SortAdapter;
import com.wecall.contacts.constants.Constants;
import com.wecall.contacts.database.DatabaseManager;
import com.wecall.contacts.entity.ContactItem;
import com.wecall.contacts.util.EncodeUtil;
import com.wecall.contacts.util.ImageUtil;
import com.wecall.contacts.util.PinYin;
import com.wecall.contacts.util.SPUtil;
import com.wecall.contacts.view.ClearableEditText;
import com.wecall.contacts.view.SideBar;
import com.wecall.contacts.view.SideBar.onTouchLetterChangeListener;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private static final int INFO_REQUEST_CODE = 1;
	private static final int EDIT_REQUEST_CODE = 2;
	private static final int SETTING_REQUEST_CODE = 3;

	// ��ϵ���б�ؼ�
	private ListView contactListView;
	// ����������ؼ�
	private SideBar sideBar;
	// ��ɾ��������ؼ�
	private ClearableEditText inputEditText;
	// ��ʾ��ǰѡ�е���ĸ�������ı��ؼ�
	private TextView showAheadTV;
	// �û���ά��
	private ImageView qrcodeIMG;
	// �û�ͷ��
	private ImageView ownerPhoto;
	// �û������û��绰
	private TextView ownerNameTV, ownerPhoneTV;
	private String ownerName;
	private String ownerPhone;
	// ����
	private LinearLayout settingLL;
	// �����ϵ�˰�ť
	private ImageButton addContactBtn;
	// �����������
	private SortAdapter adapter;
	// ��ϵ����Ϣ
	private List<ContactItem> contactList;
	// ���ݿ����ʵ��
	private DatabaseManager mManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	// ��ʼ���ؼ�
	@SuppressWarnings("unchecked")
	private void initView() {
		contactListView = (ListView) findViewById(R.id.lv_contacts);
		sideBar = (SideBar) findViewById(R.id.sidebar);
		inputEditText = (ClearableEditText) findViewById(R.id.ed_input);
		showAheadTV = (TextView) findViewById(R.id.tv_show_ahead);
		addContactBtn = (ImageButton) findViewById(R.id.ibtn_addcontact);
		qrcodeIMG = (ImageView) findViewById(R.id.iv_owner_qrcode);
		ownerPhoto = (ImageView) findViewById(R.id.iv_owner_photo);
		ownerNameTV = (TextView) findViewById(R.id.tv_owner_name);
		ownerPhoneTV = (TextView) findViewById(R.id.tv_owner_phone);
		settingLL = (LinearLayout) findViewById(R.id.ll_setting);
		mManager = new DatabaseManager(this);
		sideBar.setLetterShow(showAheadTV);

		sideBar.setTouchLetterChangeListener(new onTouchLetterChangeListener() {

			@Override
			public void onTouchLetterChange(String letter) {
				int position = adapter.getPositionForSection(letter.charAt(0));
				if (position != -1) {
					contactListView.setSelection(position);
				}
			}
		});
		contactListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(MainActivity.this, ContactInfo.class);
				Bundle bundle = new Bundle();
				bundle.putInt("cid",
						((ContactItem) adapter.getItem(arg2)).getId());
				intent.putExtras(bundle);
				startActivityForResult(intent, INFO_REQUEST_CODE);
			}
		});

		// ��ȡ��ϵ����Ϣ
		filledData(getResources().getStringArray(R.array.date));
		inputEditText.setHint("������" + contactList.size() + "λ��ϵ��");
		// ����ϵ�˰�����ĸ��˳������
		Collections.sort(contactList);
		adapter = new SortAdapter(contactList, this);
		contactListView.setAdapter(adapter);

		inputEditText = (ClearableEditText) findViewById(R.id.ed_input);
		// �������������ֵ�ĸı�����������
		inputEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// ������������ֵΪ�գ�����Ϊԭ�����б�����Ϊ���������б�
				filterData(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		addContactBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this,
						ContactEditor.class);
				Bundle bundle = new Bundle();
				bundle.putInt("type", 1);
				intent.putExtras(bundle);
				startActivityForResult(intent, EDIT_REQUEST_CODE);
			}
		});

		settingLL.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this, Setting.class);
				Bundle bundle = new Bundle();
				bundle.putString("name", ownerName);
				bundle.putString("phone", ownerPhone);
				intent.putExtras(bundle);
				startActivityForResult(intent, SETTING_REQUEST_CODE);
			}
		});
		setOwnerInfo();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.v(TAG, "requestcode:" + requestCode + " resultcode:" + resultCode);
		if (requestCode == EDIT_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "��ӳɹ�", Toast.LENGTH_SHORT).show();
				refreshContacts();
			}
		} else if (requestCode == INFO_REQUEST_CODE) {
			// ɾ����ϵ�˳ɹ�����RESULT_OK
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "ɾ���ɹ�", Toast.LENGTH_SHORT).show();
				refreshContacts();
			}
		} else if (requestCode == SETTING_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				setOwnerInfo();
				Toast.makeText(this, "�û���Ϣ�޸ĳɹ�", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			View view = getCurrentFocus();
			if (isShouldHideKeyboard(view, ev)) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			}
			return super.dispatchTouchEvent(ev);
		}

		if (getWindow().superDispatchTouchEvent(ev)) {
			return true;
		}

		return onTouchEvent(ev);
	}

	private boolean isShouldHideKeyboard(View view, MotionEvent ev) {
		if (view != null && (view instanceof EditText)) {
			int[] leftTop = { 0, 0 };
			view.getLocationInWindow(leftTop);
			int left = leftTop[0];
			int top = leftTop[1];
			int right = left + view.getWidth();
			int bottom = top + view.getHeight();

			if (ev.getX() > left && ev.getX() < right && ev.getY() > top
					&& ev.getY() < bottom) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * ΪListView�������
	 * 
	 * @param date
	 */
	@SuppressLint("DefaultLocale")
	private void filledData(String[] data) {

		contactList = mManager.queryAllContact();

		Log.v(TAG, "size:" + contactList.size());
		if (contactList == null || contactList.size() == 0) {
			String areas[] = { "����ɽ", "�㶫����", "�ɶ�", "NewYork", "�ܴ�", "Ȫ��", "��ɽ" };
			for (int i = 0; i < data.length; i++) {

				ContactItem contactItem = new ContactItem();
				contactItem.setName(data[i]);
				contactItem.setPhoneNumber(genRandomPhone());
				int ind = (int) (Math.random() * areas.length);
				contactItem.setAddress(areas[ind]);

				contactList.add(contactItem);
			}
			mManager.addContacts(contactList);
		}

	}

	/**
	 * ����������е�ֵ���������ݲ�����ListView �ɸ���ƴ�������֣���д������
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		List<ContactItem> filterDateList = new ArrayList<ContactItem>();

		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = contactList;
		} else {
			filterDateList.clear();
			for (ContactItem contactItem : contactList) {
				String filterStrInPinyin = PinYin.getPinYin(filterStr);
				String name = contactItem.getName();
				String fullPinyin = contactItem.getFullPinyin();
				String simplePinyin = contactItem.getSimplePinyin();
				if (name.contains(filterStr)
						|| fullPinyin.contains(filterStrInPinyin)
						|| simplePinyin.contains(filterStrInPinyin)) {
					filterDateList.add(contactItem);
				}
			}
		}
		adapter.updateListView(filterDateList);
	}

	@SuppressWarnings("unchecked")
	private void refreshContacts() {
		contactList = mManager.queryAllContact();
		Collections.sort(contactList);
		adapter.updateListView(contactList);
		inputEditText.setHint("������" + contactList.size() + "λ��ϵ��");
	}

	/**
	 * �����û���Ϣ
	 */
	private void setOwnerInfo() {
		ownerName = (String) SPUtil.get(MainActivity.this, "name", "С��");
		ownerPhone = (String) SPUtil.get(MainActivity.this, "phone",
				"13929514504");

		ownerNameTV.setText(ownerName);
		ownerPhoneTV.setText(ownerPhone);
		Bitmap userPhoto = ImageUtil.getLocalBitmap(Constants.ALBUM_PATH,
				"user.jpg");
		if (userPhoto == null) {
			ownerPhoto.setImageResource(R.drawable.ic_contact_picture);
		} else {
			ownerPhoto.setImageBitmap(userPhoto);
		}
		JSONObject jsonObject = new JSONObject();
		Bitmap bitmap = null;
		try {
			jsonObject.put("name", ownerName);
			jsonObject.put("phone", ownerPhone);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String codedJson;
		try {
			try {
				codedJson = EncodeUtil.encrypt(Constants.AESKEY, jsonObject.toString());
			} catch (Exception e) {
				codedJson = jsonObject.toString();
				e.printStackTrace();
			}
			bitmap = ImageUtil.CreateQRCode(codedJson);
		} catch (WriterException e) {
			e.printStackTrace();
		}
		qrcodeIMG.setImageBitmap(bitmap);
	}

	/**
	 * ����������룬������
	 * 
	 * @return �������
	 */
	private String genRandomPhone() {
		String str = "";
		for (int i = 0; i < 11; i++) {
			str += (int) (Math.random() * 10);
		}
		return str;
	}

}
