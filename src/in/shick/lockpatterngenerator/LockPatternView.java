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
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LockPatternView extends View
{
    public static final int DEFAULT_LENGTH_PX = 100, DEFAULT_LENGTH_NODES = 3;
    public static final float CELL_NODE_RATIO = 0.75f, NODE_EDGE_RATIO = 0.33f;
    public static final int EDGE_COLOR = 0xffcccccc;

    protected int mLengthPx;
    protected int mLengthNodes;
    protected int mCellLength;
    protected NodeDrawable[][] mNodeDrawables;
    protected Paint mEdgePaint;

    protected List<Point> mCurrentPattern;

    public LockPatternView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        mLengthPx = DEFAULT_LENGTH_PX;
        mLengthNodes = DEFAULT_LENGTH_NODES;
        mNodeDrawables = new NodeDrawable[0][0];
        mCurrentPattern = Collections.emptyList();

        mEdgePaint = new Paint();
        mEdgePaint.setColor(EDGE_COLOR);
        mEdgePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    // called whenever either the actual drawn length or the nodewise length
    // changes
    private void buildDrawables()
    {
        mNodeDrawables = new NodeDrawable[mLengthNodes][mLengthNodes];

        mCellLength = mLengthPx / mLengthNodes;

        float nodeDiameter = ((float) mCellLength) * CELL_NODE_RATIO;
        mEdgePaint.setStrokeWidth(nodeDiameter * NODE_EDGE_RATIO);
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

    //
    // android.view.View overrides
    //

    @Override
    protected void onDraw(Canvas canvas)
    {
        // draw pattern edges first
        Point edgeStart, edgeEnd;
        CenterIterator patternPx =
            new CenterIterator(mCurrentPattern.iterator());

        if(patternPx.hasNext())
        {
            edgeStart = patternPx.next();
            while(patternPx.hasNext())
            {
                edgeEnd = patternPx.next();
                canvas.drawLine(edgeStart.x, edgeStart.y, edgeEnd.x, edgeEnd.y,
                        mEdgePaint);

                edgeStart = edgeEnd;
            }
        }

        // then draw nodes
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
        buildDrawables();
    }

    //
    // Accessors / Mutators
    //

    public void setPattern(List<Point> pattern)
    {
        // clear old pattern from nodes
        for(Point e : mCurrentPattern)
        {
            mNodeDrawables[e.x][e.y]
                .setNodeState(NodeDrawable.STATE_UNSELECTED);
        }
        // load new pattern into nodes
        for(int ii = 0; ii < pattern.size(); ii++)
        {
            Point e = pattern.get(ii);
            mNodeDrawables[e.x][e.y]
                .setNodeState(NodeDrawable.STATE_SELECTED);
            // if another node follows, then tell the current node which way
            // to point
            if(ii < pattern.size() - 1)
            {
                Point f = pattern.get(ii+1);
                Point centerE = mNodeDrawables[e.x][e.y].getCenter();
                Point centerF = mNodeDrawables[f.x][f.y].getCenter();

                mNodeDrawables[e.x][e.y].setExitAngle((float)
                        Math.atan2(centerE.y - centerF.y,
                            centerE.x - centerF.x));
            }
        }

        mCurrentPattern = pattern;
    }
    public List<Point> getPattern()
    {
        return mCurrentPattern;
    }

    public void setGridLength(int length)
    {
        mLengthNodes = length;
        mCurrentPattern = Collections.emptyList();
        buildDrawables();
    }
    public int getGridLength()
    {
        return mLengthNodes;
    }

    //
    // Inner classes
    //

    private class CenterIterator implements Iterator<Point>
    {
        private Iterator<Point> nodeIterator;

        public CenterIterator(Iterator<Point> iterator)
        {
            nodeIterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return nodeIterator.hasNext();
        }

        @Override
        public Point next() {
            Point node = nodeIterator.next();
            return mNodeDrawables[node.x][node.y].getCenter();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
