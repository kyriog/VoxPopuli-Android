package fr.kyriog.android.voxpopuli.entity;

import java.util.List;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Player implements Parcelable {
	private String id;
	private String avatarUrl;
	private Bitmap avatarBitmap;
	private String username;
	private boolean hasVoted = false;

	public Player(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	public boolean hasVoted() {
		return hasVoted;
	}

	public void setVoted(boolean hasVoted) {
		this.hasVoted = hasVoted;
	}

	public static void resetVotes(List<Player> players) {
		for(Player player : players) {
			player.setVoted(false);
		}
	}

	public static Player getPlayerById(List<Player> players, String id) {
		if(id != null) {
			for(Player player : players) {
				if(id.equals(player.getId()))
					return player;
			}
		}
		return null;
	}

	public static Player getPlayerByUsername(List<Player> players, String username) {
		if(username != null) {
			for(Player player : players) {
				if(username.equals(player.getUsername()))
					return player;
			}
		}
		return null;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(avatarBitmap, 0);
		String[] data = new String[] {
				id,
				avatarUrl,
				username
		};
		dest.writeStringArray(data);
		boolean[] booleans = new boolean[] {
				hasVoted
		};
		dest.writeBooleanArray(booleans);
	}

	public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {
		@Override
		public Player createFromParcel(Parcel source) {
			return new Player(source);
		}

		@Override
		public Player[] newArray(int size) {
			return new Player[size];
		}
	};

	public Player(Parcel in) {
		avatarBitmap = in.readParcelable(Bitmap.class.getClassLoader());
		String[] data = new String[3];
		in.readStringArray(data);
		id = data[0];
		avatarUrl = data[1];
		username = data[2];
		boolean[] booleans = new boolean[1];
		in.readBooleanArray(booleans);
		hasVoted = booleans[0];
	}
}
