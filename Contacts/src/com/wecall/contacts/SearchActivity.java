package com.wecall.contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.wecall.contacts.adapter.SearchAdapter;
import com.wecall.contacts.database.DatabaseManager;
import com.wecall.contacts.entity.ContactItem;

/**
 * ����ҳ
 * 
 * @author xiaoxin 2015-4-16
 */
public class SearchActivity extends Activity {

	private static final String TAG = "SearchActivity";
	private static final int INFO_REQUEST_CODE = 1;
	private static final int EDIT_REQUEST_CODE = 2;

	private SearchView mSearchView;
	private TextView mResultText;
	private ListView mContactListView;
	private SearchAdapter adapter;
	private DatabaseManager mManager;

	// ��ϵ����Ϣ
	private List<ContactItem> contactList = new ArrayList<ContactItem>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		init();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * ��ʼ�����ݺͿؼ�
	 */
	@SuppressWarnings("unchecked")
	private void init() {
		mSearchView = (SearchView) findViewById(R.id.sv_search);
		mResultText = (TextView) findViewById(R.id.tv_result_search);
		mContactListView = (ListView) findViewById(R.id.lv_search_contact_list);
		mManager = new DatabaseManager(this);
		contactList = new ArrayList<ContactItem>();
		Collections.sort(contactList);
		adapter = new SearchAdapter(this, null, null);
		mContactListView.setAdapter(adapter);

		mContactListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(SearchActivity.this,
						ContactInfo.class);
				intent.putExtra("cid",
						((ContactItem) adapter.getItem(arg2)).getId());
				startActivityForResult(intent, INFO_REQUEST_CODE);
			}
		});

		mContactListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {
						showOperationDialog(arg2);
						return false;
					}
				});

		mSearchView.setQueryHint("������" + contactList.size() + "λ��ϵ��");
		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String arg0) {
				filterData(arg0);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String arg0) {
				filterData(arg0);
				return false;
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INFO_REQUEST_CODE) {
			updateContacts();
		}
		if (requestCode == EDIT_REQUEST_CODE) {
			updateContacts();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * ����������е�ֵ���������ݲ�����ListView �ɸ���ƴ�������֣���д������
	 * 
	 * @param filterStr
	 */
	@SuppressWarnings("unchecked")
	@SuppressLint("DefaultLocale")
	private void filterData(String filterStr) {
		contactList.clear();
		contactList = (List<ContactItem>) mManager.ftsSearch(filterStr).get(0);
		Log.v(TAG, "contactList:"+contactList.toString());
		List<Map<String, Integer>> mapList = new ArrayList<Map<String, Integer>>();
		if (!TextUtils.isEmpty(filterStr)) {
			for (ContactItem contactItem : contactList) {
//				String convertStr = filterStr.toLowerCase();
				Map<String, Integer> map = contactItem.contains(filterStr);
				// Map<String, Integer> convertMap = contactItem
				// .contains(convertStr);
				if (map != null && map.size() != 0) {
					mapList.add(map);
				}
			}
		}
		mResultText.setText("������" + contactList.size() + "λ��ϵ��");
		adapter.updateListView(contactList, mapList, filterStr.length());
	}

	protected void showOperationDialog(final int position) {
		new AlertDialog.Builder(this)
				.setTitle(((ContactItem) adapter.getItem(position)).getName())
				.setPositiveButton("�༭", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.v(TAG, "edit");
						dialog.dismiss();
						Intent intent = new Intent(SearchActivity.this,
								ContactEditor.class);
						Bundle bundle = new Bundle();
						bundle.putInt("cid", ((ContactItem) adapter
								.getItem(position)).getId());
						bundle.putInt("type", 2);
						intent.putExtras(bundle);
						startActivityForResult(intent, EDIT_REQUEST_CODE);
					}

				})
				.setNegativeButton("ɾ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.v(TAG, "delete");
						dialog.dismiss();
						showDeleteDialog(position);
					}
				}).show();
	}

	private void showDeleteDialog(final int position) {
		new AlertDialog.Builder(this)
				.setTitle("�Ƿ�ȷ��ɾ����")
				.setPositiveButton("��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// �����ݿ���ɾ������¼
						mManager.deleteContactById(((ContactItem) adapter
								.getItem(position)).getId());
						// ɾ��֮���ȡ���µ���ϵ����Ϣ
						updateContacts();
						Toast.makeText(SearchActivity.this, "��ϵ��ɾ���ɹ�",
								Toast.LENGTH_SHORT).show();
					}
				})
				.setNegativeButton("��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	@SuppressWarnings("unchecked")
	private void updateContacts() {
		contactList = (List<ContactItem>) mManager.ftsSearch(
				mSearchView.getQuery().toString()).get(0);
		Collections.sort(contactList);
		filterData(mSearchView.getQuery().toString());
	}
}
