/**
 *  DirectSIDING: Log-in directo al SIDING desde tu dispositivo Android.
 *  La idea original de DirectSIDING fue de Pedro Pablo Aste Kompen.
 *  
    Copyright (C) 2013  Lukas Zorich

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.directsiding.Interfaces;

public interface ISharedPrefs {
	
	public final static String SHARED_PREFERENCE_NAME = "com.directsiding.SavedPreferences";
	public final static String RECORDAR_PREF = "recordadPref";
	public final static String USUARIO_PREF = "usuarioPref";
	public final static String PASSWD_PREF = "passwdPref";
	public final static String INGCURSOS_PREF = "directoIngCursos";
	public final static String ENTRARAUTO_PREF = "entrarAutomaticamente";
	
}
