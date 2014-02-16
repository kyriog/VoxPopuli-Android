package fr.kyriog.android.voxpopuli;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			setContentView(R.layout.activity_home);

			TextView username = (TextView) findViewById(R.id.home_username);
			username.setText(getResources().getString(R.string.home_username, data.getStringExtra(VP_DATA_USERNAME)));

			Button joinGame = (Button) findViewById(R.id.home_join);
			joinGame.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(HomeActivity.this, GameActivity.class);
					intent.putExtras(data.getExtras());
					startActivity(intent);
				}
			});
		} else
			finish();
	}
}