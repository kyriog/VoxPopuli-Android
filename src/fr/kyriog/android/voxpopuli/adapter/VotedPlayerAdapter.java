package fr.kyriog.android.voxpopuli.adapter;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import fr.kyriog.android.voxpopuli.R;
import fr.kyriog.android.voxpopuli.entity.Player;

public class VotedPlayerAdapter extends PlayerAdapter {
	public VotedPlayerAdapter(Activity activity, List<Player> players) {
		super(activity, players);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = super.getView(position, convertView, parent);
		Player player = getItem(position);

		float alpha = (float) 0.5;
		if(player.hasVoted())
			alpha = 1;

		ImageView avatar = (ImageView) convertView.findViewById(R.id.game_waiting_player_image);
		TextView username = (TextView) convertView.findViewById(R.id.game_waiting_player_username);
		avatar.setAlpha(alpha);
		username.setAlpha(alpha);

		return convertView;
	}
}
