package com.peng.slidepicker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements FragmentListener{

	@Bind(R.id.pickText)
	Button pickText;

	private SelectorDialog dialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
	}

	@OnClick(R.id.pickerBtn)
	public void onClick() {
		List<String> data = new ArrayList<String>();
		for (int i = 0; i < 10; i++) {
			data.add("测试文字123456" + i);
		}
		dialog = SelectorDialog.newInstance(data);
		dialog.show(getFragmentManager(), "dialog");
	}

	@Override
	public void showText(String text) {
		pickText.setText(text);
		dialog.dismiss();
	}
}
