package fr.kyriog.android.voxpopuli.socketio;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import fr.kyriog.android.voxpopuli.entity.Player;
import fr.kyriog.android.voxpopuli.entity.Question;
import fr.kyriog.android.voxpopuli.handler.GameHandler;
import io.socket.IOAcknowledge;
import io.socket.SocketIOException;

public class GameCallback extends BaseCallback {
	private Question question;
	private int timer = -1;
	private Timer timerThread;

	public GameCallback(Handler handler) {
		super(handler);
	}

	@Override
	public void on(String event, IOAcknowledge ack, Object... args) {
		if("gameEvent".equals(event)) {
			try {
				Message msg = new Message();
				JSONObject rootData = (JSONObject) args[0];
				String action = rootData.getString("action");
				if("roomData".equals(action)) {
					msg.arg1 = GameHandler.ACTION_ROOMDATA;
					JSONObject data = rootData.getJSONObject("roomData");
					if("waiting".equals(data.getString("status"))) {
						msg.arg2 = GameHandler.STATUS_WAITING;
						ArrayList<Player> players = new ArrayList<Player>();
						JSONObject jsonPlayers = data.getJSONObject("players");
						JSONArray jsonId = jsonPlayers.names();
						for(int i = 0; i < jsonPlayers.length(); i++) {
							String id = jsonId.getString(i);
							JSONObject jsonPlayer = jsonPlayers.getJSONObject(id);
							Player player = createPlayerFromJSONObject(jsonPlayer);
							players.add(player);
						}
						Bundle extras = new Bundle();
						extras.putParcelableArrayList(GameHandler.BUNDLE_PLAYERS, players);
						extras.putInt(GameHandler.BUNDLE_CURRENT_PLAYER_COUNT, data.getInt("nbPlayers"));
						extras.putInt(GameHandler.BUNDLE_START_PLAYER_COUNT, data.getInt("minPlayers"));
						extras.putInt(GameHandler.BUNDLE_MAX_PLAYER_COUNT, data.getInt("maxPlayers"));
						msg.obj = extras;
						handler.sendMessage(msg);
					}
				} else if("addPlayer".equals(action)) {
					msg.arg1 = GameHandler.ACTION_ADDPLAYER;
					JSONObject jsonPlayer = rootData.getJSONObject("player");
					Player player = createPlayerFromJSONObject(jsonPlayer);
					msg.obj = player;
					handler.sendMessage(msg);
				} else if("removePlayer".equals(action)) {
					msg.arg1 = GameHandler.ACTION_REMOVEPLAYER;
					msg.obj = rootData.getString("player"); // Player ID
					handler.sendMessage(msg);
				} else if("updateTimer".equals(action)) {
					int newTimer = (int) Math.floor(rootData.getInt("newValue")/1000);
					int maxTimer = (int) Math.floor(rootData.getInt("maxValue")/1000);
					int diff = Math.abs(timer - newTimer);
					if(diff > 1) {
						if(timer == -1) {
							timerThread = new Timer();
							timerThread.start();
						}
						Log.w("timer", "Timer not synchronized!");
						timer = newTimer;
						sendTimerUpdate(maxTimer);
					}
				} else if("removeTimer".equals(action)) {
					resetTimer();
					sendTimerUpdate(-1);
				} else if("gainLife".equals(action)) {
					msg.arg1 = GameHandler.ACTION_GAINLIFE;
					msg.arg2 = rootData.getInt("newPoints");
					handler.sendMessage(msg);
				} else if("newQuestion".equals(action)) {
					msg.arg1 = GameHandler.ACTION_NEWQUESTION;
					JSONObject jsonQuestion = rootData.getJSONObject("question");
					JSONArray jsonAnswers = jsonQuestion.getJSONArray("answers");
					String questionText = jsonQuestion.getString("content");
					String answerA = null,  answerB = null, answerC = null;
					for(int i = 0; i < 3; i++) {
						String answer = jsonAnswers.getJSONArray(i).getString(1);
						switch(i) {
						case 0:
							answerA = answer;
							break;
						case 1:
							answerB = answer;
							break;
						case 2:
							answerC = answer;
							break;
						}
					}
					question = new Question(questionText, answerA, answerB, answerC);
					msg.obj = question;
					handler.sendMessage(msg);
					resetTimer();
				} else if("hasVoted".equals(action)) {
					msg.arg1 = GameHandler.ACTION_HASVOTED;
					String voter = rootData.getString("player");
					msg.obj = voter;
					handler.sendMessage(msg);
				} else if("showVotes".equals(action)) {
					msg.arg1 = GameHandler.ACTION_SHOWVOTES;
					Bundle data = new Bundle();

					JSONArray votes = rootData.getJSONArray("votes");
					for(int i = 0; i < 3; i++) {
						int vote = votes.getInt(i);
						switch(i) {
						case 0:
							question.setResultA(vote);
							break;
						case 1:
							question.setResultB(vote);
							break;
						case 2:
							question.setResultC(vote);
							break;
						}
					}
					data.putParcelable(GameHandler.BUNDLE_QUESTION, question);

					ArrayList<Player> playersVotes = new ArrayList<Player>();
					JSONObject jsonPlayersVotes = rootData.getJSONObject("votesNamed");
					JSONArray playersIds = jsonPlayersVotes.names();
					if(playersIds != null) {
						for(int i = 0; i < playersIds.length(); i++) {
							Player player = new Player(playersIds.getString(i));
							player.setVote(jsonPlayersVotes.getInt(playersIds.getString(i)));
							playersVotes.add(player);
						}
					}
					data.putParcelableArrayList(GameHandler.BUNDLE_PLAYERS, playersVotes);

					JSONArray jsonDeadPlayers = rootData.getJSONArray("deadPlayers");
					String[] deadPlayers = new String[jsonDeadPlayers.length()];
					for(int i = 0; i < jsonDeadPlayers.length(); i++) {
						deadPlayers[i] = jsonDeadPlayers.getString(i);
					}
					data.putStringArray(GameHandler.BUNDLE_DEADPLAYERS, deadPlayers);

					JSONArray jsonMajorities = rootData.getJSONArray("majs");
					int[] majorities = new int[jsonMajorities.length()];
					for(int i = 0; i < jsonMajorities.length(); i++) {
						majorities[i] = jsonMajorities.getInt(i);
					}
					data.putIntArray(GameHandler.BUNDLE_MAJORITIES, majorities);

					msg.obj = data;
					handler.sendMessage(msg);
					resetTimer();
				} else if("looseLife".equals(action)) {
					msg.arg1 = GameHandler.ACTION_LOOSELIFE;
					msg.arg2 = rootData.getInt("newPoints"); // Nb of lifes
					handler.sendMessage(msg);
				} else if("endGame".equals(action)) {
					resetTimer();
					msg.arg1 = GameHandler.ACTION_ENDGAME;
					Bundle endData = new Bundle();
					JSONArray jsonPlayers = rootData.getJSONArray("winners");
					String[] players;
					if(jsonPlayers.length() > 2)
						players = new String[jsonPlayers.length()];
					else
						players = new String[2];
					for(int i = 0; i < jsonPlayers.length(); i++) {
						players[i] = jsonPlayers.getString(i);
					}
					endData.putStringArray(GameHandler.BUNDLE_PLAYERS, players);
					msg.obj = endData;
					handler.sendMessage(msg);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

	private Player createPlayerFromJSONObject(JSONObject jsonPlayer) throws JSONException {
		Player player = new Player(jsonPlayer.getString("user_id"));
		player.setUsername(jsonPlayer.getString("screen_name"));
		player.setAvatarUrl(jsonPlayer.getString("avatar_url"));
		return player;
	}

	private void sendTimerUpdate(int maxValue) {
		Message msg = new Message();
		msg.arg1 = GameHandler.ACTION_UPDATETIMER;
		msg.arg2 = timer;
		msg.obj = maxValue;
		handler.sendMessage(msg);
	}

	private void resetTimer() {
		if(timerThread != null && timerThread.isAlive())
			timerThread.interrupt();
		timer = -1;
		sendTimerUpdate(-1);
	}

	private class Timer extends Thread {
		@Override
		public void run() {
			try {
				while(timer > 0) {
					sleep(1000);
					if(interrupted())
						return;
					timer--;
					sendTimerUpdate(-1);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onError(SocketIOException socketIOException) {
		socketIOException.printStackTrace();
	}

	@Override
	public void onConnect() {}
	@Override
	public void onDisconnect() {}
	@Override
	public void onMessage(String data, IOAcknowledge ack) {}
	@Override
	public void onMessage(JSONObject json, IOAcknowledge ack) {}
}
