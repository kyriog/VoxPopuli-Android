package fr.kyriog.android.voxpopuli.thread;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import fr.kyriog.android.voxpopuli.entity.Player;
import fr.kyriog.android.voxpopuli.handler.AvatarHandler;

public class AvatarDownloader extends Thread {
	private final AvatarHandler handler;
	private final Player player;

	public AvatarDownloader(AvatarHandler handler, Player player) {
		this.handler = handler;
		this.player = player;
	}

	@Override
	public void run() {
		if(player.getAvatarBitmap() == null) {
			try {
				URL url = new URL(player.getAvatarUrl());
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				InputStream is = new BufferedInputStream(connection.getInputStream());
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				if(bitmap != null) {
					player.setAvatarBitmap(bitmap);
					handler.sendEmptyMessage(0);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
