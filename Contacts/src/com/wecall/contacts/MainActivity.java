package com.wecall.contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.wecall.contacts.adapter.SortAdapter;
import com.wecall.contacts.entity.ContactItem;
import com.wecall.contacts.util.PinYin;
import com.wecall.contacts.view.ClearableEditText;
import com.wecall.contacts.view.SideBar;
import com.wecall.contacts.view.SideBar.onTouchLetterChangeListener;

public class MainActivity extends Activity {

	// ��ϵ���б�ؼ�
	private ListView contactListView;
	// ����������ؼ�
	private SideBar sideBar;
	// ��ɾ��������ؼ�
	private ClearableEditText inputEditText;
	// ��ʾ��ǰѡ�е���ĸ�������ı��ؼ�
	private TextView showAheadTV;
	// �����������
	private SortAdapter adapter;
	// ��ϵ����Ϣ
	private List<ContactItem> contactList;

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
				bundle.putString("cname",
						((ContactItem) adapter.getItem(arg2)).getName());
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});

		// ��ȡ��ϵ����Ϣ
		// TODO: use SQLite after
		contactList = filledData(getResources().getStringArray(R.array.date));

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
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	private List<ContactItem> filledData(String[] data) {
		List<ContactItem> mSortList = new ArrayList<ContactItem>();

		for (int i = 0; i < data.length; i++) {
			ContactItem contactItem = new ContactItem();
			contactItem.setName(data[i]);
			String sortString = contactItem.getFullPinyin().substring(0, 1)
					.toUpperCase();

			// ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ
			if (sortString.matches("[A-Z]")) {
				contactItem.setSortLetter(sortString.toUpperCase());
			} else {
				contactItem.setSortLetter("#");
			}

			mSortList.add(contactItem);
		}
		return mSortList;

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

}
