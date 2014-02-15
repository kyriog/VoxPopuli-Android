package fr.kyriog.android.voxpopuli.handler;

import java.util.List;

import fr.kyriog.android.voxpopuli.R;
import fr.kyriog.android.voxpopuli.adapter.PlayerAdapter;
import fr.kyriog.android.voxpopuli.entity.Player;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.GridView;

public class GameHandler extends Handler {
	public final static int ACTION_ROOMDATA = 1000;

	public final static int STATUS_WAITING = 2000;

	private final Activity activity;

	public GameHandler(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message msg) {
		switch(msg.arg1) {
		case ACTION_ROOMDATA:
			switch(msg.arg2) {
			case STATUS_WAITING:
				activity.setContentView(R.layout.activity_game_waiting);

				@SuppressWarnings("unchecked")
				List<Player> players = (List<Player>) msg.obj;

				GridView grid = (GridView) activity.findViewById(R.id.game_waiting_players);
				grid.setAdapter(new PlayerAdapter(activity, players));
				break;
			}
			break;
		}
	}

}
