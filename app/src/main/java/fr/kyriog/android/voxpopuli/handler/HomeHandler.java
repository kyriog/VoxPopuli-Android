package fr.kyriog.android.voxpopuli.handler;

import java.util.List;

import fr.kyriog.android.voxpopuli.entity.Game;
import android.os.Handler;
import android.os.Message;
import android.widget.BaseAdapter;

public class HomeHandler extends Handler {
	private final List<Game> games;
	private final BaseAdapter adapter;

	public HomeHandler(List<Game> games, BaseAdapter adapter) {
		this.games = games;
		this.adapter = adapter;
	}

	@Override
	public void handleMessage(Message msg) {
		@SuppressWarnings("unchecked")
		List<Game> newGames = (List<Game>) msg.obj;
		games.clear();
		games.addAll(newGames);
		adapter.notifyDataSetChanged();
	}
}
