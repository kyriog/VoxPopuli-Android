package fr.kyriog.android.voxpopuli.socketio;

import org.json.JSONObject;

import android.os.Handler;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;

public abstract class BaseCallback implements IOCallback {
	protected Handler handler;

	public BaseCallback(Handler handler) {
		this.handler = handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void onError(SocketIOException e) {
		e.printStackTrace();
	}

	@Override
	public abstract void on(String event, IOAcknowledge ack, Object... args);

	@Override
	public void onConnect() {}
	@Override
	public void onDisconnect() {}
	@Override
	public void onMessage(String data, IOAcknowledge ack) {}
	@Override
	public void onMessage(JSONObject json, IOAcknowledge ack) {}
}
