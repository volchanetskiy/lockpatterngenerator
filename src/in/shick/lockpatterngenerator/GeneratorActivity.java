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

import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class GeneratorActivity extends BaseActivity
{
    protected LockPatternView mPatternView;
    protected Button mGenerateButton;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // find views
        setContentView(R.layout.generator_activity);
        mPatternView = (LockPatternView) findViewById(R.id.pattern_view);
        mGenerateButton = (Button) findViewById(R.id.generate_button);

        // set up views
        mGenerateButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: remove
                ArrayList<Point> testPattern = new ArrayList<Point>();
                testPattern.add(new Point(0,1));
                testPattern.add(new Point(1,2));
                testPattern.add(new Point(2,1));
                testPattern.add(new Point(1,0));
                mPatternView.setPattern(testPattern);
                mPatternView.invalidate();
            }
        });
    }
}
