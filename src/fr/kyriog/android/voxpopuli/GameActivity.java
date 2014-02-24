package fr.kyriog.android.voxpopuli;

import fr.kyriog.android.voxpopuli.adapter.PlayerAdapter;
import fr.kyriog.android.voxpopuli.entity.Player;
import fr.kyriog.android.voxpopuli.entity.Question;
import fr.kyriog.android.voxpopuli.handler.GameHandler;
import fr.kyriog.android.voxpopuli.socketio.BaseCallback;
import fr.kyriog.android.voxpopuli.socketio.GameCallback;
import io.socket.SocketIO;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity {
	private final static int GAMESTATUS_WAITING = 1;
	private final static int GAMESTATUS_VOTING = 2;
	private final static int GAMESTATUS_VOTED = 3;
	private final static int GAMESTATUS_RESULTS = 4;
	private final static int GAMESTATUS_ENDED = 5;
	private final static String GAMESTATUS = "gameStatus";
	private final static String GAMESTATUS_TIMER = "timer";
	private final static String GAMESTATUS_MAXTIMER = "maxTimer";
	private final static String GAMESTATUS_PLAYERS = "players";
	private final static String GAMESTATUS_WAITING_NBPLAYERS = "waitingNbPlayers";
	private final static String GAMESTATUS_WAITING_NBMINPLAYERS = "waitingNbMinPlayers";
	private final static String GAMESTATUS_WAITING_NBMAXPLAYERS = "waitingNbMaxPlayers";
	private final static String GAMESTATUS_VOTING_VOTINGPLAYERS_COUNT = "votingVotingPlayersCount";
	private final static String GAMESTATUS_VOTING_ALIVEPLAYERS_COUNT = "votingAlivePlayersCount";
	private final static String GAMESTATUS_VOTING_LIFECOUNT = "votingLifeCount";
	private final static String GAMESTATUS_VOTING_QUESTIONNB = "votingQuestionNb";
	private final static String GAMESTATUS_VOTING_QUESTION = "votingQuestion";
	private final static String GAMESTATUS_VOTED_ANSWER = "votedAnswer";
	private final static String GAMESTATUS_ENDED_WINNERS = "endedWinners";

	private static SocketIO socket;
	private static BaseCallback callback;

	private BaseAdapter adapter;
	private int gameStatus = 0;
	private ArrayList<Player> players = new ArrayList<Player>();
	private int nbPlayers;
	private int nbMinPlayers;
	private int nbMaxPlayers;
	private int nbAlivePlayers;
	private int timer = -1;
	private int maxTimer = -1;
	private int lifeCount;
	private AlertDialog deathDialog;
	private int questionNb = 1;
	private Question question;
	private int votedAnswer;
	private int nbVotingPlayers;
	private boolean votingDisplayed = false;
	private final String[] winners = new String[2];
	private boolean canPlay = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
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
		GameHandler handler = new GameHandler(this);
		if(!socket.isConnected()) {
			callback = new GameCallback(handler);
			socket.connect(callback);
		} else
			callback.setHandler(handler);

		if(savedInstanceState != null) {
			gameStatus = savedInstanceState.getInt(GAMESTATUS);
			players = savedInstanceState.getParcelableArrayList(GAMESTATUS_PLAYERS);
		}

		setTitle(getResources().getString(R.string.game_waiting_title, extras.getString(HomeActivity.VP_DATA_GAME)));
		switch(gameStatus) {
		case 0:
			setContentView(R.layout.loading);
			TextView text = (TextView) findViewById(R.id.loading_text);
			text.setText(R.string.connecting);
			break;
		case GAMESTATUS_WAITING:
			int nbPlayers = savedInstanceState.getInt(GAMESTATUS_WAITING_NBPLAYERS);
			int nbMinPlayers = savedInstanceState.getInt(GAMESTATUS_WAITING_NBMINPLAYERS);
			int nbMaxPlayers = savedInstanceState.getInt(GAMESTATUS_WAITING_NBMAXPLAYERS);
			List<Player> playersClone = new ArrayList<Player>(players);
			onWaiting(playersClone, nbPlayers, nbMinPlayers, nbMaxPlayers);
			break;
		case GAMESTATUS_VOTING:
			onGainLife(savedInstanceState.getInt(GAMESTATUS_VOTING_LIFECOUNT));
			nbAlivePlayers = savedInstanceState.getInt(GAMESTATUS_VOTING_ALIVEPLAYERS_COUNT);
			if(savedInstanceState.containsKey(GAMESTATUS_VOTING_QUESTION)) {
				questionNb = savedInstanceState.getInt(GAMESTATUS_VOTING_QUESTIONNB);
				Question questionVoting = savedInstanceState.getParcelable(GAMESTATUS_VOTING_QUESTION);
				onNewQuestion(questionVoting);
				nbVotingPlayers = savedInstanceState.getInt(GAMESTATUS_VOTING_VOTINGPLAYERS_COUNT);
				updateVotingPlayersCount();
			}
			break;
		case GAMESTATUS_VOTED: // Could it be optimized?
			onGainLife(savedInstanceState.getInt(GAMESTATUS_VOTING_LIFECOUNT));
			nbAlivePlayers = savedInstanceState.getInt(GAMESTATUS_VOTING_ALIVEPLAYERS_COUNT);
			questionNb = savedInstanceState.getInt(GAMESTATUS_VOTING_QUESTIONNB);
			Question questionVoted = savedInstanceState.getParcelable(GAMESTATUS_VOTING_QUESTION);
			onNewQuestion(questionVoted);
			nbVotingPlayers = savedInstanceState.getInt(GAMESTATUS_VOTING_VOTINGPLAYERS_COUNT);
			updateVotingPlayersCount();
			votedAnswer = savedInstanceState.getInt(GAMESTATUS_VOTED_ANSWER);
			onVote(votedAnswer);
			break;
		case GAMESTATUS_RESULTS:
			onGainLife(savedInstanceState.getInt(GAMESTATUS_VOTING_LIFECOUNT));
			nbAlivePlayers = savedInstanceState.getInt(GAMESTATUS_VOTING_ALIVEPLAYERS_COUNT);
			questionNb = savedInstanceState.getInt(GAMESTATUS_VOTING_QUESTIONNB);
			Question questionResults = savedInstanceState.getParcelable(GAMESTATUS_VOTING_QUESTION);
			onNewQuestion(questionResults);
			nbVotingPlayers = savedInstanceState.getInt(GAMESTATUS_VOTING_VOTINGPLAYERS_COUNT);
			updateVotingPlayersCount();
			onShowVotes(questionResults);
			break;
		case GAMESTATUS_ENDED:
			String[] winners = savedInstanceState.getStringArray(GAMESTATUS_ENDED_WINNERS);
			onEndGame(winners[0], winners[1]);
			break;
		}

		if(savedInstanceState != null && savedInstanceState.getInt(GAMESTATUS_MAXTIMER) != -1) {
			int newTimer = savedInstanceState.getInt(GAMESTATUS_TIMER);
			int maxTimer = savedInstanceState.getInt(GAMESTATUS_MAXTIMER);
			onUpdateTimer(newTimer, maxTimer);
		}
	}

	@Override
	public void onBackPressed() {
		doBack();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			doBack();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void doBack() {
		if(gameStatus == GAMESTATUS_WAITING || gameStatus == GAMESTATUS_ENDED)
			finish();
		else {
			Resources res = getResources();
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setTitle(R.string.game_leave_title);
			dialog.setMessage(res.getString(R.string.game_leave_message));
			dialog.setButton(AlertDialog.BUTTON_NEGATIVE, res.getString(R.string.game_leave_cancel), (DialogInterface.OnClickListener) null);
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, res.getString(R.string.game_leave_ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			dialog.show();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(GAMESTATUS, gameStatus);
		outState.putInt(GAMESTATUS_TIMER, timer);
		outState.putInt(GAMESTATUS_MAXTIMER, maxTimer);
		outState.putParcelableArrayList(GAMESTATUS_PLAYERS, players);
		switch(gameStatus) {
		case GAMESTATUS_WAITING:
			outState.putInt(GAMESTATUS_WAITING_NBPLAYERS, nbPlayers);
			outState.putInt(GAMESTATUS_WAITING_NBMINPLAYERS, nbMinPlayers);
			outState.putInt(GAMESTATUS_WAITING_NBMAXPLAYERS, nbMaxPlayers);
			break;
		case GAMESTATUS_VOTED:
			outState.putInt(GAMESTATUS_VOTED_ANSWER, votedAnswer);
		case GAMESTATUS_VOTING:
		case GAMESTATUS_RESULTS:
			outState.putInt(GAMESTATUS_VOTING_VOTINGPLAYERS_COUNT, nbVotingPlayers);
			outState.putInt(GAMESTATUS_VOTING_ALIVEPLAYERS_COUNT, nbAlivePlayers);
			outState.putInt(GAMESTATUS_VOTING_LIFECOUNT, lifeCount);
			outState.putInt(GAMESTATUS_VOTING_QUESTIONNB, questionNb-1);
			if(question != null)
				outState.putParcelable(GAMESTATUS_VOTING_QUESTION, question);
			break;
		case GAMESTATUS_ENDED:
			outState.putStringArray(GAMESTATUS_ENDED_WINNERS, winners);
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
		nbAlivePlayers = nbPlayers;
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

	private void displayVoteLayout() {
		if(!votingDisplayed) {
			setContentView(R.layout.activity_game_voting);
			votingDisplayed = true;
		}
		gameStatus = GAMESTATUS_VOTING;
	}

	public void onGainLife(int newLife) {
		displayVoteLayout();

		lifeCount = newLife;
		TextView lifecount = (TextView) findViewById(R.id.game_voting_lifecount);
		TextView dead = (TextView) findViewById(R.id.game_voting_life);
		if(newLife > 0) {
			canPlay = true;
			lifecount.setText(String.valueOf(newLife));
			lifecount.setVisibility(View.VISIBLE);

			dead.setText("💜");
			if(deathDialog != null && deathDialog.isShowing())
				deathDialog.cancel();
		} else {
			canPlay = false;
			lifecount.setVisibility(View.INVISIBLE);

			dead.setText("😟");

			if(deathDialog == null || !deathDialog.isShowing()) {
				deathDialog = new AlertDialog.Builder(this).create();
				deathDialog.setTitle(R.string.game_voting_death_title);
				deathDialog.setMessage(getResources().getString(R.string.game_voting_death_message));
				deathDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.game_voting_death_button), (DialogInterface.OnClickListener) null);
				deathDialog.show();
			}
		}
	}

	public void onLooseLife(int newLife) {
		String toastMsg;
		if(lifeCount - newLife == 1)
			toastMsg = getResources().getString(R.string.game_voting_looselife_one);
		else
			toastMsg = getResources().getString(R.string.game_voting_looselife_more, lifeCount - newLife);
		Toast toast = Toast.makeText(this, toastMsg, Toast.LENGTH_LONG);
		toast.show();
		onGainLife(newLife);
	}

	public void onNewQuestion(Question question) {
		displayVoteLayout();

		this.question = question;

		setTitle(getResources().getString(R.string.game_voting_title, questionNb++));
		TextView questionView = (TextView) findViewById(R.id.game_voting_question);
		questionView.setText(question.getQuestion());

		Button answerA = (Button) findViewById(R.id.game_voting_answer_a);
		answerA.setBackgroundResource(R.drawable.blue_btn);
		answerA.setText(getResources().getString(R.string.game_voting_answera, question.getAnswerA()));
		answerA.setOnClickListener(new OnAnswerListener(OnAnswerListener.ANSWER_A));

		Button answerB = (Button) findViewById(R.id.game_voting_answer_b);
		answerB.setBackgroundResource(R.drawable.red_btn);
		answerB.setText(getResources().getString(R.string.game_voting_answerb, question.getAnswerB()));
		answerB.setOnClickListener(new OnAnswerListener(OnAnswerListener.ANSWER_B));

		Button answerC = (Button) findViewById(R.id.game_voting_answer_c);
		answerC.setBackgroundResource(R.drawable.green_btn);
		answerC.setText(getResources().getString(R.string.game_voting_answerc, question.getAnswerC()));
		answerC.setOnClickListener(new OnAnswerListener(OnAnswerListener.ANSWER_C));

		answerA.setEnabled(canPlay);
		answerB.setEnabled(canPlay);
		answerC.setEnabled(canPlay);

		TextView votesInvisibleA = (TextView) findViewById(R.id.game_voting_vote_a);
		votesInvisibleA.setVisibility(View.INVISIBLE);

		TextView votesInvisibleB = (TextView) findViewById(R.id.game_voting_vote_b);
		votesInvisibleB.setVisibility(View.INVISIBLE);

		TextView votesInvisibleC = (TextView) findViewById(R.id.game_voting_vote_c);
		votesInvisibleC.setVisibility(View.INVISIBLE);

		nbVotingPlayers = 0;
		updateVotingPlayersCount();
		updateAlivePlayersCount();
	}

	public void increaseVotingPlayers() {
		nbVotingPlayers++;
		updateVotingPlayersCount();
	}

	private void updateVotingPlayersCount() {
		String text = getResources().getQuantityString(R.plurals.game_voting_voting_players_count, nbVotingPlayers, nbVotingPlayers);
		TextView votingPlayers = (TextView) findViewById(R.id.game_voting_voting_players_count);
		votingPlayers.setText(text);
	}

	public void decreaseAlivePlayers(int deadPlayersCount) {
		nbAlivePlayers -= deadPlayersCount;
		updateAlivePlayersCount();
	}

	private void updateAlivePlayersCount() {
		String text = getResources().getQuantityString(R.plurals.game_voting_alive_players_count, nbAlivePlayers, nbAlivePlayers);
		TextView alivePlayers = (TextView) findViewById(R.id.game_voting_alive_players_count);
		alivePlayers.setText(text);
	}

	public void onVote(int votingAnswer) {
		gameStatus = GAMESTATUS_VOTED;

		Button answerA = (Button) findViewById(R.id.game_voting_answer_a);
		answerA.setEnabled(false);
		Button answerB = (Button) findViewById(R.id.game_voting_answer_b);
		answerB.setEnabled(false);
		Button answerC = (Button) findViewById(R.id.game_voting_answer_c);
		answerC.setEnabled(false);

		votedAnswer = votingAnswer;
		switch(votingAnswer) {
		case OnAnswerListener.ANSWER_A:
			answerA.setBackgroundResource(R.drawable.blue_btn_pressed);
			answerA.setTextColor(getResources().getColor(android.R.color.primary_text_light));
			break;
		case OnAnswerListener.ANSWER_B:
			answerB.setBackgroundResource(R.drawable.red_btn_pressed);
			answerB.setTextColor(getResources().getColor(android.R.color.primary_text_light));
			break;
		case OnAnswerListener.ANSWER_C:
			answerC.setBackgroundResource(R.drawable.green_btn_pressed);
			answerC.setTextColor(getResources().getColor(android.R.color.primary_text_light));
			break;
		}
	}

	public void onShowVotes(Question question) {
		gameStatus = GAMESTATUS_RESULTS;

		Button answerDisableA = (Button) findViewById(R.id.game_voting_answer_a);
		answerDisableA.setEnabled(false);
		Button answerDisableB = (Button) findViewById(R.id.game_voting_answer_b);
		answerDisableB.setEnabled(false);
		Button answerDisableC = (Button) findViewById(R.id.game_voting_answer_c);
		answerDisableC.setEnabled(false);

		TextView votesA = (TextView) findViewById(R.id.game_voting_vote_a);
		votesA.setText(String.valueOf(question.getResultA()));
		votesA.setVisibility(View.VISIBLE);

		TextView votesB = (TextView) findViewById(R.id.game_voting_vote_b);
		votesB.setText(String.valueOf(question.getResultB()));
		votesB.setVisibility(View.VISIBLE);

		TextView votesC = (TextView) findViewById(R.id.game_voting_vote_c);
		votesC.setText(String.valueOf(question.getResultC()));
		votesC.setVisibility(View.VISIBLE);
	}

	public void onEndGame(String winner1, String winner2) {
		gameStatus = GAMESTATUS_ENDED;
		setContentView(R.layout.activity_game_ending);
		setTitle(R.string.game_ending_title);

		winners[0] = winner1;
		Player player1 = Player.getPlayerByUsername(players, winner1);
		if(player1 != null) {
			ImageView imageWinner1 = (ImageView) findViewById(R.id.game_ending_winner1_image);
			imageWinner1.setImageBitmap(player1.getAvatarBitmap());

			TextView usernameWinner1 = (TextView) findViewById(R.id.game_ending_winner1_username);
			usernameWinner1.setText("@" + player1.getUsername());
		}

		winners[1] = winner2;
		Player player2 = Player.getPlayerByUsername(players, winner2);
		if(player2 != null) {
			ImageView imageWinner2 = (ImageView) findViewById(R.id.game_ending_winner2_image);
			imageWinner2.setImageBitmap(player2.getAvatarBitmap());

			TextView usernameWinner2 = (TextView) findViewById(R.id.game_ending_winner2_username);
			usernameWinner2.setText("@" + player2.getUsername());
		} else {
			TextView and = (TextView) findViewById(R.id.game_ending_and);
			and.setVisibility(View.INVISIBLE);
		}

		Button newGame = (Button) findViewById(R.id.game_ending_newgame);
		newGame.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
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

	private class OnAnswerListener implements OnClickListener {
		public final static int ANSWER_A = 0;
		public final static int ANSWER_B = 1;
		public final static int ANSWER_C = 2;

		private final int answer;

		public OnAnswerListener(int answer) {
			this.answer = answer;
		}

		@Override
		public void onClick(View v) {
			try {
				JSONObject data = new JSONObject();
				data.put("action", "vote");
				data.put("voteid", answer);
				socket.emit("clientEvent", data);
				onVote(answer);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
