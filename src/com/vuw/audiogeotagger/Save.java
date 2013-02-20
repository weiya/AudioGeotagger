package com.vuw.audiogeotagger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import com.vuw.audiogeotagger.MainActivity;

/**
 * This class displays save page on the screen. The module will write the title, 
 * description, audio and xml paths of the interview into the file. Otherwise, the
 * audio and xml files will not be saved on the SD card
 * @author Weiya Xu
 *
 */
public class Save extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.save);
		EditText title = (EditText) findViewById(R.id.saveTitleText);
		title.setText(getIntent().getStringExtra("title"));
		EditText description = (EditText) findViewById(R.id.saveDescriptionText);
		description.setText(getIntent().getStringExtra("description"));
	}
	
	public void previewButton(View view) {
		Intent intent = new Intent();
		intent.putExtra("xmlPath", getIntent().getStringExtra("xmlPath"));
		intent.putExtra("audioPath", getIntent().getStringExtra("audioPath"));
		intent.putExtra("title", getIntent().getStringExtra("title"));
		intent.setClass(Save.this, Preview.class);
		Save.this.startActivity(intent);
	}
	
	public void saveButton(View view) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("/sdcard/Android/data/com.vuw.audiogeotagger/storage.txt", true));
			bw.write(getIntent().getStringExtra("title"));
			bw.newLine();
			bw.write(getIntent().getStringExtra("description"));
			bw.newLine();
			bw.write(getIntent().getStringExtra("xmlPath"));
			bw.newLine();
			bw.write(getIntent().getStringExtra("audioPath"));
			bw.newLine();
			bw.close();
		} catch(Exception e) {}
		
		Intent intent = new Intent();
		intent.setClass(Save.this, MainActivity.class);
		Save.this.startActivity(intent);
		finish();
	}
	
	public void cancelButton(View view) {
		File f = new File(getIntent().getStringExtra("audioPath"));
		f.delete();
		Intent intent = new Intent();
		intent.setClass(Save.this, MainActivity.class);
		Save.this.startActivity(intent);
		finish();
	}
}