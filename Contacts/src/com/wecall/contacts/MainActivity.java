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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.wecall.contacts.adapter.SortAdapter;
import com.wecall.contacts.database.DatabaseManager;
import com.wecall.contacts.entity.ContactItem;
import com.wecall.contacts.util.ImageUtil;
import com.wecall.contacts.util.PinYin;
import com.wecall.contacts.view.ClearableEditText;
import com.wecall.contacts.view.SideBar;
import com.wecall.contacts.view.SideBar.onTouchLetterChangeListener;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private static final int REQUEST_CODE = 1;

	// ��ϵ���б�ؼ�
	private ListView contactListView;
	// ����������ؼ�
	private SideBar sideBar;
	// ��ɾ��������ؼ�
	private ClearableEditText inputEditText;
	// ��ʾ��ǰѡ�е���ĸ�������ı��ؼ�
	private TextView showAheadTV;
	private ImageView qrcodeIMG;
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
		qrcodeIMG = (ImageView)findViewById(R.id.iv_owner_qrcode);
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
				startActivity(intent);
			}
		});

		// ��ȡ��ϵ����Ϣ
		// TODO: use SQLite after
		filledData(getResources().getStringArray(R.array.date));

		// ����ϵ�˰�����ĸ��˳������
		Collections.sort(contactList);
		adapter = new SortAdapter(contactList, this);
		contactListView.setAdapter(adapter);

		inputEditText = (ClearableEditText) findViewById(R.id.ed_input);

		inputEditText.setHint("������" + contactList.size() + "λ��ϵ��");
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
				Intent intent = new Intent(MainActivity.this, ContactEditor.class);
				Bundle bundle = new Bundle();
				bundle.putInt("type", 1);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_CODE);
			}
		});
		
		setOwerQRCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "��ӳɹ�", Toast.LENGTH_SHORT).show();
				contactList = mManager.queryAllContact();
				Collections.sort(contactList);
				adapter.updateListView(contactList);
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
			mManager.addContact(contactList);
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

	private void setOwerQRCode(){
		String name = "С��";
		String phone = "13929514504";
		JSONObject jsonObject = new JSONObject();
		Bitmap bitmap = null ;
		try {
			jsonObject.put("name", name);
			jsonObject.put("phone", phone);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			bitmap = ImageUtil.CreateQRCode(jsonObject.toString());
		} catch (WriterException e) {
			e.printStackTrace();
		}
		qrcodeIMG.setImageBitmap(bitmap);
	}
	
	/** ����������룬������
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
