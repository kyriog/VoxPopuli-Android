package fr.kyriog.android.voxpopuli;

import fr.kyriog.android.voxpopuli.adapter.PlayerAdapter;
import fr.kyriog.android.voxpopuli.entity.Player;
import fr.kyriog.android.voxpopuli.handler.GameHandler;
import fr.kyriog.android.voxpopuli.socketio.BaseCallback;
import fr.kyriog.android.voxpopuli.socketio.GameCallback;
import io.socket.SocketIO;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class GameActivity extends Activity {
	private final static int GAMESTATUS_WAITING = 1;
	private final static int GAMESTATUS_VOTING = 2;
	private final static int GAMESTATUS_VOTED = 3;
	private final static int GAMESTATUS_RESULTS = 4;
	private final static int GAMESTATUS_ENDED = 5;
	private final static String GAMESTATUS = "gameStatus";
	private final static String GAMESTATUS_WAITING_PLAYERS = "waitingPlayers";
	private final static String GAMESTATUS_WAITING_NBPLAYERS = "waitingNbPlayers";
	private final static String GAMESTATUS_WAITING_NBMINPLAYERS = "waitingNbMinPlayers";
	private final static String GAMESTATUS_WAITING_NBMAXPLAYERS = "waitingNbMaxPlayers";

	private static SocketIO socket;
	private static BaseCallback callback;

	private int gameStatus = 0;
	private final ArrayList<Player> players = new ArrayList<Player>();
	private int nbPlayers;
	private int nbMinPlayers;
	private int nbMaxPlayers;
	private BaseAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		if(socket == null || !socket.isConnected()) {
			try {
				StringBuilder header = new StringBuilder();
				header.append("user_id=" + extras.getString(HomeActivity.VP_DATA_USER_ID));
				header.append("&user_session=" + extras.getString(HomeActivity.VP_DATA_USER_SESSION));
				header.append("&page=game");
				header.append("&room=" + extras.getString(HomeActivity.VP_DATA_GAME));
				socket = new SocketIO("http://ks.richie.fr:443/lldpgn", header.toString());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		GameHandler handler = new GameHandler(this, socket);
		if(!socket.isConnected()) {
			callback = new GameCallback(handler);
			socket.connect(callback);
		} else
			callback.setHandler(handler);

		if(savedInstanceState != null)
			gameStatus = savedInstanceState.getInt(GAMESTATUS);

		setTitle(getResources().getString(R.string.title_activity_game, extras.getString(HomeActivity.VP_DATA_GAME)));
		switch(gameStatus) {
		case 0:
			setContentView(R.layout.loading);
			TextView text = (TextView) findViewById(R.id.loading_text);
			text.setText(R.string.connecting);
			break;
		case GAMESTATUS_WAITING:
			List<Player> players = savedInstanceState.getParcelableArrayList(GAMESTATUS_WAITING_PLAYERS);
			int nbPlayers = savedInstanceState.getInt(GAMESTATUS_WAITING_NBPLAYERS);
			int nbMinPlayers = savedInstanceState.getInt(GAMESTATUS_WAITING_NBMINPLAYERS);
			int nbMaxPlayers = savedInstanceState.getInt(GAMESTATUS_WAITING_NBMAXPLAYERS);
			onWaiting(players, nbPlayers, nbMinPlayers, nbMaxPlayers);
			break;
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(GAMESTATUS, gameStatus);
		switch(gameStatus) {
		case GAMESTATUS_WAITING:
			outState.putParcelableArrayList(GAMESTATUS_WAITING_PLAYERS, players);
			outState.putInt(GAMESTATUS_WAITING_NBPLAYERS, nbPlayers);
			outState.putInt(GAMESTATUS_WAITING_NBMINPLAYERS, nbMinPlayers);
			outState.putInt(GAMESTATUS_WAITING_NBMAXPLAYERS, nbMaxPlayers);
			break;
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStop() {
		if(isFinishing()) {
			socket.disconnect();
			socket = null;
		}

		super.onStop();
	}

	public void updatePlayerCounter() {
		TextView counter = (TextView) findViewById(R.id.game_waiting_counter);
		String counterText = getResources().getString(R.string.game_waiting_counter,
				nbPlayers,
				nbMinPlayers,
				nbMaxPlayers);
		counter.setText(counterText);
	}

	public void onWaiting(List<Player> players, int nbPlayers, int nbMinPlayers, int nbMaxPlayers) {
		gameStatus = GAMESTATUS_WAITING;

		setContentView(R.layout.activity_game_waiting);

		this.players.clear();
		this.players.addAll(players);
		if(adapter == null) {
			adapter = new PlayerAdapter(this, this.players);
			GridView grid = (GridView) findViewById(R.id.game_waiting_players);
			grid.setAdapter(adapter);
		}
		adapter.notifyDataSetChanged();

		this.nbPlayers = nbPlayers;
		this.nbMinPlayers = nbMinPlayers;
		this.nbMaxPlayers = nbMaxPlayers;
		updatePlayerCounter();
	}

	public void onAddPlayer(Player player) {
		players.add(player);
		nbPlayers++;
		adapter.notifyDataSetChanged();
		updatePlayerCounter();
	}

	public void onRemovePlayer(String playerId) {
		List<Player> forPlayers = new ArrayList<Player>(players);
		for(Player player : forPlayers) {
			if(playerId.equals(player.getId())) {
				players.remove(player);
				nbPlayers--;
				adapter.notifyDataSetChanged();
				updatePlayerCounter();
				return;
			}
		}
	}
}
