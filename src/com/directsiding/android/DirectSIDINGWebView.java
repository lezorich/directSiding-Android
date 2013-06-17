/**
 *  DirectSIDING: Log-in directo al SIDING desde tu dispositivo Android.
 *  La idea original de DirectSIDING fue de Pedro Pablo Aste Kompen.
 *  
 *  Copyright (C) 2013  Lukas Zorich
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.directsiding.android;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Custom webview en el que modificamos el evento onTouchEvent para poder sacar los controles del zoom.
 * @author lukas
 *
 */

public class DirectSIDINGWebView extends WebView {

	public DirectSIDINGWebView(Context context) {
		super(context);
	}
	
	public DirectSIDINGWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			if (event.getAction() == MotionEvent.ACTION_DOWN || 
					event.getAction() == MotionEvent.ACTION_POINTER_DOWN || 
					event.getAction() == MotionEvent.ACTION_POINTER_1_DOWN ||
					event.getAction() == MotionEvent.ACTION_POINTER_2_DOWN ||
					event.getAction() == MotionEvent.ACTION_POINTER_3_DOWN) {
				if (event.getPointerCount() > 1) {
					getSettings().setBuiltInZoomControls(true);
				} else {
					getSettings().setBuiltInZoomControls(false);
				}
				
			}
		}
		
		return super.onTouchEvent(event);
	}
}
