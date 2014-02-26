package fr.kyriog.android.voxpopuli.adapter;

import java.util.List;

import fr.kyriog.android.voxpopuli.R;
import fr.kyriog.android.voxpopuli.entity.Player;
import fr.kyriog.android.voxpopuli.handler.AvatarHandler;
import fr.kyriog.android.voxpopuli.thread.AvatarDownloader;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PlayerAdapter extends BaseAdapter {
	private final Activity activity;
	protected final List<Player> players;

	public PlayerAdapter(Activity activity, List<Player> players) {
		this.activity = activity;
		this.players = players;
	}

	@Override
	public int getCount() {
		return players.size();
	}

	@Override
	public Player getItem(int position) {
		return players.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null)
			convertView = activity.getLayoutInflater().inflate(R.layout.game_waiting_player, null);

		Player player = getItem(position);

		TextView username = (TextView) convertView.findViewById(R.id.game_waiting_player_username);
		username.setText(player.getUsername());

		if(player.getAvatarBitmap() == null) {
			AvatarDownloader downloader = new AvatarDownloader(new AvatarHandler(this), player);
			downloader.start();
		} else {
			ImageView avatar = (ImageView) convertView.findViewById(R.id.game_waiting_player_image);
			avatar.setImageBitmap(player.getAvatarBitmap());
		}

		return convertView;
	}
}
