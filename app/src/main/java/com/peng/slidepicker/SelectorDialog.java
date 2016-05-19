package com.peng.slidepicker;

import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by PS on 2016/5/17.
 */
public class SelectorDialog extends DialogFragment {

	@Bind(R.id.SlidePicker)
	SlidePicker slidePicker;

	private static List<String> mData = null;

	private FragmentListener fragmentListener = null;

	private static final String TAG = SelectorDialog.class.getSimpleName();

	public static SelectorDialog newInstance(List<String> data) {
		mData = data;
		return new SelectorDialog();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_selector_layout, container, false);
		ButterKnife.bind(this, view);

		slidePicker.setData(mData);
		slidePicker.setLineColor(getResources().getColor(R.color.lineColor));
		slidePicker.setOnSelectListener(new SlidePicker.onSelectListener() {
			@Override
			public void onSelect(String text) {
				Log.d(TAG, "onSelect " + text);
			}
		});
		slidePicker.setPickOnClickListener(new SlidePicker.pickClickListener() {
			@Override
			public void onClick(String text) {
				fragmentListener.showText(text);
			}
		});
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		fragmentListener = (FragmentListener)activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(true);
		int style = DialogFragment.STYLE_NO_TITLE;
		setStyle(style, 0);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}

}
