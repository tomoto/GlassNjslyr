package com.tomoto.glass.model.factory;

import android.content.res.Resources;

import com.tomoto.glass.njslyr.R;
import com.tomoto.glass.njslyr.model.StoryModel;

public class StoryModelFactory {
	private static StoryModel[] storyModels;
	
	public static synchronized StoryModel[] getStoryModels(Resources res) {
		if (storyModels == null) {
			storyModels = new StoryModel[] {
				createStoryModel(res, R.string.story_title_72801, R.array.story_text_72801, R.array.story_speech_72801),
				createStoryModel(res, R.string.story_title_72865, R.array.story_text_72865, R.array.story_speech_72865),
				createStoryModel(res, R.string.story_title_72874, R.array.story_text_72874, R.array.story_speech_72874),
				createStoryModel(res, R.string.story_title_72885, R.array.story_text_72885, R.array.story_speech_72885),
				createStoryModel(res, R.string.story_title_72400, R.array.story_text_72400, R.array.story_speech_72400),
				createStoryModel(res, R.string.story_title_67523, R.array.story_text_67523, R.array.story_speech_67523),
				createStoryModel(res, R.string.story_title_72554, R.array.story_text_72554, R.array.story_speech_72554),
				createStoryModel(res, R.string.story_title_67518, R.array.story_text_67518, R.array.story_speech_67518),
				createStoryModel(res, R.string.story_title_67517, R.array.story_text_67517, R.array.story_speech_67517),
				createStoryModel(res, R.string.story_title_67510, R.array.story_text_67510, R.array.story_speech_67510),
				createStoryModel(res, R.string.story_title_67506, R.array.story_text_67506, R.array.story_speech_67506),
				createStoryModel(res, R.string.story_title_67505, R.array.story_text_67505, R.array.story_speech_67505),
				createStoryModel(res, R.string.story_title_73081, R.array.story_text_73081, R.array.story_speech_73081),
				createStoryModel(res, R.string.story_title_73084, R.array.story_text_73084, R.array.story_speech_73084),
				createStoryModel(res, R.string.story_title_73090, R.array.story_text_73090, R.array.story_speech_73090),
				createStoryModel(res, R.string.story_title_73111, R.array.story_text_73111, R.array.story_speech_73111),
				createStoryModel(res, R.string.story_title_73121, R.array.story_text_73121, R.array.story_speech_73121),
				createStoryModel(res, R.string.story_title_73136, R.array.story_text_73136, R.array.story_speech_73136),
				createStoryModel(res, R.string.story_title_73139, R.array.story_text_73139, R.array.story_speech_73139),
				createStoryModel(res, R.string.story_title_73143, R.array.story_text_73143, R.array.story_speech_73143),
				createStoryModel(res, R.string.story_title_210287, R.array.story_text_210287, R.array.story_speech_210287),
				createStoryModel(res, R.string.story_title_210847, R.array.story_text_210847, R.array.story_speech_210847),
				createStoryModel(res, R.string.story_title_211063, R.array.story_text_211063, R.array.story_speech_211063),
				createStoryModel(res, R.string.story_title_77433, R.array.story_text_77433, R.array.story_speech_77433),
				createStoryModel(res, R.string.story_title_77439, R.array.story_text_77439, R.array.story_speech_77439),
			};
		}
		return storyModels;
	}
	
	public static int count = 0;

	private static StoryModel createStoryModel(Resources res, int titleResID, int textResID, int speechResID) {
		return new StoryModel(count++, res.getString(titleResID), res.getStringArray(textResID), res.getStringArray(speechResID));
	}
}
