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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

public class LockPatternView extends View
{
    public static final int DEFAULT_LENGTH_PX = 100, DEFAULT_LENGTH_NODES = 3;
    public static final float CELL_NODE_RATIO = 0.75f;

    protected PatternProvider mPatternProvider;
    protected int mLengthPx;
    protected int mLengthNodes;
    protected int mCellLength;
    protected Drawable[][] mNodeDrawables;

    public LockPatternView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        mLengthPx = DEFAULT_LENGTH_PX;
        mLengthNodes = DEFAULT_LENGTH_NODES;
        mNodeDrawables = new Drawable[0][0];
    }

    // prep to save time in onDraw calls
    private void buildDrawables()
    {
        mNodeDrawables = new Drawable[mLengthNodes][mLengthNodes];

        float nodeDiameter = ((float) mCellLength) * CELL_NODE_RATIO;
        int cellHalf = mCellLength / 2;

        for(int y = 0; y < mLengthNodes; y++)
        {
            for(int x = 0; x < mLengthNodes; x++)
            {
                Point center = new Point(x * mCellLength + cellHalf,
                        y * mCellLength + cellHalf);
                mNodeDrawables[x][y] = new NodeDrawable(nodeDiameter, center);
            }
        }
    }

    // called whenever either the actual drawn length or the nodewise length
    // changes
    private void crunchCellLength()
    {
        mCellLength = mLengthPx / mLengthNodes;
    }

    //
    // android.view.View overrides
    //

    @Override
    protected void onDraw(Canvas canvas)
    {
        for(int y = 0; y < mLengthNodes; y++)
        {
            for(int x = 0; x < mLengthNodes; x++)
            {
                mNodeDrawables[x][y].draw(canvas);
            }
        }
    }

    // expand to be as large as the smallest dictated size, or to the default
    // length if both dimensions are unspecified
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int length = 0;
        int width = View.MeasureSpec.getSize(widthMeasureSpec); 
        int wMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec); 
        int hMode = View.MeasureSpec.getMode(heightMeasureSpec);

        if(wMode == View.MeasureSpec.UNSPECIFIED
                && hMode == View.MeasureSpec.UNSPECIFIED)
        {
            length = DEFAULT_LENGTH_PX;
            setMeasuredDimension(length, length);
        }
        else if(wMode == View.MeasureSpec.UNSPECIFIED)
        {
            length = height;
        }
        else if(hMode == View.MeasureSpec.UNSPECIFIED)
        {
            length = width;
        }
        else
        {
            length = Math.min(width,height);
        }

        setMeasuredDimension(length,length);
    }

    // update draw values dependent on view size so it doesn't have to happen
    // in every onDraw()
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        mLengthPx = Math.min(w,h);
        crunchCellLength();
        buildDrawables();
    }

    //
    // Accessors / Mutators
    //

    public PatternProvider getPatternProvider()
    {
        return mPatternProvider;
    }
    public void setPatternProvider(PatternProvider provider)
    {
        mPatternProvider = provider;
    }
}
