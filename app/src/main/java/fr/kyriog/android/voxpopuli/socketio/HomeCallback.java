package fr.kyriog.android.voxpopuli.socketio;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.kyriog.android.voxpopuli.entity.Game;

import android.os.Handler;
import android.os.Message;

import io.socket.IOAcknowledge;

public class HomeCallback extends BaseCallback {
	public HomeCallback(Handler handler) {
		super(handler);
	}

	@Override
	public void on(String event, IOAcknowledge ack, Object... args) {
		if("tickList".equals(event)) {
			try {
				JSONObject json = (JSONObject) args[0];
				JSONArray waitingGames = json.getJSONArray("waiting");
				List<Game> games = new ArrayList<Game>();
				for(int i = 0; i < waitingGames.length(); i++) {
					JSONObject jsonGame = waitingGames.getJSONObject(i);
					Game game = new Game(jsonGame.getString("room_id"));
					game.setGamemode(jsonGame.getJSONObject("gamemode").getString("name"));
					game.setNbPlayers(jsonGame.getInt("nbPlayers"));
					game.setNbMinPlayers(jsonGame.getInt("minPlayers"));
					game.setNbMaxPlayers(jsonGame.getInt("maxPlayers"));

					games.add(game);
				}

				Message msg = new Message();
				msg.obj = games;
				handler.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
