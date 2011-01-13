package in.shick.lockpatterngenerator;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.preference.PreferenceManager;
import java.util.LinkedList;
import java.util.Random;

public class GenLockPattern extends Activity
{
    private LockPatternView outputView;
    private SharedPreferences generationPrefs;
    private Random r;
    private int gridSize = 3;
    private int minNodes = 4;
    private int maxNodes = 6;
    private boolean highlightFirstNode = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        outputView = (LockPatternView) this.findViewById(R.id.patternOutputView);
        r = new Random();

        generationPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        gridSize = Integer.parseInt(generationPrefs.getString("gridSizePref","3").trim());
        minNodes = Integer.parseInt(generationPrefs.getString("minLengthPref","4").trim());
        maxNodes = Integer.parseInt(generationPrefs.getString("maxLengthPref","6").trim());
        highlightFirstNode = generationPrefs.getBoolean("firstNodePref",false);

        outputView.setGridSize(gridSize);
        outputView.setHighlight(highlightFirstNode);

        final Button generateButton = (Button) this.findViewById(R.id.generateButton);
        final Button settingsButton = (Button) this.findViewById(R.id.settingsButton);
        final GenLockPattern toastAnchor = this;

        generateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                toastAnchor.generateLockPattern();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent walkIntoMordor = new Intent();
                walkIntoMordor.setClassName("in.shick.lockpatterngenerator", "in.shick.lockpatterngenerator.PreferencesActivity");
                startActivity(walkIntoMordor);
            }
        });
    }

    public void updateSettings()
    {
        int newGridSize = Integer.parseInt(generationPrefs.getString("gridSizePref","3").trim());
        int newMinNodes = Integer.parseInt(generationPrefs.getString("minLengthPref","4").trim());
        int newMaxNodes = Integer.parseInt(generationPrefs.getString("maxLengthPref","6").trim());

        boolean newHighlightFirstNode = generationPrefs.getBoolean("firstNodePref",false);

        if(newMinNodes < 1)
        {
            newMinNodes = 1;
            generationPrefs.edit().putString("minLengthPref","1").commit();
        }
        if(newMinNodes > newGridSize*newGridSize)
        {
            newMinNodes = newGridSize*newGridSize;
            generationPrefs.edit().putString("minLengthPref",Integer.toString(newMinNodes)).commit();
        }

        if(newMaxNodes < 1)
        {
            newMaxNodes = 1;
            generationPrefs.edit().putString("maxLengthPref","1").commit();
        }
        if(newMaxNodes > newGridSize*newGridSize)
        {
            newMaxNodes = newGridSize*newGridSize;
            generationPrefs.edit().putString("maxLengthPref",Integer.toString(newMaxNodes)).commit();
        }

        if(newMinNodes > newMaxNodes)
        {
            newMaxNodes = newMinNodes;
            generationPrefs.edit().putString("maxLengthPref",Integer.toString(newMaxNodes)).commit();
        }

        if(newGridSize != gridSize || newMinNodes != minNodes || newMaxNodes != maxNodes)
            outputView.updatePath(new LinkedList<Integer>());

        highlightFirstNode = newHighlightFirstNode;
        gridSize = newGridSize;
        minNodes = newMinNodes;
        maxNodes = newMaxNodes;

        outputView.setGridSize(gridSize);
        outputView.setHighlight(highlightFirstNode);

        outputView.updateDrawableNodes();
    }

    public void onResume()
    {
        super.onResume();
        updateSettings();
        outputView.refreshPath();
        outputView.invalidate();
    }

    public void generateLockPattern()
    {
        /* path must implement java.util.Queue or else! */
        LinkedList<Integer> path = new LinkedList<Integer>();
        LinkedList<Integer> opts = new LinkedList<Integer>();
        int numNodes = r.nextInt(maxNodes-minNodes+1)+minNodes, currentNum = 0, rise = 0, run = 0, gcd = 0, lastNum = 0;

        for(int i = 0; i < gridSize*gridSize; i++)
            opts.offer(new Integer(i));

        currentNum = r.nextInt(gridSize*gridSize);
        path.offer(new Integer(currentNum));
        opts.remove(new Integer(currentNum));
        lastNum = currentNum;

        for(int i = 1; i < numNodes; i++)
        {
            if(opts.size() < 1)
                break;
            do
            {
                currentNum = opts.get(r.nextInt(opts.size())).intValue();
                rise = (currentNum / gridSize) - (lastNum / gridSize);
                run = (currentNum % gridSize) - (lastNum % gridSize);
                /* rise = r.nextInt(gridSize) - (lastNum / gridSize); */
                /* run = r.nextInt(gridSize) - (lastNum % gridSize); */

                gcd = Math.abs(computeGcd(rise, run));

                if(gcd != 0)
                {
                    rise /= gcd;
                    run /= gcd;
                    for(int j = 1;j <= gcd; j++)
                    {
                        System.out.println("rise: " + rise + " run: " + run + " gcd: " + gcd + " j: " + j);
                        System.out.println("test for: " + new Integer (lastNum + rise * j * gridSize + run * j));
                        if(!path.contains(new Integer (lastNum + rise * j * gridSize + run * j)))
                        {
                            System.out.println("available!");
                            rise *= j;
                            run *= j;
                            break;
                        }
                    }
                }

                currentNum = lastNum + rise * gridSize + run;

                System.out.print("derp. optss:" + opts.size() + " cur:" + currentNum + " path: ");
                for(Integer e : path)
                    System.out.print(e.intValue() + " ");
                System.out.print("opts: ");
                for(Integer e : opts)
                    System.out.print(e.intValue() + " ");
                System.out.println();

            }while(path.contains(new Integer(currentNum)));

            path.offer(new Integer(currentNum));
            opts.remove(new Integer(currentNum));
            lastNum = currentNum;

        }

        outputView.updatePath(path);
        return;
    }

    private int computeGcd(int a, int b)
    /* Implementation taken from
     * http://en.literateprograms.org/Euclidean_algorithm_(Java)
     * Accessed on 12/28/10
     */
    {
        if(b > a)
        {
            int temp = a;
            a = b;
            b = temp;
        }

        while(b != 0)
        {
            int m = a % b;
            a = b;
            b = m;
        }

        return a;
    }
}
