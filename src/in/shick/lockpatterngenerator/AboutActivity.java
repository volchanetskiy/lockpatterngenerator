package in.shick.lockpatterngenerator;

import java.io.InputStream;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Html;
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
    }
}
