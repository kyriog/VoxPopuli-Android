package fr.kyriog.android.voxpopuli.handler;

import java.util.List;

import fr.kyriog.android.voxpopuli.GameActivity;
import fr.kyriog.android.voxpopuli.HomeActivity;
import fr.kyriog.android.voxpopuli.R;
import fr.kyriog.android.voxpopuli.adapter.PlayerAdapter;
import fr.kyriog.android.voxpopuli.entity.Player;
import fr.kyriog.android.voxpopuli.entity.Question;
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
import android.widget.TextView;

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
	private PlayerAdapter adapter;

	public GameHandler(GameActivity activity) {
		this.activity = activity;
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
			activity.onUpdateTimer(msg.arg2, (Integer) msg.obj);
			break;
		case ACTION_GAINLIFE:
			activity.onGainLife(msg.arg2);
			break;
		case ACTION_NEWQUESTION:
			activity.onNewQuestion((Question) msg.obj);
			break;
		case ACTION_SHOWVOTES:
			activity.onShowVotes((Question) msg.obj);
			break;
		case ACTION_LOOSELIFE:
			activity.onLooseLife();
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
}
