package fr.kyriog.android.voxpopuli.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Question implements Parcelable {
	private final String question;
	private final String answerA;
	private final String answerB;
	private final String answerC;
	private int resultA;
	private int resultB;
	private int resultC;

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

	public int getResultA() {
		return resultA;
	}

	public void setResultA(int resultA) {
		this.resultA = resultA;
	}

	public int getResultB() {
		return resultB;
	}

	public void setResultB(int resultB) {
		this.resultB = resultB;
	}

	public int getResultC() {
		return resultC;
	}

	public void setResultC(int resultC) {
		this.resultC = resultC;
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

		int[] results = new int[] {
				resultA,
				resultB,
				resultC
		};
		dest.writeIntArray(results);
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

		int[] results = new int[3];
		in.readIntArray(results);
		resultA = results[0];
		resultB = results[1];
		resultC = results[2];
	}
}
