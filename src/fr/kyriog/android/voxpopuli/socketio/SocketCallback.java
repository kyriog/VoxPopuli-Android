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
import fr.kyriog.android.voxpopuli.handler.GameHandler;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;

public class SocketCallback implements IOCallback {
	private final Handler handler;
	private int timer = -1;
	private Timer timerThread;

	public SocketCallback(Handler handler) {
		this.handler = handler;
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
					msg.arg2 = rootData.getInt("player"); // Player ID
					handler.sendMessage(msg);
				} else if("updateTimer".equals(action)) {
					int newTimer = (int) Math.floor(rootData.getInt("newValue")/1000);
					int diff = Math.abs(timer - newTimer);
					if(diff > 1) {
						if(timer == -1) {
							timerThread = new Timer();
							timerThread.start();
						}
						Log.w("timer", "Timer not synchronized!");
						timer = newTimer;
						sendTimerUpdate();
					}
				} else if("removeTimer".equals(action)) {
					resetTimer();
					sendTimerUpdate();
				} else if("gainLife".equals(action)) {
					msg.arg1 = GameHandler.ACTION_GAINLIFE;
					msg.arg2 = rootData.getInt("newPoints");
					handler.sendMessage(msg);
				} else if("newQuestion".equals(action)) {
					msg.arg1 = GameHandler.ACTION_NEWQUESTION;
					JSONObject jsonQuestion = rootData.getJSONObject("question");
					JSONArray jsonAnswers = jsonQuestion.getJSONArray("answers");
					Bundle question = new Bundle();
					question.putString(GameHandler.BUNDLE_QUESTION, jsonQuestion.getString("content"));
					for(int i = 0; i < 3; i++) {
						String answer = jsonAnswers.getJSONArray(i).getString(1);
						switch(i) {
						case 0:
							question.putString(GameHandler.BUNDLE_ANSWER_A, answer);
							break;
						case 1:
							question.putString(GameHandler.BUNDLE_ANSWER_B, answer);
							break;
						case 2:
							question.putString(GameHandler.BUNDLE_ANSWER_C, answer);
							break;
						}
					}
					msg.obj = question;
					handler.sendMessage(msg);
					resetTimer();
				} else if("showVotes".equals(action)) {
					msg.arg1 = GameHandler.ACTION_SHOWVOTES;
					Bundle data = new Bundle();
					JSONArray votes = rootData.getJSONArray("votes");
					for(int i = 0; i < 3; i++) {
						int vote = votes.getInt(i);
						switch(i) {
						case 0:
							data.putInt(GameHandler.BUNDLE_ANSWER_A, vote);
							break;
						case 1:
							data.putInt(GameHandler.BUNDLE_ANSWER_B, vote);
							break;
						case 2:
							data.putInt(GameHandler.BUNDLE_ANSWER_C, vote);
							break;
						}
					}
					msg.obj = data;
					handler.sendMessage(msg);
					resetTimer();
				} else if("looseLife".equals(action)) {
					msg.arg1 = GameHandler.ACTION_LOOSELIFE;
					msg.arg2 = rootData.getInt("newPoints"); // Nb of lifes
					handler.sendMessage(msg);
				} else if("endGame".equals(action)) {
					msg.arg1 = GameHandler.ACTION_ENDGAME;
					Bundle endData = new Bundle();
					JSONArray jsonPlayers = rootData.getJSONArray("winners");
					String[] players = new String[jsonPlayers.length()];
					for(int i = 0; i < jsonPlayers.length(); i++) {
						players[i] = jsonPlayers.getString(i);
					}
					endData.putStringArray(GameHandler.BUNDLE_PLAYERS, players);
					String jsonMessage = rootData.getString("message");
					String newGame = jsonMessage.split("/game/", 2)[1].split("\\\"", 2)[0];
					endData.putString(GameHandler.BUNDLE_GAME, newGame);
					msg.obj = endData;
					handler.sendMessage(msg);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

	private Player createPlayerFromJSONObject(JSONObject jsonPlayer) throws JSONException {
		Player player = new Player(jsonPlayer.getInt("user_id"));
		player.setUsername(jsonPlayer.getString("screen_name"));
		player.setAvatarUrl(jsonPlayer.getString("avatar_url"));
		return player;
	}

	private void sendTimerUpdate() {
		Message msg = new Message();
		msg.arg1 = GameHandler.ACTION_UPDATETIMER;
		msg.arg2 = timer;
		handler.sendMessage(msg);
	}

	private void resetTimer() {
		if(timerThread != null && timerThread.isAlive())
			timerThread.interrupt();
		timer = -1;
		sendTimerUpdate();
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
					sendTimerUpdate();
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
