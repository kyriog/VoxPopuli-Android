package fr.kyriog.android.voxpopuli.entity;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Player implements Parcelable {
	private String id;
	private String avatarUrl;
	private Bitmap avatarBitmap;
	private String username;

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
	}
}
