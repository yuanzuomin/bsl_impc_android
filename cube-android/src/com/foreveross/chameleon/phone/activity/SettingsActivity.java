package com.foreveross.chameleon.phone.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.csair.impc.R;
import com.foreveross.chameleon.TmpConstants;
import com.foreveross.chameleon.device.DeviceRegisteTask;
import com.foreveross.chameleon.phone.modules.CubeApplication;
import com.foreveross.chameleon.phone.modules.CubeModelItemView;
import com.foreveross.chameleon.phone.modules.CubeModule;
import com.foreveross.chameleon.phone.modules.CubeModuleManager;
import com.foreveross.chameleon.update.CheckUpdateTask;
import com.foreveross.chameleon.update.ManualCheckUpdateListener;
import com.foreveross.chameleon.util.FileCopeTool;
import com.foreveross.chameleon.util.PadUtils;

public class SettingsActivity extends BaseActivity {
	private Button titlebar_left;
	private Button titlebar_right;
	private TextView titlebar_content;
	private RelativeLayout setting_about;
	private RelativeLayout setting_update;
	private RelativeLayout setting_pushsetting;
	private RelativeLayout setting_btn_register;
	private Button logOff;
	private LinearLayout module_setting_layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		PadUtils.setSceenSize(this);
		initValue();
	}

	private void initValue() {
		titlebar_left = (Button) findViewById(R.id.title_barleft);
		titlebar_left.setOnClickListener(listener);
		titlebar_right = (Button) findViewById(R.id.title_barright);
		titlebar_right.setVisibility(View.GONE);
		titlebar_content = (TextView) findViewById(R.id.title_barcontent);
		titlebar_content.setText("设置");

		setting_about = (RelativeLayout) findViewById(R.id.setting_btn_about);
		setting_update = (RelativeLayout) findViewById(R.id.setting_btn_update);
		setting_pushsetting = (RelativeLayout) findViewById(R.id.setting_btn_pushstting);
		logOff = (Button) findViewById(R.id.logoff);
		logOff.setOnClickListener(listener);
		setting_about.setOnClickListener(listener);
		setting_update.setOnClickListener(listener);
		setting_pushsetting.setOnClickListener(listener);
		module_setting_layout = (LinearLayout) findViewById(R.id.module_setting_layout);
		// CubeApplication.getInstance(this)ACCESSIBILITY_SERVICE;
		Iterator<Entry<String, List<CubeModule>>> it = CubeModuleManager
				.getInstance().getAll_map().entrySet().iterator();
		List<CubeModule> moduleList = new ArrayList<CubeModule>();
		while (it.hasNext()) {
			moduleList.addAll(it.next().getValue());
		}
		// Set<CubeModule> modules =
		// CubeApplication.getInstance(this).getModules();
		FileCopeTool fileTool = new FileCopeTool(this);
		//将模块添加到application中如果存在就不添加了
		String results = fileTool.getFromAssets("Cube.json");
		CubeApplication app = application.getCubeApplication().buildApplication(results);
		List<CubeModule> tmpList = new ArrayList<CubeModule>();
		for (CubeModule cubeModule : moduleList) {
			for (CubeModule cube : app.getModules()) {
				if(!cubeModule.getIdentifier().equals(cube.getIdentifier()))
				{
					tmpList.add(cube);
				}
			}
		}
		moduleList.addAll(tmpList);
		Iterator<CubeModule> its = moduleList.iterator();
		List<CubeModule> modules = new ArrayList<CubeModule>();
		String path = Environment.getExternalStorageDirectory().getPath() + "/"
				+ getPackageName();
		
		while (its.hasNext()) {
			CubeModule cubeModule = its.next();
			String url = path + "/www/" + cubeModule.getIdentifier();
			if (fileTool.isfileExist(url, "settings.html")) {
				if(!isExistModule(cubeModule, modules))
				modules.add(cubeModule);
			}
		}
		if (modules.size() > 0) {
			module_setting_layout.removeAllViews();
			TextView titleView = new TextView(this);
			titleView.setText("模块设置");
			titleView.setTextColor(getResources().getColor(R.color.font_grey));
			titleView.setTextSize(13);
			module_setting_layout.addView(titleView);
			int i = 0;
			for (CubeModule cube : modules) {
				CubeModelItemView cubeItemView = new CubeModelItemView(this);
				if (modules.size() == 1) {
					cubeItemView.setBackgroundDrawable(getResources()
							.getDrawable(R.drawable.listview_select_corner));
				} else {
					if (i == 0) {
						cubeItemView.setBackgroundDrawable(getResources()
								.getDrawable(
										R.drawable.listview_select_white_top));
					} else if (i == modules.size() - 1) {
						cubeItemView
								.setBackgroundDrawable(getResources()
										.getDrawable(
												R.drawable.listview_select_white_bottom));
					} else {
						cubeItemView.setBackgroundDrawable(getResources()
								.getDrawable(
										R.drawable.listview_select_white_cell));
					}

					i++;
				}
				cubeItemView.updateView(cube);
				module_setting_layout.addView(cubeItemView);
			}
		}

	}

	protected void onResume() {
		super.onResume();
		application.setShouldSendChatNotification(true);
		application.setShouldSendNoticeNotification(true);
		application.setShouldSendMessageNotification(true);

	};

	OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.title_barleft:
				finish();
				break;
			case R.id.setting_btn_about:
				Intent i = new Intent();
				i.setClass(SettingsActivity.this, AboutActivity.class);
				startActivity(i);
				break;
			case R.id.setting_btn_update:
				if (application.getLoginType() == TmpConstants.LOGIN_OUTLINE) {
					Toast.makeText(v.getContext(), "离线登录不能使用该功能",
							Toast.LENGTH_SHORT).show();
					return;
				} else {
					new CheckUpdateTask(
							application.getCubeApplication(),
							new ManualCheckUpdateListener(SettingsActivity.this))
							.execute();
				}
				break;
			case R.id.setting_btn_pushstting:
				if (application.getLoginType() == TmpConstants.LOGIN_OUTLINE) {
					Toast.makeText(v.getContext(), "离线登录不能使用该功能",
							Toast.LENGTH_SHORT).show();
					return;
				} else {
					Intent intent = new Intent();
					intent.setClass(SettingsActivity.this,
							PushSettingActivity.class);
					startActivity(intent);
				}
				break;
			case R.id.logoff:
				Dialog dialog = new AlertDialog.Builder(SettingsActivity.this)
						.setTitle("提示")
						.setMessage("确认要注销？")
						.setNegativeButton("取消", null)
						.setPositiveButton("确认",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										application.logOff();

									}
								}).create();
				dialog.show();
				break;
			default:
				break;
			}
		}

	};
	
	private boolean isExistModule(CubeModule module,List<CubeModule> list)
	{
		if(list.isEmpty())
		{
			return false;
		}
		for (CubeModule cubeModule : list) {
			if(cubeModule.getIdentifier().equals(module.getIdentifier()))
			{
				return true;
			}
		}
		return false;
	}
}
