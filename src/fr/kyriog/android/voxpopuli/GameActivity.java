package fr.kyriog.android.voxpopuli;

import fr.kyriog.android.voxpopuli.handler.GameHandler;
import fr.kyriog.android.voxpopuli.socketio.SocketCallback;
import io.socket.SocketIO;
import java.net.MalformedURLException;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class GameActivity extends Activity {
	private SocketIO socket;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		setTitle(getResources().getString(R.string.title_activity_game, extras.getString(HomeActivity.VP_DATA_GAME)));

		setContentView(R.layout.loading);
		TextView text = (TextView) findViewById(R.id.loading_text);
		text.setText(R.string.connecting);

		try {
			StringBuilder header = new StringBuilder();
			header.append("user_id=" + extras.getString(HomeActivity.VP_DATA_USER_ID));
			header.append("&user_session=" + extras.getString(HomeActivity.VP_DATA_USER_SESSION));
			header.append("&page=game");
			header.append("&room=" + extras.getString(HomeActivity.VP_DATA_GAME));
			socket = new SocketIO("http://ks.richie.fr:443/lldpgn", header.toString());
			GameHandler handler = new GameHandler(this);
			socket.connect(new SocketCallback(handler));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStop() {
		if(isFinishing())
			socket.disconnect();

		super.onStop();
	}
}
