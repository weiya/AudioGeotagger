package com.vuw.audiogeotagger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * In this class, we initialize the home screen of the app(background, textviews, 
 * buttons & viewpaper). Also, We check whether a SD card has been inserted into 
 * the device
 * @author Weiya Xu
 *
 */
public class MainActivity extends Activity {
	private ViewPager viewPager;
	private List<View> viewsList;
	private TextView tv1, tv2, tv3;
	private int currentIndex = 0;
	private ListView listView;
	private List<HashMap<String, String>> itemsList;
	private HashMap<String, String> xmlPaths = new HashMap<String, String>();
	private HashMap<String, String> audioPaths = new HashMap<String, String>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		initTextView();
		initViewPager();
		initListView();
		initsdcard();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void initTextView() {
		tv1 = (TextView) findViewById(R.id.interviewsOption);
		tv2 = (TextView) findViewById(R.id.searchOption);
		tv3 = (TextView) findViewById(R.id.createOption);

		tv1.setOnClickListener(new MyOnClickListener(0));
		tv2.setOnClickListener(new MyOnClickListener(1));
		tv3.setOnClickListener(new MyOnClickListener(2));

		tv1.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
		tv1.setTextColor(Color.rgb(0, 51, 153));
	}

	private void initViewPager() {
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewsList = new ArrayList<View>();
		LayoutInflater inflater = getLayoutInflater();
		
		viewsList.add(inflater.inflate(R.layout.interviews, null));
		viewsList.add(inflater.inflate(R.layout.search, null));
		viewsList.add(inflater.inflate(R.layout.create, null));
		
		viewPager.setAdapter(new MyPagerAdapter(viewsList));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	private class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int idx) {
			this.index = idx;
		}

		@SuppressWarnings("deprecation")
		public void onClick(View view) {
			viewPager.setCurrentItem(index);
			if(index == 0) {
				tv1.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
				tv1.setTextColor(Color.rgb(0, 51, 153));
				tv2.setBackgroundColor(Color.TRANSPARENT);
				tv2.setTextColor(Color.BLACK);
				tv3.setBackgroundColor(Color.TRANSPARENT);
				tv3.setTextColor(Color.BLACK);
			} else if (index == 1) {
				tv2.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
				tv2.setTextColor(Color.rgb(0, 51, 153));
				tv1.setBackgroundColor(Color.TRANSPARENT);
				tv1.setTextColor(Color.BLACK);
				tv3.setBackgroundColor(Color.TRANSPARENT);
				tv3.setTextColor(Color.BLACK);
			} else {
				tv3.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
				tv3.setTextColor(Color.rgb(0, 51, 153));
				tv2.setBackgroundColor(Color.TRANSPARENT);
				tv2.setTextColor(Color.BLACK);
				tv1.setBackgroundColor(Color.TRANSPARENT);
				tv1.setTextColor(Color.BLACK);
			}
		}
	}

	private class MyPagerAdapter extends PagerAdapter {
		private List<View> viewsList;

		public MyPagerAdapter(List<View> viewsList) {
			this.viewsList = viewsList;
		}

		public int getCount() {
			return viewsList.size();
		}

		public boolean isViewFromObject(View arg0, Object arg1) {
			return (arg0 == arg1);
		}

		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(viewsList.get(arg1));
		}

		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(viewsList.get(arg1), 0);
			return viewsList.get(arg1);
		}
	}

	private class MyOnPageChangeListener implements OnPageChangeListener {
		public void onPageScrollStateChanged(int arg0) {}
		public void onPageScrolled(int arg0, float arg1, int arg2) {}

		@SuppressWarnings("deprecation")
		public void onPageSelected(int arg0) {
			switch(arg0) {
			case 0:
				if(currentIndex == 1) {
					tv1.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
					tv1.setTextColor(Color.rgb(0, 51, 153));
					tv2.setBackgroundColor(Color.TRANSPARENT);
					tv2.setTextColor(Color.BLACK);
				} else if(currentIndex == 2) {
					tv3.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
					tv3.setTextColor(Color.WHITE);
					tv2.setBackgroundColor(Color.TRANSPARENT);
					tv2.setTextColor(Color.BLACK);
				}
				break;
			case 1:
				if(currentIndex == 0) {
					tv2.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
					tv2.setTextColor(Color.rgb(0, 51, 153));
					tv1.setBackgroundColor(Color.TRANSPARENT);
					tv1.setTextColor(Color.BLACK);
				} else if(currentIndex == 2) {
					tv2.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
					tv2.setTextColor(Color.rgb(0, 51, 153));
					tv3.setBackgroundColor(Color.TRANSPARENT);
					tv3.setTextColor(Color.BLACK);
				}
				break;
			case 2:
				if(currentIndex == 0) {
					tv3.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
					tv3.setTextColor(Color.rgb(0, 51, 153));
					tv1.setBackgroundColor(Color.TRANSPARENT);
					tv1.setTextColor(Color.BLACK);
				} else if(currentIndex == 1) {
					tv3.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
					tv3.setTextColor(Color.rgb(0, 51, 153));
					tv2.setBackgroundColor(Color.TRANSPARENT);
					tv2.setTextColor(Color.BLACK);
				}
				break;
			}
			currentIndex = arg0;
		}
	}

	public void createButton(View view) {
		EditText title = (EditText) findViewById(R.id.titleText);
		EditText description = (EditText) findViewById(R.id.descriptionText);

		Intent intent = new Intent();
		intent.setClass(MainActivity.this, Map.class);
		intent.putExtra("title", title.getText().toString().trim());
		intent.putExtra("description", description.getText().toString().trim());
		MainActivity.this.startActivity(intent);
		finish();
	}

	public void initListView() {
		listView = (ListView) viewsList.get(0).findViewById(R.id.interviewsList);
		itemsList = new ArrayList<HashMap<String, String>>(); 

		try {
			BufferedReader br = new BufferedReader(new FileReader("/sdcard/Android/data/com.vuw.audiogeotagger/storage.txt"));
			HashMap<String, String> map = new HashMap<String, String>();
			String data;
			while((data = br.readLine()) != null) {
				map.put("titletextview", data);
				map.put("datatextview", br.readLine());
				itemsList.add(map);
				xmlPaths.put(map.get("titletextview"), br.readLine());
				audioPaths.put(map.get("titletextview"), br.readLine());
				map = new HashMap<String, String>();
			}
			br.close();
		} catch(Exception e) {

		}

		final SimpleAdapter listViewAdapter = new SimpleAdapter(getApplicationContext(), itemsList,  
				R.layout.listviewitem, new String[] { "titletextview", "datatextview" }, new int[] {  
			R.id.titletextview, R.id.datatextview }); 
		
		listView.setAdapter(listViewAdapter);
		listView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ListView listView = (ListView) arg0;
				HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(arg2);
				String title = map.get("titletextview");
				Intent intent = new Intent();
				intent.putExtra("title", title);
				intent.putExtra("xmlPath", xmlPaths.get(title));
				intent.putExtra("audioPath", audioPaths.get(title));
				intent.setClass(MainActivity.this, Preview.class);
				MainActivity.this.startActivity(intent);
			}
		});

		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final int position = arg2;
				final View listItem = arg1;
				Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake);
				final Button delete = (Button) arg1.findViewById(R.id.delete);
				delete.setVisibility(View.VISIBLE);
				final Button cancel = (Button) viewsList.get(0).findViewById(R.id.edit);
				cancel.setVisibility(View.VISIBLE);
				cancel.startAnimation(animation);
				final Button edit = (Button) arg1.findViewById(R.id.change);
				edit.setVisibility(View.VISIBLE);
				final Button overview = (Button) arg1.findViewById(R.id.overview);
				overview.setVisibility(View.VISIBLE);

				final ListView lv = (ListView) arg0;
				HashMap<String, String> map = (HashMap<String, String>) lv.getItemAtPosition(arg2);
				final String title = map.get("titletextview");
				final String description = map.get("datatextview");

				overview.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.putExtra("title", title);
						intent.putExtra("xmlPath", xmlPaths.get(title));
						intent.setClass(MainActivity.this, Overview.class);
						MainActivity.this.startActivity(intent);
					}
				});

				edit.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						View view = getLayoutInflater().inflate(R.layout.editpanel, null);
						final EditText titleField = (EditText) view.findViewById(R.id.edittitle);
						final EditText descriptionField = (EditText) view.findViewById(R.id.editdescription);
						titleField.setText(title);
						descriptionField.setText(description);
						final PopupWindow pw = new PopupWindow(view, 500, LayoutParams.WRAP_CONTENT);
						pw.setAnimationStyle(R.style.PopupAnimation);
						pw.showAtLocation(view, Gravity.CENTER, 0, -100);
						pw.setFocusable(true);
						pw.update();

						Button close = (Button) view.findViewById(R.id.close);
						close.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								pw.dismiss();
							}
						});
						Button confirm = (Button) view.findViewById(R.id.confirmButton);
						confirm.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								HashMap<String, String> map = (HashMap<String, String>) lv.getItemAtPosition(position);
								String newTitle = titleField.getText().toString();
								String newDescription = descriptionField.getText().toString();
								map.put("titletextview", newTitle);
								map.put("datatextview", newDescription);
								pw.dismiss();
								listViewAdapter.notifyDataSetChanged();

								delete.setVisibility(View.GONE);
								overview.setVisibility(View.GONE);
								edit.setVisibility(View.GONE);
								int count = 0;
								for(int i=0; i<listView.getChildCount(); i++) {
									View view = listView.getChildAt(i);
									Button delete = (Button) view.findViewById(R.id.delete);
									if(delete.getVisibility() == View.VISIBLE) count++;
								}
								if(count == 0) {
									cancel.clearAnimation();
									cancel.setVisibility(View.GONE);
								}

								updateSaveLocations(title, newTitle);
								updateStorageFile();
							}
						});
					}
				});

				delete.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.remove);
						animation.setAnimationListener(new AnimationListener() {
							public void onAnimationStart(Animation animation) {}
							public void onAnimationRepeat(Animation animation) {}
							public void onAnimationEnd(Animation animation) {
								delete.setVisibility(View.GONE);
								overview.setVisibility(View.GONE);
								edit.setVisibility(View.GONE);
								itemsList.remove(position);
								listViewAdapter.notifyDataSetChanged();

								int count = 0;
								String audioName = audioPaths.get(((TextView)listItem.findViewById(R.id.titletextview)).getText());
								String xmlName = xmlPaths.get(((TextView)listItem.findViewById(R.id.titletextview)).getText());

								for(int i=0; i<listView.getChildCount(); i++) {
									View view = listView.getChildAt(i);
									Button delete = (Button) view.findViewById(R.id.delete);
									if(delete.getVisibility() == View.VISIBLE) count++;
								}
								if(count == 0) {
									cancel.clearAnimation();
									cancel.setVisibility(View.GONE);
								}

								File f = new File(audioName);
								f.delete();
								f = new File("/data/data/com.vuw.audiogeotagger/files/" + xmlName);
								f.delete();

								try {
									BufferedWriter bw = new BufferedWriter(new FileWriter("/sdcard/Android/data/com.vuw.audiogeotagger/storage.txt"));
									for(int i=0; i<itemsList.size(); i++) {
										String title = itemsList.get(i).get("titletextview");
										String des = itemsList.get(i).get("datatextview");
										bw.write(title);
										bw.newLine();
										bw.write(des);
										bw.newLine();
										bw.write(xmlPaths.get(title));
										bw.newLine();
										bw.write(audioPaths.get(title));
										bw.newLine();
										bw.flush();
									}
									bw.close();
								} catch(Exception e) {}
							}
						});
						listView.getChildAt(position).startAnimation(animation);
					}
				});

				return true;
			}
		});
	}


	public void editCancel(View view) {
		Button cancel = (Button) view.findViewById(R.id.edit);

		for(int i=0; i<listView.getChildCount(); i++) {
			View v = listView.getChildAt(i);
			Button delete = (Button) v.findViewById(R.id.delete);
			delete.setVisibility(View.GONE);
			Button edit = (Button) v.findViewById(R.id.change);
			edit.setVisibility(View.GONE);
			Button overview = (Button) v.findViewById(R.id.overview);
			overview.setVisibility(View.GONE);
		}
		cancel.clearAnimation();
		cancel.setVisibility(View.GONE);
	}

	public void initsdcard() {
		boolean sdcard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);  
		if(!sdcard) {
			new AlertDialog.Builder(MainActivity.this).setTitle("No SD card").setMessage("Your device does not have a SD card inserted.")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}).show();
		}

		String sdpath = Environment.getExternalStorageDirectory() + "/Android/data/com.vuw.audiogeotagger/files";
		File path = new File(sdpath);
		if(!path.exists()) {
			path.mkdir();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(MainActivity.this).setMessage("Are you sure you want to quit ?")
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			})
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			}).show();

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void updateSaveLocations(String oldTile, String newTitle) {
		String xmlPath = xmlPaths.get(oldTile);
		String audioPath = audioPaths.get(oldTile);
		xmlPaths.remove(oldTile);
		audioPaths.remove(oldTile);
		xmlPaths.put(newTitle, xmlPath);
		audioPaths.put(newTitle, audioPath);
	}

	private void updateStorageFile() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("/sdcard/Android/data/com.vuw.audiogeotagger/storage.txt"));
			for(int i=0; i<itemsList.size(); i++) {
				String title = itemsList.get(i).get("titletextview");
				String des = itemsList.get(i).get("datatextview");
				bw.write(title);
				bw.newLine();
				bw.write(des);
				bw.newLine();
				bw.write(xmlPaths.get(title));
				bw.newLine();
				bw.write(audioPaths.get(title));
				bw.newLine();
				bw.flush();
			}
			bw.close();
		} catch(Exception e) {}
	}

}