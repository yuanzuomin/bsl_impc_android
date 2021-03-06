package com.foreveross.chameleon.pad.component;

import org.apache.cordova.CordovaChromeClient;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.IceCreamCordovaWebViewClient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.csair.impc.R;

public class WebViewFragment extends Fragment {

	private CordovaWebView cordovaWebView;
	private ClosableWindow closableWindow;

	public WebViewFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		closableWindow = initWindow();
		((ViewGroup) closableWindow.getParent()).removeView(closableWindow);
		return closableWindow;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// System.out.println(initUlr);

	}

	private String initUlr = null;

	public void initUrl(String url) {
		initUlr = url;
	}

	public void loadUrl(String url, int time) {
		cordovaWebView.loadUrl(url, time);
		onNewIntent(intent);
		cordovaWebView.handleResume(true, true);
		cordovaWebView.setVisibility(View.VISIBLE);

	}

	@Override
	public void onResume() {
		super.onResume();
		cordovaWebView.loadUrl(initUlr, 6000);
		cordovaWebView.setVisibility(View.VISIBLE);

	}

	public void loadUrl(String url) {
		cordovaWebView.loadUrl(url, 100);
		cordovaWebView.setVisibility(View.VISIBLE);
	}

	private ClosableWindow initWindow() {
		ClosableWindow closableWindow = new ClosableWindow(getAssocActivity(),
				getAssocActivity().findViewById(R.id.child_frame));
		closableWindow.addContentView(cordovaWebView = initWebView());
		closableWindow.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return closableWindow;
	}

	@SuppressLint("NewApi")
	private CordovaWebView initWebView() {

		CordovaWebView webView = new CordovaWebView(getAssocActivity());
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setDisplayZoomControls(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setAppCacheEnabled(true);
		webView.getSettings().setJavaScriptEnabled(true);
		CordovaWebViewClient webViewClient = null;
		if (Build.VERSION.SDK_INT < 11) {
			webViewClient = new CordovaWebViewClient(getFragmentActivity(),
					webView);
		} else {
			webViewClient = new IceCreamCordovaWebViewClient(
					getFragmentActivity(), webView);
		}
		webView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		webView.setWebViewClient(webViewClient);
		webView.setWebChromeClient(new CordovaChromeClient(
				getFragmentActivity(), webView));
		return webView;
	}

	public CordovaInterface getFragmentActivity() {
		return (CordovaInterface) getAssocActivity();
	}

	private Intent intent;

	public void initIntent(Intent intent) {
		this.intent = intent;
	}

	public void onNewIntent(Intent intent) {
		cordovaWebView.onNewIntent(intent);
	}
	
	public void sendJavascript(String js){
		cordovaWebView.sendJavascript(js);
	}
}
