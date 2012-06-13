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

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class NodeDrawable extends Drawable
{
    public static final int STATE_UNSELECTED = 0, STATE_SELECTED = 1,
           STATE_HIGHLIGHTED = 2, STATE_CORRECT = 3, STATE_INCORRECT = 4;
    public static final int[] DEFAULT_STATE_COLORS = {
        0xff999999, 0xff00cc00, 0xff00cccc, 0xff1111ff, 0xffdd1111
    };

    public static final int PART_COUNT = 3;
    public static final int PART_OUTER = 0, PART_MIDDLE = 1, PART_INNER = 2;
    public static final float[] PART_RATIOS = {
        1.0f, 0.9f, 0.33f
    };
    public static final int[] DEFAULT_PART_COLORS = {
        DEFAULT_STATE_COLORS[STATE_UNSELECTED], 0xff000000, 0xffffffff
    };
    public static final int[] PART_ORDER = {
        PART_OUTER, PART_MIDDLE, PART_INNER
    };

    // For drawing an arrow exit indicator
    protected float mArrowTipRad, mArrowBaseRad, mArrowHalfBase;

    protected ShapeDrawable mCircles[];
    protected Paint mExitPaint;
    protected Path mExitIndicator;
    protected float mExitAngle;
    protected Point mCenter;
    protected float mDiameter;
    protected int mState;

    public NodeDrawable(float diameter, Point center)
    {
        mCircles = new ShapeDrawable[PART_COUNT];
        mCenter = center;
        mDiameter = diameter;
        mState = STATE_UNSELECTED;
        mExitAngle = Float.NaN;

        mExitPaint = new Paint();
        mExitPaint.setStyle(Paint.Style.FILL);

        buildShapes(diameter, center);
    }

    @Override
    public void draw(Canvas canvas)
    {
        for(int ii = 0; ii < PART_COUNT; ii++)
        {
            mCircles[PART_ORDER[ii]].draw(canvas);
        }
        if(!Float.isNaN(mExitAngle))
        {
            canvas.drawPath(mExitIndicator, mExitPaint);
        }
    }

    private void buildShapes(float outerDiameter, Point center)
    {
        for(int ii = 0; ii < PART_COUNT; ii++)
        {
            mCircles[ii] = new ShapeDrawable(new OvalShape());
            mCircles[ii].getPaint().setColor(DEFAULT_PART_COLORS[ii]);

            float diameter = outerDiameter * PART_RATIOS[ii];
            int offset = (int) (diameter / 2.0f);

            mCircles[ii].setBounds(center.x - offset, center.y - offset,
                    center.x + offset, center.y + offset);
        }

        // crunch variables for exit arrows independent of angle
        float middleDiameter = outerDiameter * PART_RATIOS[PART_MIDDLE];

        mArrowTipRad = middleDiameter / 2.0f * 0.9f;
        mArrowBaseRad = middleDiameter / 2.0f * 0.6f;
        mArrowHalfBase = middleDiameter / 2.0f * 0.3f;
    }

    //
    // Accessors / mutators
    //

    public void setNodeState(int state)
    {
        mCircles[PART_OUTER].getPaint().setColor(DEFAULT_STATE_COLORS[state]);
        mExitPaint.setColor(DEFAULT_STATE_COLORS[state]);
        if(state == STATE_UNSELECTED)
        {
            setExitAngle(Float.NaN);
        }
        mState = state;
    }
    public int getNodeState()
    {
        return mState;
    }

    public void setExitAngle(float angle)
    {
        // construct exit indicator arrow
        if(!Float.isNaN(angle))
        {
            float tipX = mCenter.x - ((float) Math.cos(angle)) * mArrowTipRad;
            float tipY = mCenter.y - ((float) Math.sin(angle)) * mArrowTipRad;

            float baseCenterX = mCenter.x
                - ((float) Math.cos(angle)) * mArrowBaseRad;
            float baseCenterY = mCenter.y
                - ((float) Math.sin(angle)) * mArrowBaseRad;

            // first base vertex of arrow
            float baseVertAX = baseCenterX
                - mArrowHalfBase * ((float) Math.cos(angle + Math.PI / 2));
            float baseVertAY = baseCenterY
                - mArrowHalfBase * ((float) Math.sin(angle + Math.PI / 2));
            // second base vertex of arrow
            float baseVertBX = baseCenterX
                - mArrowHalfBase * ((float) Math.cos(angle - Math.PI / 2));
            float baseVertBY = baseCenterY
                - mArrowHalfBase * ((float) Math.sin(angle - Math.PI / 2));

            Path arrow = new Path();
            arrow.moveTo(tipX, tipY);
            arrow.lineTo(baseVertAX, baseVertAY);
            arrow.lineTo(baseVertBX, baseVertBY);
            arrow.lineTo(tipX, tipY);

            mExitIndicator = arrow;
        }
        mExitAngle = angle;
    }
    public float getExitAngle()
    {
        return mExitAngle;
    }

    public Point getCenter()
    {
        return mCenter;
    }

    //
    // Required methods for a Drawable, generally just phoning it in to the
    // child drawables
    //

    @Override
    public int getOpacity()
    {
        return mCircles[PART_OUTER].getOpacity();
    }

    @Override
    public void setAlpha(int alpha)
    {
        for(int ii = 0; ii < PART_COUNT; ii++)
        {
            mCircles[ii].setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(android.graphics.ColorFilter cf)
    {
        for(int ii = 0; ii < PART_COUNT; ii++)
        {
            mCircles[ii].setColorFilter(cf);
        }
    }
}
