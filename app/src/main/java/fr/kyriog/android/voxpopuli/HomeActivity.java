package fr.kyriog.android.voxpopuli;

import fr.kyriog.android.voxpopuli.adapter.HomeAdapter;
import fr.kyriog.android.voxpopuli.entity.Game;
import fr.kyriog.android.voxpopuli.handler.HomeHandler;
import fr.kyriog.android.voxpopuli.socketio.BaseCallback;
import fr.kyriog.android.voxpopuli.socketio.HomeCallback;
import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class HomeActivity extends Activity {
	public final static String VP_DATA_USERNAME = "username";
	public final static String VP_DATA_GAME = "game";
	public final static String VP_DATA_USER_ID = "userId";
	public final static String VP_DATA_USER_SESSION = "userSession";

	private final static int LOGIN_REQUEST_CODE = 1;
	private final static String SIS_GAMES = "games";

	private static Socket socket;
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
		else
			launchLogin();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(SIS_GAMES, games);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			games.clear();
			Bundle userData = data.getExtras();

			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(VP_DATA_USER_ID, userData.getString(VP_DATA_USER_ID));
			editor.putString(VP_DATA_USER_SESSION, userData.getString(VP_DATA_USER_SESSION));
			editor.commit();
			loadGames();
		} else
			finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.home_menu_logout:
			SharedPreferences.Editor editor = prefs.edit();
			editor.remove(VP_DATA_USER_ID);
			editor.remove(VP_DATA_USER_SESSION);
			editor.commit();
			launchLogin();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void loadGames() {
		if(callback == null) {
			HomeHandler handler = new HomeHandler(games, adapter);
			callback = new HomeCallback(handler);
		}
		if(socket == null || !socket.connected()) {
			StringBuilder header = new StringBuilder();
			header.append("user_id=" + prefs.getString(HomeActivity.VP_DATA_USER_ID, ""));
			header.append("&user_session=" + prefs.getString(HomeActivity.VP_DATA_USER_SESSION, ""));
			header.append("&page=index");
			try {
				IO.Options opts = new IO.Options();
				opts.query = header.toString();
				opts.forceNew = true;
				socket = IO.socket("https://vox-populi.richie.fr/lldpgn", opts);
				socket.on("tickList", callback);
				socket.connect();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

		setContentView(R.layout.activity_home);

		ListView gamesView = (ListView) findViewById(R.id.home_games);
		gamesView.setAdapter(adapter);
	}

	public void launchGame(Game game) {
		Intent intent = new Intent(this, GameActivity.class);
		intent.putExtra(VP_DATA_USER_ID, prefs.getString(VP_DATA_USER_ID, ""));
		intent.putExtra(VP_DATA_USER_SESSION, prefs.getString(VP_DATA_USER_SESSION, ""));
		intent.putExtra(VP_DATA_GAME, game.getId());
		startActivity(intent);
		clearSocket();
	}

	private void launchLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivityForResult(intent, LOGIN_REQUEST_CODE);
	}

	private void clearSocket() {
		if(socket != null)
			socket.disconnect();
		socket = null;
		callback = null;
	}
}
