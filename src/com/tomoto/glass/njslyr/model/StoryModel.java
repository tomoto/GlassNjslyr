package com.tomoto.glass.njslyr.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class StoryModel implements Serializable {
	private int index;
	private String title;
	private TextLineModel[] lines;
	
	public StoryModel(int index, String title, String[] texts, String[] speakingTexts) {
		this.index = index;
		this.title = title;
		assert texts.length == speakingTexts.length;
		
		lines = new TextLineModel[texts.length];
		for (int i = 0; i < lines.length; i++) {
			lines[i] = new TextLineModel(i, texts[i], speakingTexts[i]);
		}
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public TextLineModel[] getLines() {
		return lines;
	}
	public void setLines(TextLineModel[] lines) {
		this.lines = lines;
	}
}
