package com.tomoto.glass.njslyr;

public class TextLineModel {
	private int index;
	private String text;
	private String speakingText;
	
	public TextLineModel(int index, String text, String speakingText) {
		this.index = index;
		this.text = text;
		this.speakingText = speakingText;
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	public String getSpeakingText() {
		return speakingText;
	}

	public void setSpeakingText(String speakingText) {
		this.speakingText = speakingText;
	}
}
