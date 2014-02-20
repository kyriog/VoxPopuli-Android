package fr.kyriog.android.voxpopuli.handler;

import io.socket.SocketIO;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import fr.kyriog.android.voxpopuli.GameActivity;
import fr.kyriog.android.voxpopuli.HomeActivity;
import fr.kyriog.android.voxpopuli.R;
import fr.kyriog.android.voxpopuli.adapter.PlayerAdapter;
import fr.kyriog.android.voxpopuli.entity.Player;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class GameHandler extends Handler {
	public final static int ACTION_ROOMDATA = 1000;
	public final static int ACTION_ADDPLAYER = 1001;
	public final static int ACTION_REMOVEPLAYER = 1002;
	public final static int ACTION_UPDATETIMER = 1003;
	public final static int ACTION_GAINLIFE = 1004;
	public final static int ACTION_NEWQUESTION = 1005;
	public final static int ACTION_SHOWVOTES = 1006;
	public final static int ACTION_LOOSELIFE = 1007;
	public final static int ACTION_ENDGAME = 1008;

	public final static int STATUS_WAITING = 2000;

	public final static String BUNDLE_PLAYERS = "players";
	public final static String BUNDLE_CURRENT_PLAYER_COUNT = "currentPlayerCount";
	public final static String BUNDLE_START_PLAYER_COUNT = "startPlayerCount";
	public final static String BUNDLE_MAX_PLAYER_COUNT = "maxPlayerCount";
	public final static String BUNDLE_QUESTION = "question";
	public final static String BUNDLE_ANSWER_A = "answerA";
	public final static String BUNDLE_ANSWER_B = "answerB";
	public final static String BUNDLE_ANSWER_C = "answerC";
	public final static String BUNDLE_GAME = "game";

	private final GameActivity activity;
	private final SocketIO socket;
	private PlayerAdapter adapter;

	private boolean progressLaunched = false;
	private boolean gameStarted = false;

	public GameHandler(GameActivity activity, SocketIO socket) {
		this.activity = activity;
		this.socket = socket;
	}

	@SuppressLint("CutPasteId")
	@Override
	public void handleMessage(Message msg) {
		switch(msg.arg1) {
		case ACTION_ROOMDATA:
			switch(msg.arg2) {
			case STATUS_WAITING:
				Bundle data = (Bundle) msg.obj;
				List<Player> players = data.getParcelableArrayList(BUNDLE_PLAYERS);
				int currentPlayerCount = data.getInt(BUNDLE_CURRENT_PLAYER_COUNT);
				int startPlayerCount = data.getInt(BUNDLE_START_PLAYER_COUNT);
				int maxPlayerCount = data.getInt(BUNDLE_MAX_PLAYER_COUNT);

				activity.onWaiting(players, currentPlayerCount, startPlayerCount, maxPlayerCount);
				break;
			}
			break;
		case ACTION_ADDPLAYER:
			activity.onAddPlayer((Player) msg.obj);
			break;
		case ACTION_REMOVEPLAYER:
			activity.onRemovePlayer((String) msg.obj);
			break;
		case ACTION_UPDATETIMER:
			ProgressBar progress = (ProgressBar) activity.findViewById(R.id.game_progress);
			TextView time = (TextView) activity.findViewById(R.id.game_time);
			if(msg.arg2 == -1) {
				progress.setVisibility(View.INVISIBLE);
				time.setVisibility(View.INVISIBLE);
				progressLaunched = false;
			} else {
				if(!progressLaunched) {
					progress.setVisibility(View.VISIBLE);
					progress.setMax(msg.arg2);
				}
				progress.setProgress(msg.arg2);

				if(!progressLaunched) {
					time.setVisibility(View.VISIBLE);
					progressLaunched = true;
				}
				time.setText(activity.getResources().getString(R.string.game_waiting_time, msg.arg2));
			}
			break;
		case ACTION_GAINLIFE:
			if(!gameStarted) {
				activity.setContentView(R.layout.activity_game_voting);
				progressLaunched = false;
				gameStarted = true;
			}
			TextView lifecount = (TextView) activity.findViewById(R.id.game_voting_lifecount);
			lifecount.setText(String.valueOf(msg.arg2));
			break;
		case ACTION_NEWQUESTION:
			Bundle questionData = (Bundle) msg.obj;

			TextView question = (TextView) activity.findViewById(R.id.game_voting_question);
			question.setText(questionData.getString(BUNDLE_QUESTION));

			Button answerA = (Button) activity.findViewById(R.id.game_voting_answer_a);
			answerA.setText(questionData.getString(BUNDLE_ANSWER_A));
			answerA.setOnClickListener(new OnAnswerListener(OnAnswerListener.ANSWER_A));
			answerA.setEnabled(true);

			Button answerB = (Button) activity.findViewById(R.id.game_voting_answer_b);
			answerB.setText(questionData.getString(BUNDLE_ANSWER_B));
			answerB.setOnClickListener(new OnAnswerListener(OnAnswerListener.ANSWER_B));
			answerB.setEnabled(true);

			Button answerC = (Button) activity.findViewById(R.id.game_voting_answer_c);
			answerC.setText(questionData.getString(BUNDLE_ANSWER_C));
			answerC.setOnClickListener(new OnAnswerListener(OnAnswerListener.ANSWER_C));
			answerC.setEnabled(true);

			TextView votesInvisibleA = (TextView) activity.findViewById(R.id.game_voting_vote_a);
			votesInvisibleA.setVisibility(View.GONE);

			TextView votesInvisibleB = (TextView) activity.findViewById(R.id.game_voting_vote_b);
			votesInvisibleB.setVisibility(View.GONE);

			TextView votesInvisibleC = (TextView) activity.findViewById(R.id.game_voting_vote_c);
			votesInvisibleC.setVisibility(View.GONE);
			break;
		case ACTION_SHOWVOTES:
			Bundle votesData = (Bundle) msg.obj;

			Button answerDisableA = (Button) activity.findViewById(R.id.game_voting_answer_a);
			answerDisableA.setEnabled(false);
			Button answerDisableB = (Button) activity.findViewById(R.id.game_voting_answer_b);
			answerDisableB.setEnabled(false);
			Button answerDisableC = (Button) activity.findViewById(R.id.game_voting_answer_c);
			answerDisableC.setEnabled(false);

			TextView votesA = (TextView) activity.findViewById(R.id.game_voting_vote_a);
			votesA.setText(String.valueOf(votesData.getInt(BUNDLE_ANSWER_A)));
			votesA.setVisibility(View.VISIBLE);

			TextView votesB = (TextView) activity.findViewById(R.id.game_voting_vote_b);
			votesB.setText(String.valueOf(votesData.getInt(BUNDLE_ANSWER_B)));
			votesB.setVisibility(View.VISIBLE);

			TextView votesC = (TextView) activity.findViewById(R.id.game_voting_vote_c);
			votesC.setText(String.valueOf(votesData.getInt(BUNDLE_ANSWER_C)));
			votesC.setVisibility(View.VISIBLE);
			break;
		case ACTION_LOOSELIFE:
			TextView lifecounter = (TextView) activity.findViewById(R.id.game_voting_lifecount);
			lifecounter.setText(String.valueOf(msg.arg2));
			String toastMsg = activity.getResources().getString(R.string.game_voting_looselife);
			Toast toast = Toast.makeText(activity, toastMsg, Toast.LENGTH_SHORT);
			toast.show();
			break;
		case ACTION_ENDGAME:
			Bundle endData = (Bundle) msg.obj;
			final Intent intent = new Intent();
			intent.putExtra(HomeActivity.VP_DATA_GAME, endData.getString(BUNDLE_GAME));
			activity.setResult(Activity.RESULT_FIRST_USER, intent);

			String[] players = endData.getStringArray(BUNDLE_PLAYERS);
			Player winner1 = adapter.getPlayerByUsername(players[0]);
			Player winner2 = adapter.getPlayerByUsername(players[1]);

			activity.setContentView(R.layout.activity_game_ending);

			if(winner1 != null) {
				ImageView imageWinner1 = (ImageView) activity.findViewById(R.id.game_ending_winner1_image);
				imageWinner1.setImageBitmap(winner1.getAvatarBitmap());

				TextView usernameWinner1 = (TextView) activity.findViewById(R.id.game_ending_winner1_username);
				usernameWinner1.setText("@" + winner1.getUsername());
			}

			if(winner2 != null) {
				ImageView imageWinner2 = (ImageView) activity.findViewById(R.id.game_ending_winner2_image);
				imageWinner2.setImageBitmap(winner2.getAvatarBitmap());

				TextView usernameWinner2 = (TextView) activity.findViewById(R.id.game_ending_winner2_username);
				usernameWinner2.setText("@" + winner2.getUsername());
			}

			Button newGame = (Button) activity.findViewById(R.id.game_ending_newgame);
			newGame.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					activity.setResult(Activity.RESULT_OK, intent);
					activity.finish();
				}
			});
			break;
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
				Button answerA = (Button) activity.findViewById(R.id.game_voting_answer_a);
				answerA.setEnabled(false);
				Button answerB = (Button) activity.findViewById(R.id.game_voting_answer_b);
				answerB.setEnabled(false);
				Button answerC = (Button) activity.findViewById(R.id.game_voting_answer_c);
				answerC.setEnabled(false);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
