package fr.kyriog.android.voxpopuli.thread;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet();
			try {
				URI uri = new URI(player.getAvatarUrl());
				httpGet.setURI(uri);
				httpGet.addHeader("Accept-Encoding", "gzip");
				HttpResponse response = httpClient.execute(httpGet);
				InputStream is = response.getEntity().getContent();
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				if(bitmap != null) {
					player.setAvatarBitmap(bitmap);
					handler.sendEmptyMessage(0);
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
