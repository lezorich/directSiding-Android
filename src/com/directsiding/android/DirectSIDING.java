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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.Application;

public class DirectSIDING extends Application {
	
	public final static String SHARED_PREFERENCE_NAME = "com.directsiding.SavedPreferences";
	public final static String RECORDAR_PREF = "recordadPref";
	public final static String USUARIO_PREF = "usuarioPref";
	public final static String PASSWD_PREF = "passwdPref";
	public final static String INGCURSOS_PREF = "directoIngCursos";
	public final static String ENTRARAUTO_PREF = "entrarAutomaticamente";
	public final static String PREF_ZOOM_ENABLED = "zoomEnabled";
	
	public static final String EXTRA_WHICH_ACTIVITY = "EXTRA_ACTIVITY";
	
	private Map<String, List<CustomAsyncTask<?, ?, ?>>> mActivityTaskMap;
	
	public DirectSIDING() {
		mActivityTaskMap = new HashMap<String, List<CustomAsyncTask<?, ?, ?>>>();
	}
	
	public void removeTask(CustomAsyncTask<?, ?, ?> task) {
		for (Entry<String, List<CustomAsyncTask<?, ?, ?>>> entry : mActivityTaskMap.entrySet()) {
			List<CustomAsyncTask<?, ?, ?>> tasks = entry.getValue();
			for (int i = 0; i < tasks.size(); i++) {
				if (tasks.get(i) == task) {
					tasks.remove(i);
					break;
				}
			}
			
			if (tasks.size() == 0) {
				mActivityTaskMap.remove(entry.getKey());
				return;
			}
		}
	}
	
	public void addTask(Activity activity, CustomAsyncTask<?, ?, ?> task) {
		 String key = activity.getClass().getCanonicalName();
	        List<CustomAsyncTask<?,?,?>> tasks = mActivityTaskMap.get(key);
	        if (tasks == null) {
	            tasks = new ArrayList<CustomAsyncTask<?,?,?>>();
	            mActivityTaskMap.put(key, tasks);
	        }
	 
	        tasks.add(task);
	}
	
	public void detach(Activity activity) {
        List<CustomAsyncTask<?,?,?>> tasks = mActivityTaskMap.get(activity.getClass().getCanonicalName());
        if (tasks != null) {
            for (CustomAsyncTask<?,?,?> task : tasks) {
                task.setActivity(null);
            }
        }
    }
 
    public void attach(Activity activity) {
        List<CustomAsyncTask<?,?,?>> tasks = mActivityTaskMap.get(activity.getClass().getCanonicalName());
        if (tasks != null) {
            for (CustomAsyncTask<?,?,?> task : tasks) {
                task.setActivity(activity);
            }
        }
    }
	
}
