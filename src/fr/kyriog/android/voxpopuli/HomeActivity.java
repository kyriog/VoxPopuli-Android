package fr.kyriog.android.voxpopuli;

import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;

public class HomeActivity extends Activity {
	public final static String VP_DATA_USERNAME = "username";
	public final static String VP_DATA_GAME = "game";
	public final static String VP_DATA_USER_ID = "userId";
	public final static String VP_DATA_USER_SESSION = "userSession";

	private final static int LOGIN_REQUEST_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(this, LoginActivity.class);
		startActivityForResult(intent, LOGIN_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			setContentView(R.layout.activity_home);
			TextView username = (TextView) findViewById(R.id.home_username);
			username.setText(getResources().getString(R.string.home_username, data.getStringExtra(VP_DATA_USERNAME)));
		} else
			finish();
	}
}
