package com.wecall.contacts.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.wecall.contacts.LabelInfo;
import com.wecall.contacts.R;
import com.wecall.contacts.SelectLabelMember;

/**
 * ��ǩҳfragment
 * @author xiaoxin
 *	2015-4-11
 */
public class LabelFragment extends Fragment {

	private static final String TAG = "LabelFragment";
	private ListView lableListView;
	private ImageButton addImageButton;
	private ArrayAdapter<String> adapter;
	private List<String> list = new ArrayList<String>();
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.label_fragment, container, false);
		findView(view);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		initData();
		adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, list);
		lableListView.setAdapter(adapter);
		super.onActivityCreated(savedInstanceState);
	}
	private void initData() {
		list.clear();
		list.add("����");
		list.add("ʲô��");
		list.add("�׶�԰ͬ��");
		list.add("��������");
		list.add("����ҧ��ѽ��");
		list.add("��������ս");
		list.add("Сè��");
		list.add("һֱ��������");
		list.add("΢Ѷ�Ŷ�");
		list.add("����");
		list.add("ʲô��");
		list.add("�׶�԰ͬ��");
		list.add("��������");
		list.add("����ҧ��ѽ��");
		list.add("��������ս");
		list.add("Сè��");
		list.add("һֱ��������");
		list.add("΢Ѷ�Ŷ�");
	}

	private void findView(View view) {
		lableListView = (ListView) view.findViewById(R.id.lv_label);
		addImageButton = (ImageButton)view.findViewById(R.id.ibtn_label_add);
		
		lableListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(getActivity(),LabelInfo.class);
				intent.putExtra("label", list.get(arg2));
				startActivity(intent);
			}
		});
		
		addImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Log.v(TAG, "AddBtnClick");
				Intent intent = new Intent(getActivity(),SelectLabelMember.class);
				startActivity(intent);
			}
		});
	}
}
