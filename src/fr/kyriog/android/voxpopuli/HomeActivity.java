package fr.kyriog.android.voxpopuli;

import fr.kyriog.android.voxpopuli.adapter.HomeAdapter;
import fr.kyriog.android.voxpopuli.entity.Game;
import fr.kyriog.android.voxpopuli.handler.HomeHandler;
import fr.kyriog.android.voxpopuli.socketio.BaseCallback;
import fr.kyriog.android.voxpopuli.socketio.HomeCallback;
import io.socket.SocketIO;

import java.net.MalformedURLException;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class HomeActivity extends Activity {
	public final static String VP_DATA_USERNAME = "username";
	public final static String VP_DATA_GAME = "game";
	public final static String VP_DATA_USER_ID = "userId";
	public final static String VP_DATA_USER_SESSION = "userSession";

	private final static int LOGIN_REQUEST_CODE = 1;
	private final static String SIS_GAMES = "games";

	private static SocketIO socket;
	private static BaseCallback callback;
	private BaseAdapter adapter;

	private SharedPreferences prefs;
	private ArrayList<Game> games = new ArrayList<Game>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null)
			games = savedInstanceState.getParcelableArrayList(SIS_GAMES);
	}

	@Override
	protected void onStart() {
		super.onStart();
		adapter = new HomeAdapter(this, games);
		if(callback != null)
			callback.setHandler(new HomeHandler(games, adapter));
		prefs = getPreferences(MODE_PRIVATE);
		if(prefs.contains(VP_DATA_USER_ID) && prefs.contains(VP_DATA_USER_SESSION))
			loadGames();
		else {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivityForResult(intent, LOGIN_REQUEST_CODE);
		}
	}

	@Override
	protected void onDestroy() {
		if(isFinishing())
			clearSocket();
		super.onDestroy();
	}

	@Override
	protected void onUserLeaveHint() {
		clearSocket();
		super.onUserLeaveHint();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(SIS_GAMES, games);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			Bundle userData = data.getExtras();

			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(VP_DATA_USER_ID, userData.getInt(VP_DATA_USER_ID));
			editor.putString(VP_DATA_USER_SESSION, userData.getString(VP_DATA_USER_SESSION));
			editor.commit();
			loadGames();
		} else
			finish();
	}

	private void loadGames() {
		if(socket == null) {
			StringBuilder header = new StringBuilder();
			header.append("user_id=" + prefs.getInt(HomeActivity.VP_DATA_USER_ID, 0));
			header.append("&user_session=" + prefs.getString(HomeActivity.VP_DATA_USER_SESSION, ""));
			header.append("&page=index");
			try {
				socket = new SocketIO("http://ks.richie.fr:443/lldpgn", header.toString());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		if(callback == null)
			callback = new HomeCallback(new HomeHandler(games, adapter));
		if(!socket.isConnected())
			socket.connect(callback);

		setContentView(R.layout.activity_home);

		ListView gamesView = (ListView) findViewById(R.id.home_games);
		gamesView.setAdapter(adapter);
	}

	public void launchGame(Game game) {
		Intent intent = new Intent(this, GameActivity.class);
		Log.i("userid", String.valueOf(prefs.getInt(VP_DATA_USER_ID, 0)));
		intent.putExtra(VP_DATA_USER_ID, prefs.getInt(VP_DATA_USER_ID, 0));
		Log.i("usersession", prefs.getString(VP_DATA_USER_SESSION, "nothing"));
		intent.putExtra(VP_DATA_USER_SESSION, prefs.getString(VP_DATA_USER_SESSION, ""));
		intent.putExtra(VP_DATA_GAME, game.getId());
		startActivity(intent);
		clearSocket();
	}

	private void clearSocket() {
		if(socket != null)
			socket.disconnect();
		socket = null;
		callback = null;
	}
}
