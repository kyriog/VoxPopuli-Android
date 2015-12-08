package fr.kyriog.android.voxpopuli.socketio;

import android.os.Handler;

import io.socket.emitter.Emitter;

public abstract class BaseCallback implements Emitter.Listener {
	protected Handler handler;

	public BaseCallback(Handler handler) {
		this.handler = handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
}
