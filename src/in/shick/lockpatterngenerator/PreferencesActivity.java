/**
Copyright 2010,2011 Michael Shick

This file is part of lockpatterngenerator.

Lockpatterngenerator is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

lockpatterngenerator is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with lockpatterngenerator.  If not, see <http://www.gnu.org/licenses/>.
*/
package in.shick.lockpatterngenerator;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.widget.LinearLayout;
import android.content.DialogInterface;

public class PreferencesActivity extends PreferenceActivity
{
    public static final int DIALOG_DANGEROUS_ID = 0;
    private SharedPreferences generationPrefs;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        Preference dangerousPref = (Preference) findPreference("arbitraryGridPref");
        generationPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        dangerousPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if(generationPrefs.getBoolean("arbitraryGridPref",false))
                    showDialog(PreferencesActivity.DIALOG_DANGEROUS_ID);
                return true;
            }
        });
    }

    protected Dialog onCreateDialog(int id)
    {
        Dialog dialog = null;
        switch(id)
        {
            case DIALOG_DANGEROUS_ID:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Enabling this feature will allow you to make " +
                        "grids as large as you want.  Large grids may lock up or crash " +
                        "the program.  If the program crashes at launch, use \"Clear Data\" " +
                        "in the system Applications settings, or reinstall Lock Pattern Generator")
                       .setTitle("Notice")
                       .setCancelable(false)
                       .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                               dialog.cancel();
                           }
                       });
                dialog = builder.create();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }
}
