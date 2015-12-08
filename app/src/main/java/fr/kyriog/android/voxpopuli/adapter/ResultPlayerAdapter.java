package fr.kyriog.android.voxpopuli.adapter;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import fr.kyriog.android.voxpopuli.GameActivity;
import fr.kyriog.android.voxpopuli.R;
import fr.kyriog.android.voxpopuli.entity.Player;

public class ResultPlayerAdapter extends VotedPlayerAdapter {
	public ResultPlayerAdapter(Activity activity, List<Player> players) {
		super(activity, players);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = super.getView(position, convertView, parent);

		LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.game_waiting_player);
		Player player = getItem(position);
		switch(player.getVote()) {
		case GameActivity.ANSWER_A:
			layout.setBackgroundResource(R.drawable.blue_rounded_background);
			break;
		case GameActivity.ANSWER_B:
			layout.setBackgroundResource(R.drawable.red_rounded_background);
			break;
		case GameActivity.ANSWER_C:
			layout.setBackgroundResource(R.drawable.green_rounded_background);
			break;
		}

		return convertView;
	}
}
