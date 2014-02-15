package fr.kyriog.android.voxpopuli;

import android.os.Bundle;
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
				view.loadUrl("javascript:window.androidjs.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
		}
	}

	private class JS {
		@JavascriptInterface
		public void getHtml(String rawHtml) {
			String[] html = rawHtml.split("\n");
			String username = html[2].split("@", 2)[1].split("\\.", 2)[0];
			String game = html[5].split("/game/", 2)[1].split("\"", 2)[0];
			String userId = html[12].split("user_id=", 2)[1].split("&", 2)[0];
			String userSession = html[12].split("user_session=", 2)[1].split("&", 2)[0];

			Intent data = new Intent();
			data.putExtra(HomeActivity.VP_DATA_USERNAME, username);
			data.putExtra(HomeActivity.VP_DATA_GAME, game);
			data.putExtra(HomeActivity.VP_DATA_USER_ID, userId);
			data.putExtra(HomeActivity.VP_DATA_USER_SESSION, userSession);

			setResult(Activity.RESULT_OK, data);
			finish();
		}
	}
}
