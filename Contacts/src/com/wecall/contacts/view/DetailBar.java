package com.wecall.contacts.view;

import com.wecall.contacts.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * �Զ��������ϸ��Ϣtab
 * 
 * @author xiaoxin
 *
 */
public class DetailBar extends RelativeLayout {

	//�ڲ��Ŀؼ�
	private Button leftBtn,rightBtn;
	private TextView tvInfo;
	
	//�ڲ��ؼ�����Ϣ
	private String leftText;
	private int leftTextColor;
	private float leftTextSize;
	private Drawable leftBackground;
	
	private String rightText;
	private int rightTextColor;
	private float rightTextSize;
	private Drawable rightBackground;
	
	private String infoText;
	private int infoTextColor;
	private float infoTextSize;
	private Drawable infoBackground;
	
	private LayoutParams leftParams,rightParams,infoParams;
	
	//����ӿ�
	public interface DetailBarClickListener{
		void leftClick();
		void rightClick();
		void infoClick();
	}
	
	private DetailBarClickListener listener;
	
	//���Ÿ��ⲿ�ķ���
	public void setOnDetailBarClickListener(DetailBarClickListener listener){
		this.listener = listener;
	}
	
	//ʵ�����������Ĺ��췽�������еڶ�������Ϊ����Ĳ�������
	public DetailBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		//��ʼ���Һ�
		initAttrs(context,attrs);
		//��ʼ���ڲ��ؼ�
		initView(context);
		
	}
	
	public String getInfo(){
		return tvInfo.getText().toString();
	}

	private void initAttrs(Context context, AttributeSet attrs) {
		
		TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.Detailbar);
		
		leftText = ta.getString(R.styleable.Detailbar_leftText);
		leftTextColor = ta.getColor(R.styleable.Detailbar_leftTextColor, 0);
		leftTextSize = ta.getDimension(R.styleable.Detailbar_leftTextSize, 0);
		leftBackground = ta.getDrawable(R.styleable.Detailbar_leftBackground);
		
		rightText = ta.getString(R.styleable.Detailbar_rightText);
		rightTextColor = ta.getColor(R.styleable.Detailbar_rightTextColor, 0);
		rightTextSize = ta.getDimension(R.styleable.Detailbar_rightTextSize, 0);
		rightBackground = ta.getDrawable(R.styleable.Detailbar_rightBackground);
		
		infoText = ta.getString(R.styleable.Detailbar_infoText);
		infoTextColor = ta.getColor(R.styleable.Detailbar_infoTextColor, 0);
		infoTextSize = ta.getDimension(R.styleable.Detailbar_infoTextSize, 0);
		infoBackground = ta.getDrawable(R.styleable.Detailbar_infoBackground);
		
		ta.recycle();
	}

	private void initView(Context context) {
		leftBtn = new Button(context);
		rightBtn = new Button(context);
		tvInfo = new TextView(context);
		
		leftBtn.setText(leftText);
		leftBtn.setTextColor(leftTextColor);
		leftBtn.setTextSize(leftTextSize);
		leftBtn.setBackground(leftBackground);
		
		rightBtn.setText(rightText);
		rightBtn.setTextColor(rightTextColor);
		rightBtn.setTextSize(rightTextSize);
		rightBtn.setBackground(rightBackground);
		
		tvInfo.setText(infoText);
		tvInfo.setTextColor(infoTextColor);
		tvInfo.setTextSize(infoTextSize);
		tvInfo.setBackground(infoBackground);
		tvInfo.setGravity(Gravity.CENTER);
		
		setBackgroundColor(0xfff59563);
		
		leftParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,TRUE);
		leftParams.addRule(RelativeLayout.CENTER_VERTICAL,TRUE);
		addView(leftBtn,leftParams);
		
		rightParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		rightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,TRUE);
		rightParams.addRule(RelativeLayout.CENTER_VERTICAL,TRUE);
		addView(rightBtn,rightParams);
		
		infoParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
		infoParams.addRule(RelativeLayout.CENTER_IN_PARENT,TRUE);
		addView(tvInfo,infoParams);
		
		leftBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				listener.leftClick();
			}
		});
		
		rightBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				listener.rightClick();
			}
		});
		
		tvInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				listener.infoClick();
			}
		});
	}
}
