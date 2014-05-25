package com.tomoto.glass.njslyr.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TextLineModel implements Serializable {
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
		// return speakingText;
		return text.replaceAll("[「」『』　]", "、").replaceAll("[？！]", "。");
	}

	public void setSpeakingText(String speakingText) {
		this.speakingText = speakingText;
	}
}
