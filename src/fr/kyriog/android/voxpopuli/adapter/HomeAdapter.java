package fr.kyriog.android.voxpopuli.adapter;

import java.util.List;

import fr.kyriog.android.voxpopuli.HomeActivity;
import fr.kyriog.android.voxpopuli.R;
import fr.kyriog.android.voxpopuli.entity.Game;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class HomeAdapter extends BaseAdapter {
	private final HomeActivity activity;
	private final List<Game> games;

	public HomeAdapter(HomeActivity activity, List<Game> games) {
		this.activity = activity;
		this.games = games;
	}

	@Override
	public int getCount() {
		return games.size();
	}

	@Override
	public Game getItem(int position) {
		return games.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Game game = getItem(position);

		if(convertView == null)
			convertView = activity.getLayoutInflater().inflate(R.layout.home_game, null);

		Button gameId = (Button) convertView.findViewById(R.id.home_game_id);
		gameId.setText(game.getId());
		gameId.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.launchGame(game);
			}
		});

		TextView gamemode = (TextView) convertView.findViewById(R.id.home_game_mode);
		gamemode.setText(game.getGamemode());

		TextView players = (TextView) convertView.findViewById(R.id.home_game_players);
		players.setText(activity.getResources().getString(R.string.home_game_players, game.getNbPlayers(), game.getNbMinPlayers()));

		return convertView;
	}

}
