/*
Copyright 2010-2012 Michael Shick

This file is part of 'Lock Pattern Generator'.

'Lock Pattern Generator' is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or (at your option)
any later version.

'Lock Pattern Generator' is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
'Lock Pattern Generator'.  If not, see <http://www.gnu.org/licenses/>.
*/
package in.shick.lockpatterngenerator;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

public class GeneratorActivity extends BaseActivity
{
    protected LockPatternView mPatternView;
    protected Button mGenerateButton;
    protected Button mSecuritySettingsButton;
    protected ToggleButton mPracticeToggle;
    protected PatternGenerator mGenerator;
    protected int mGridLength;
    protected int mPatternMin;
    protected int mPatternMax;
    protected String mHighlightMode;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // non-UI setup
        mGenerator = new PatternGenerator();

        // find views
        setContentView(R.layout.generator_activity);
        mPatternView = (LockPatternView) findViewById(R.id.pattern_view);
        mGenerateButton = (Button) findViewById(R.id.generate_button);
        mSecuritySettingsButton =
            (Button) findViewById(R.id.security_settings_button);
        mPracticeToggle = (ToggleButton) findViewById(R.id.practice_toggle);

        // set up views
        mGenerateButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPatternView.setPattern(mGenerator.getPattern());
                mPatternView.invalidate();
            }
        });

        mSecuritySettingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                try
                {
                    startActivity(
                        new Intent(Settings.ACTION_SECURITY_SETTINGS));
                }
                catch(android.content.ActivityNotFoundException e)
                {
                    Toast.makeText(GeneratorActivity.this,
                        getString(R.string.settings_shortcut_failure),
                        Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void onResume()
    {
        super.onResume();
        
        updateFromPrefs();
    }

    private void updateFromPrefs()
    {
        int gridLength =
            mPreferences.getInt("grid_length", Defaults.GRID_LENGTH);
        int patternMin =
            mPreferences.getInt("pattern_min", Defaults.PATTERN_MIN);
        int patternMax =
            mPreferences.getInt("pattern_max", Defaults.PATTERN_MAX);
        String highlightMode =
            mPreferences.getString("highlight_mode", Defaults.HIGHLIGHT_MODE);

        // sanity checking
        if(gridLength < 1)
        {
            gridLength = 1;
        }
        if(patternMin < 1)
        {
            patternMin = 1;
        }
        if(patternMax < 1)
        {
            patternMax = 1;
        }
        int nodeCount = (int) Math.pow(gridLength, 2);
        if(patternMin > nodeCount)
        {
            patternMin = nodeCount;
        }
        if(patternMax > nodeCount)
        {
            patternMax = nodeCount;
        }
        if(patternMin > patternMax)
        {
            patternMin = patternMax;
        }

        // only update values that differ
        if(gridLength != mGridLength)
        {
            setGridLength(gridLength);
        }
        if(patternMax != mPatternMax)
        {
            setPatternMax(patternMax);
        }
        if(patternMin != mPatternMin)
        {
            setPatternMin(patternMin);
        }
        if(!highlightMode.equals(mHighlightMode))
        {
            setHighlightMode(highlightMode);
        }
    }

    private void setGridLength(int length)
    {
        mGenerator.setGridLength(length);
        mPatternView.setGridLength(length);
    }
    private void setPatternMin(int nodes)
    {
        mPatternMin = nodes;
        mGenerator.setMinNodes(nodes);
    }
    private void setPatternMax(int nodes)
    {
        mPatternMax = nodes;
        mGenerator.setMaxNodes(nodes);
    }
    private void setHighlightMode(String mode)
    {
        if("no".equals(mode))
        {
            mPatternView.setHighlightMode(new LockPatternView.NoHighlight());
        }
        else if("first".equals(mode))
        {
            mPatternView.setHighlightMode(new LockPatternView.FirstHighlight());
        }
        else if("rainbow".equals(mode))
        {
            mPatternView.setHighlightMode(
                    new LockPatternView.RainbowHighlight());
        }

        mHighlightMode = mode;
    }
}
