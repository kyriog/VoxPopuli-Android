package fr.kyriog.android.voxpopuli.entity;

import android.graphics.Bitmap;

public class Player {
	private int id;
	private String avatarUrl;
	private Bitmap avatarBitmap;
	private String username;

	public Player(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public Bitmap getAvatarBitmap() {
		return avatarBitmap;
	}

	public void setAvatarBitmap(Bitmap avatarBitmap) {
		this.avatarBitmap = avatarBitmap;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
