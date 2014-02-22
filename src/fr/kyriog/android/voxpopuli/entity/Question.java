package fr.kyriog.android.voxpopuli.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Question implements Parcelable {
	private final String question;
	private final String answerA;
	private final String answerB;
	private final String answerC;

	public Question(String question, String answerA, String answerB, String answerC) {
		this.question = question;
		this.answerA = answerA;
		this.answerB = answerB;
		this.answerC = answerC;
	}

	public String getQuestion() {
		return question;
	}

	public String getAnswerA() {
		return answerA;
	}

	public String getAnswerB() {
		return answerB;
	}

	public String getAnswerC() {
		return answerC;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		String[] strings = new String[] {
				question,
				answerA,
				answerB,
				answerC
		};
		dest.writeStringArray(strings);
	}

	public static final Parcelable.Creator<Question> CREATOR = new Parcelable.Creator<Question>() {
		@Override
		public Question createFromParcel(Parcel source) {
			return new Question(source);
		}

		@Override
		public Question[] newArray(int size) {
			return new Question[size];
		}
	};

	public Question(Parcel in) {
		String[] strings = new String[4];
		in.readStringArray(strings);
		question = strings[0];
		answerA = strings[1];
		answerB = strings[2];
		answerC = strings[3];
	}
}
