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

import java.io.InputStream;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity
{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        TextView body = (TextView) findViewById(R.id.text_body);

        AssetManager assetManager = this.getAssets();
        String htmlString = "Error reading about file";
        String htmlFile = "about.html";
        try
        {
            InputStream input = assetManager.open(htmlFile);
            byte bytes[] = new byte[input.available()];
            input.read(bytes);
            htmlString = new String(bytes);
        }
        catch(java.io.IOException e)
        {
        }

        body.setText(Html.fromHtml(htmlString));
        body.setMovementMethod(LinkMovementMethod.getInstance());
        body.setClickable(false);
        body.setLongClickable(false);
    }
}
