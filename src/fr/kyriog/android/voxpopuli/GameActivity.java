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
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GameActivity extends Activity {
	private final static int GAMESTATUS_WAITING = 1;
	private final static int GAMESTATUS_VOTING = 2;
	private final static int GAMESTATUS_VOTED = 3;
	private final static int GAMESTATUS_RESULTS = 4;
	private final static int GAMESTATUS_ENDED = 5;
	private final static String GAMESTATUS = "gameStatus";
	private final static String GAMESTATUS_TIMER = "timer";
	private final static String GAMESTATUS_MAXTIMER = "maxTimer";
	private final static String GAMESTATUS_WAITING_PLAYERS = "waitingPlayers";
	private final static String GAMESTATUS_WAITING_NBPLAYERS = "waitingNbPlayers";
	private final static String GAMESTATUS_WAITING_NBMINPLAYERS = "waitingNbMinPlayers";
	private final static String GAMESTATUS_WAITING_NBMAXPLAYERS = "waitingNbMaxPlayers";
	private final static String GAMESTATUS_VOTING_LIFECOUNT = "votingLifeCount";

	private static SocketIO socket;
	private static BaseCallback callback;

	private BaseAdapter adapter;
	private int gameStatus = 0;
	private final ArrayList<Player> players = new ArrayList<Player>();
	private int nbPlayers;
	private int nbMinPlayers;
	private int nbMaxPlayers;
	private int timer = -1;
	private int maxTimer = -1;
	private int lifeCount;

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
		case GAMESTATUS_VOTING:
			int lifeCount = savedInstanceState.getInt(GAMESTATUS_VOTING_LIFECOUNT);
			onGainLife(lifeCount);
		}

		if(savedInstanceState != null && savedInstanceState.getInt(GAMESTATUS_TIMER) != -1) {
			int newTimer = savedInstanceState.getInt(GAMESTATUS_TIMER);
			int maxTimer = savedInstanceState.getInt(GAMESTATUS_MAXTIMER);
			onUpdateTimer(newTimer, maxTimer);
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

			outState.putInt(GAMESTATUS_TIMER, timer);
			outState.putInt(GAMESTATUS_MAXTIMER, maxTimer);
			break;
		case GAMESTATUS_VOTING:
			outState.putInt(GAMESTATUS_VOTING_LIFECOUNT, lifeCount);
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

	public void onGainLife(int newLife) {
		if(gameStatus != GAMESTATUS_VOTING)
			setContentView(R.layout.activity_game_voting);
		gameStatus = GAMESTATUS_VOTING;

		lifeCount = newLife;
		TextView lifecount = (TextView) findViewById(R.id.game_voting_lifecount);
		lifecount.setText(String.valueOf(newLife));
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

	public void onUpdateTimer(int newTimer, int maxTimer) {
		timer = newTimer;
		ProgressBar progress = (ProgressBar) findViewById(R.id.game_progress);
		TextView time = (TextView) findViewById(R.id.game_time);
		if(newTimer == -1) {
			progress.setVisibility(View.INVISIBLE);
			time.setVisibility(View.INVISIBLE);
			this.maxTimer = -1;
		} else {
			if(maxTimer != -1) {
				progress.setVisibility(View.VISIBLE);
				progress.setMax(maxTimer);
				this.maxTimer = maxTimer;

				time.setVisibility(View.VISIBLE);
			}

			progress.setProgress(timer);
			time.setText(getResources().getString(R.string.game_waiting_time, timer));
		}
	}
}
