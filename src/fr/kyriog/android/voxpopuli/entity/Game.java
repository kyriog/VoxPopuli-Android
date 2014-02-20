package fr.kyriog.android.voxpopuli.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Game implements Parcelable {
	private String id;
	private String gamemode;
	private int nbPlayers;
	private int nbMinPlayers;
	private int nbMaxPlayers;

	public Game(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGamemode() {
		return gamemode;
	}

	public void setGamemode(String gamemode) {
		this.gamemode = gamemode;
	}

	public int getNbPlayers() {
		return nbPlayers;
	}

	public void setNbPlayers(int nbPlayers) {
		this.nbPlayers = nbPlayers;
	}

	public int getNbMinPlayers() {
		return nbMinPlayers;
	}

	public void setNbMinPlayers(int nbMinPlayers) {
		this.nbMinPlayers = nbMinPlayers;
	}

	public int getNbMaxPlayers() {
		return nbMaxPlayers;
	}

	public void setNbMaxPlayers(int nbMaxPlayers) {
		this.nbMaxPlayers = nbMaxPlayers;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Game) {
			Game oGame = (Game) o;
			return id.equals(oGame.getId());
		}
		return false;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		String[] strings = new String[] {
				id,
				gamemode
		};
		dest.writeStringArray(strings);

		int[] ints = new int[] {
				nbPlayers,
				nbMinPlayers,
				nbMaxPlayers
		};
		dest.writeIntArray(ints);
	}

	public static final Parcelable.Creator<Game> CREATOR = new Parcelable.Creator<Game>() {
		@Override
		public Game createFromParcel(Parcel source) {
			return new Game(source);
		}

		@Override
		public Game[] newArray(int size) {
			return new Game[size];
		}
	};

	public Game(Parcel in) {
		String[] strings = new String[2];
		in.readStringArray(strings);
		id = strings[0];
		gamemode = strings[1];

		int[] ints = new int[3];
		in.readIntArray(ints);
		nbPlayers = ints[0];
		nbMinPlayers = ints[1];
		nbMaxPlayers = ints[2];
	}
}
