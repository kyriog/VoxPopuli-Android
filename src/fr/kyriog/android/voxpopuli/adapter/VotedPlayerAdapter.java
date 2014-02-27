package fr.kyriog.android.voxpopuli.adapter;

import java.util.List;

import android.app.Activity;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

		TextView username = (TextView) convertView.findViewById(R.id.game_waiting_player_username);

		if(player.hasVoted()) {
			LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.game_waiting_player);
			layout.setBackgroundResource(R.drawable.gray_rounded_background);
			if(player.isDead())
				username.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		} else if(player.isDead()) {
			ImageView avatar = (ImageView) convertView.findViewById(R.id.game_waiting_player_image);
			avatar.setAlpha((float) 0.3);
			username.setAlpha((float) 0.3);
		}

		return convertView;
	}
}
