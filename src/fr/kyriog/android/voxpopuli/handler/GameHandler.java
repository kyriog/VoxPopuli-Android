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
	public final static int ACTION_ADDPLAYER = 1001;
	public final static int ACTION_REMOVEPLAYER = 1002;

	public final static int STATUS_WAITING = 2000;

	private final Activity activity;
	private PlayerAdapter adapter;

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

				if(adapter == null)
					adapter = new PlayerAdapter(activity, players);

				GridView grid = (GridView) activity.findViewById(R.id.game_waiting_players);
				grid.setAdapter(adapter);
				break;
			}
			break;
		case ACTION_ADDPLAYER:
			Player player = (Player) msg.obj;
			adapter.add(player);
			break;
		case ACTION_REMOVEPLAYER:
			adapter.remove(msg.arg2);
			break;
		}
	}

}
