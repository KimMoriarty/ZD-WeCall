package com.wecall.contacts.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.wecall.contacts.ContactInfo;
import com.wecall.contacts.R;
import com.wecall.contacts.adapter.SortAdapter;
import com.wecall.contacts.database.DatabaseManager;
import com.wecall.contacts.entity.ContactItem;
import com.wecall.contacts.util.PinYin;
import com.wecall.contacts.view.SideBar;
import com.wecall.contacts.view.SideBar.onTouchLetterChangeListener;

/**
 * ��Fragment����ʾ��ϵ���б�
 * 
 * @author xiaoxin 2015-4-10
 */
public class MainFragment extends Fragment {

	private static String TAG = "MainFragment";
	private static final int INFO_REQUEST_CODE = 4;
	private ListView contactListView;
	private TextView letterTextView;
	// ����������ؼ�
	private SideBar sideBar;
	// �����������
	private SortAdapter adapter;
	// ��ϵ����Ϣ
	private List<ContactItem> contactList = new ArrayList<ContactItem>();
	// ���ݿ����ʵ��
	private DatabaseManager mManager;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Log.v(TAG, "mainFragment");
		View view = inflater.inflate(R.layout.main_fragment, container, false);
		findView(view);
		return view;
	}

	/**
	 * ��ʼ���ؼ�
	 * 
	 * @param view
	 */
	private void findView(View view) {
		contactListView = (ListView) view.findViewById(R.id.lv_contacts);
		letterTextView = (TextView) view.findViewById(R.id.tv_show_ahead);
		sideBar = (SideBar) view.findViewById(R.id.sidebar);

	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setListener();
		sideBar.setLetterShow(letterTextView);
		adapter = new SortAdapter(contactList, getActivity());
		mManager = new DatabaseManager(getActivity());
		
		contactListView.setAdapter(adapter);

		// ��ȡ��ϵ����Ϣ
		updateContacts();
		
	}

	@SuppressWarnings("unchecked")
	public void updateContacts() {
		contactList.clear();
		contactList = mManager.queryAllContact();
		Collections.sort(contactList);
		adapter.updateListView(contactList);
	}

	/**
	 * ���ü����¼�
	 */
	private void setListener() {
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
				Log.v(TAG, "position:" + arg2);
				Intent intent = new Intent(getActivity(),ContactInfo.class);
				Bundle bundle = new Bundle();
				bundle.putInt("cid",
						((ContactItem) adapter.getItem(arg2)).getId());
				intent.putExtras(bundle);
				startActivityForResult(intent, INFO_REQUEST_CODE);
			}
		});
	}
	
	/**
	 * ȡ����ϵ�˵���Ŀ
	 * @return
	 */
	public int getContactAmount(){
		return contactList.size();
	}
	
	/**
	 * ����������е�ֵ���������ݲ�����ListView �ɸ���ƴ�������֣���д������
	 * 
	 * @param filterStr
	 */
	public void filterData(String filterStr) {
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
