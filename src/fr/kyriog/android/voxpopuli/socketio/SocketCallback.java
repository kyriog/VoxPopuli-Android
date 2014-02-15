package fr.kyriog.android.voxpopuli.socketio;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import fr.kyriog.android.voxpopuli.entity.Player;
import fr.kyriog.android.voxpopuli.handler.GameHandler;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;

public class SocketCallback implements IOCallback {
	private final Handler handler;

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
						List<Player> players = new ArrayList<Player>();
						JSONObject jsonPlayers = data.getJSONObject("players");
						JSONArray jsonId = jsonPlayers.names();
						for(int i = 0; i < jsonPlayers.length(); i++) {
							String id = jsonId.getString(i);
							JSONObject jsonPlayer = jsonPlayers.getJSONObject(id);
							Player player = new Player(jsonPlayer.getInt("user_id"));
							player.setUsername(jsonPlayer.getString("screen_name"));
							player.setAvatar(jsonPlayer.getString("avatar_url"));
							players.add(player);
						}

						msg.obj = players;
						handler.sendMessage(msg);
					}
				}
			} catch (JSONException e) {
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
