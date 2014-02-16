package fr.kyriog.android.voxpopuli.handler;

import android.os.Handler;
import android.os.Message;
import android.widget.BaseAdapter;

public class AvatarHandler extends Handler {
	private final BaseAdapter adapter;

	public AvatarHandler(BaseAdapter adapter) {
		this.adapter = adapter;
	}

	@Override
	public void handleMessage(Message msg) {
		adapter.notifyDataSetChanged();
	}
}
