package fr.kyriog.android.voxpopuli.handler;

import java.util.List;

import fr.kyriog.android.voxpopuli.GameActivity;
import fr.kyriog.android.voxpopuli.entity.Player;
import fr.kyriog.android.voxpopuli.entity.Question;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

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
	public final static int ACTION_HASVOTED = 1009;

	public final static int STATUS_WAITING = 2000;

	public final static String BUNDLE_PLAYERS = "players";
	public final static String BUNDLE_CURRENT_PLAYER_COUNT = "currentPlayerCount";
	public final static String BUNDLE_START_PLAYER_COUNT = "startPlayerCount";
	public final static String BUNDLE_MAX_PLAYER_COUNT = "maxPlayerCount";
	public final static String BUNDLE_QUESTION = "question";
	public final static String BUNDLE_DEADPLAYERS_COUNT = "deadPlayersCount";
	public final static String BUNDLE_MAJORITIES = "majorities";

	private final GameActivity activity;

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
		case ACTION_HASVOTED:
			activity.increaseVotingPlayers((String) msg.obj);
			break;
		case ACTION_SHOWVOTES:
			Bundle data = (Bundle) msg.obj;
			Question question = data.getParcelable(BUNDLE_QUESTION);
			int deadCount = data.getInt(BUNDLE_DEADPLAYERS_COUNT);
			int[] majorities = data.getIntArray(BUNDLE_MAJORITIES);
			activity.onShowVotes(question, majorities);
			activity.decreaseAlivePlayers(deadCount);
			break;
		case ACTION_LOOSELIFE:
			activity.onLooseLife(msg.arg2);
			break;
		case ACTION_ENDGAME:
			Bundle endData = (Bundle) msg.obj;

			String[] players = endData.getStringArray(BUNDLE_PLAYERS);
			activity.onEndGame(players[0], players[1]);
			break;
		}
	}
}
