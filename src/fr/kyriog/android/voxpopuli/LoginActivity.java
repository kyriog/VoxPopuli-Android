package fr.kyriog.android.voxpopuli;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

public class LoginActivity extends Activity {
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		CookieManager.getInstance().removeAllCookie();
		WebView web = (WebView) findViewById(R.id.login_webview);
		WebSettings settings = web.getSettings();
		settings.setJavaScriptEnabled(true);
		web.addJavascriptInterface(new JS(), "androidjs");
		web.setWebViewClient(new Client());
		web.loadUrl("http://vox-populi.richie.fr/login");
	}

	private class Client extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if("http://vox-populi.richie.fr/".equals(url))
				setContentView(R.layout.loading);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			if("http://vox-populi.richie.fr/".equals(url))
				view.loadUrl("http://vox-populi.richie.fr/getuserjson");
			else if("http://vox-populi.richie.fr/getuserjson".equals(url)) {
				view.loadUrl("javascript:window.androidjs.getHtml(document.getElementsByTagName('body')[0].innerHTML);");
			}
		}
	}

	private class JS {
		@JavascriptInterface
		public void getHtml(String rawHtml) {
			try {
				JSONObject data = new JSONObject(rawHtml);
				String userId = data.getString("user_id");
				String userSession = data.getString("user_session");

				Intent intent = new Intent();
				intent.putExtra(HomeActivity.VP_DATA_USER_ID, userId);
				intent.putExtra(HomeActivity.VP_DATA_USER_SESSION, userSession);

				setResult(Activity.RESULT_OK, intent);
				finish();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
