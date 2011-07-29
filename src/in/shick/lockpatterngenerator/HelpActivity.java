package in.shick.lockpatterngenerator;

import java.io.InputStream;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

public class HelpActivity extends Activity
{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView body = new TextView(this);
        body.setPadding(10,10,10,10);
        ScrollView scroller = new ScrollView(this);
        scroller.addView(body);
        setContentView(scroller);

        AssetManager assetManager = this.getAssets();
        String htmlString = "Error reading help file";
        String htmlFile = "help.html";
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
    }
}
