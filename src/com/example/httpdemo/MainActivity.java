package com.example.httpdemo;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{

	private TextView mResult;
	
	private Button mNativeGetBtn;
	private Button mNativeGetWithQueryBtn;
	private Button mNativePostBtn;
	
	private String result;
	private String url = "http://www.baidu.com/";
	private String search_url = "http://www.baidu.com/s"; //?wd=android
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mResult = (TextView) findViewById(R.id.result);
		mNativeGetBtn = (Button) findViewById(R.id.native_get_btn);
		mNativeGetWithQueryBtn = (Button) findViewById(R.id.native_get_btn_with_query);
		mNativePostBtn = (Button) findViewById(R.id.native_post_btn);
		mNativeGetBtn.setOnClickListener(this);
		mNativeGetWithQueryBtn.setOnClickListener(this);
		mNativePostBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.native_get_btn:
			new Thread(){
				public void run() {
					result = NativeHttpUtil.get(url);
					mHandler.sendEmptyMessage(0);
				};
			}.start();
			break;
		case R.id.native_get_btn_with_query:
			new Thread(){
				public void run() {
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("wd", "android");
					result = NativeHttpUtil.get(search_url, params);
					mHandler.sendEmptyMessage(1);
				};
			}.start();
			break;
		case R.id.native_post_btn:
			new Thread(){
				public void run() {
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("wd", "android");
					result = NativeHttpUtil.post(search_url, params);
					mHandler.sendEmptyMessage(1);
				};
			}.start();
			break;

		default:
			break;
		}
	}
	
	private Handler mHandler = new Handler() { 
		public void handleMessage(Message msg) {
			if (TextUtils.isEmpty(result)) {
				mResult.setText("请求超时");
			} else {
				mResult.setText(result);
			}
		};
	};
	
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacksAndMessages(null);
	};

}
