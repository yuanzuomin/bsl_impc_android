/**
 * 
 */
package com.foreveross.chameleon.push.mina.library.service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.csair.impc.R;
import com.foreveross.chameleon.TmpConstants;
import com.foreveross.chameleon.event.EventBus;
import com.foreveross.chameleon.push.mina.library.protocol.PushProtocol.Auth_Rsp;
import com.foreveross.chameleon.push.mina.library.protocol.PushProtocol.MessageContent;
import com.squareup.otto.Subscribe;

/**
 * [一句话功能简述]<BR>
 * [功能详细描述]
 * 
 * @author 冯伟立
 * @version [mina_android_lib, 2013-7-8]
 */
public class TestActivity extends Activity {

	/**
	 * [一句话功能简述]<BR>
	 * [功能详细描述]
	 * 
	 * @param savedInstanceState
	 *            2013-7-8 下午4:05:55
	 */
	private EditText editText = null;
	private Button button = null;
	private AtomicInteger atomicInteger = new AtomicInteger(0);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editText = (EditText) this.findViewById(R.id.editText1);
		button = (Button) this.findViewById(R.id.button1);
		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).register(this);
		this.startService(new Intent(this, MinaPushService.class));

	}

	StringBuffer stringBuffer = new StringBuffer();

	@Subscribe
	public void onMessage(final MessageContent messageContent) {
		atomicInteger.incrementAndGet();
		stringBuffer.append(messageContent.getId()).append("_")
				.append(messageContent.getTitle()).append("_")
				.append(messageContent.getContent());
	}

	@Subscribe
	public void onMessage(final String msg) {
		editText.post(new Runnable() {

			@Override
			public void run() {
				editText.append(msg);
			}
		});

	}

	/**
	 * [一句话功能简述]<BR>
	 * [功能详细描述] 2013-7-11 下午4:49:53
	 */
	@Override
	protected void onResume() {
		super.onResume();
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				editText.post(new Runnable() {

					@Override
					public void run() {
						button.setText("收到" + atomicInteger.get()
								+ "条信息！");
						editText.setText(stringBuffer.toString());
					}
				});

			}
		}, 0, 1000);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).unregister(this);
	}

	@Subscribe
	public void onSesisonOpened(final Auth_Rsp auth_Rsp) {
		editText.post(new Runnable() {

			@Override
			public void run() {
				editText.append(auth_Rsp.getSessionId() + "成功连接....");
			}
		});

	}

}
