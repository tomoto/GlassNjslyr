package com.tomoto.glass.njslyr;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class AbstractCardScrollAdapter<T> extends CardScrollAdapter {
	protected List<T> items;
	protected List<Card> cards;
	
	public AbstractCardScrollAdapter() {
		// implementation should initialize the items and cards
	}
	
	@Override
	public int getPosition(Object id) {
		if (id instanceof Number) {
			return ((Number)id).intValue();
		} else {
			return CardScrollView.INVALID_POSITION;
		}
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public T getItem(int position) {
		return items.get(position);
	}

	@Override
	public View getView(int position, View contentView, ViewGroup parent) {
		return cards.get(position).getView();
	}
}
