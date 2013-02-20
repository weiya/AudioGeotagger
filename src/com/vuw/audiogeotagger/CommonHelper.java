package com.vuw.audiogeotagger;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * This class is used to show a progress dialog on the screen in order to 
 * inform users of the progress of the current operation
 * @author Weiya Xu
 *
 */
public class CommonHelper {
	private static ProgressDialog mProgress;

	public static void showProgress(Context context, CharSequence message) {
		mProgress = new ProgressDialog(context);
		mProgress.setMessage(message);
		mProgress.setIndeterminate(false);
		mProgress.setCancelable(false);
		mProgress.show();
	}

	public static void closeProgress() {
		try {
			if( mProgress != null ) {
				mProgress.dismiss();
				mProgress = null;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}   
}