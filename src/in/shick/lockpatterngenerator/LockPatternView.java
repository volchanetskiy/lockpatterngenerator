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

import android.graphics.drawable.shapes.*;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Path;
import android.content.Context;
import android.view.View;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.LinearLayout.*;
import java.util.Queue;
import java.util.Iterator;
import java.util.LinkedList;

public class LockPatternView extends View
{
    public static final double OUTER_SIZE_RATIO = 0.75;
    public static final double MIDDLE_SIZE_RATIO = 0.90;
    public static final double INNER_SIZE_RATIO = 0.33;

    public static final int SELECTED_COLOR = 0xff00dd00;
    public static final int FIRST_SELECTED_COLOR = 0xff00ddcc;
    public static final int UNSELECTED_COLOR = 0xff999999;
    public static final int MANTLE_COLOR = 0xff000000;
    public static final int CORE_COLOR = 0xffffffff;
    public static final int PATH_COLOR = 0xffcccccc;

    private ShapeDrawable outerCircles[], middleCircles[], innerCircles[];
    private Paint pathPaint, arrowPaint, firstArrowPaint;
    private int gridSize;
    private int gridLength;
    private LinkedList<Point> pathPoints;
    private LinkedList<Integer> pathOrder;
    private LinkedList<Path> arrows;
    private boolean highlightFirst;

    private int outerDiameter;
    private int middleDiameter;
    private int innerDiameter;

    private int outerOffset;
    private int middleOffset;
    private int innerOffset;

    private int arrowPointRadius;
    private int arrowBaseRadius;
    private int arrowBaseHalfLength;

    public LockPatternView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);

        gridSize = 3;
        gridLength = 0;

        highlightFirst = false;

        pathOrder = new LinkedList<Integer>();
        pathPoints = new LinkedList<Point>();

        pathPaint = new Paint();
        pathPaint.setColor(PATH_COLOR);

        firstArrowPaint = new Paint();
        firstArrowPaint.setColor(FIRST_SELECTED_COLOR);
        firstArrowPaint.setStyle(Paint.Style.FILL);

        arrowPaint = new Paint();
        arrowPaint.setColor(SELECTED_COLOR);
        arrowPaint.setStyle(Paint.Style.FILL);

        updateDrawableNodes();
    }

    public void updateDrawableNodes()
    {
        outerCircles = new ShapeDrawable[gridSize*gridSize];
        middleCircles = new ShapeDrawable[gridSize*gridSize];
        innerCircles = new ShapeDrawable[gridSize*gridSize];

        for(int i = 0; i < gridSize*gridSize; i++)
        {
            outerCircles[i] = new ShapeDrawable(new OvalShape());
            outerCircles[i].getPaint().setColor(UNSELECTED_COLOR);
            middleCircles[i] = new ShapeDrawable(new OvalShape());
            middleCircles[i].getPaint().setColor(MANTLE_COLOR);
            innerCircles[i] = new ShapeDrawable(new OvalShape());
            innerCircles[i].getPaint().setColor(CORE_COLOR);
        }

        outerDiameter = (int) ((double)(gridLength / gridSize) * OUTER_SIZE_RATIO);
        middleDiameter = (int) ((double)outerDiameter * MIDDLE_SIZE_RATIO);
        innerDiameter = (int) ((double)outerDiameter * INNER_SIZE_RATIO);

        outerOffset = gridLength / (gridSize*2) - outerDiameter/2;
        middleOffset = gridLength / (gridSize*2) - middleDiameter/2;
        innerOffset = gridLength / (gridSize*2) - innerDiameter/2;

        arrowPointRadius = (int) ((double)middleDiameter/2*0.9);
        arrowBaseRadius = (int) ((double)middleDiameter/2*0.6);
        arrowBaseHalfLength = (int) ((double)middleDiameter/2*0.3);


        pathPaint.setStrokeWidth(innerDiameter);


        for(int i = 0; i < gridSize; i++)
        {
            for(int j = 0; j < gridSize; j++)
            {
                int curX = j*gridLength/gridSize, curY = i*gridLength/gridSize;

                outerCircles[gridSize*i+j].setBounds(curX+outerOffset,curY+outerOffset,curX+outerOffset+outerDiameter,curY+outerOffset+outerDiameter);
                middleCircles[gridSize*i+j].setBounds(curX+middleOffset,curY+middleOffset,curX+middleOffset+middleDiameter,curY+middleOffset+middleDiameter);
                innerCircles[gridSize*i+j].setBounds(curX+innerOffset,curY+innerOffset,curX+innerOffset+innerDiameter,curY+innerOffset+innerDiameter);
            }
        }
    }

    public void refreshPath()
    {
        updatePath(pathOrder);
    }

    public void updatePath(Queue<Integer> lockPattern)
    {
        pathOrder = new LinkedList<Integer>();
        pathPoints = new LinkedList<Point>();
        Iterator<Integer> pathIterator = lockPattern.iterator();

        for(ShapeDrawable e : outerCircles)
            e.getPaint().setColor(UNSELECTED_COLOR);

        if(pathIterator.hasNext())
        {
            int e = pathIterator.next().intValue();
            if(highlightFirst)
                outerCircles[e].getPaint().setColor(FIRST_SELECTED_COLOR);
            else
                outerCircles[e].getPaint().setColor(SELECTED_COLOR);
            pathOrder.offer(new Integer(e));
        }

        while(pathIterator.hasNext())
        {
            int e = pathIterator.next().intValue();
            outerCircles[e].getPaint().setColor(SELECTED_COLOR);
            pathOrder.offer(new Integer(e));
        }

        int nodeA = 0, nodeB = 0;
        int startX = 0, startY = 0, endX = 0, endY = 0;
        pathIterator = lockPattern.iterator();

        if(lockPattern.size() > 1)
        {
            nodeA = pathIterator.next();
            nodeB = pathIterator.next();

            startX = (nodeA%gridSize)*gridLength/gridSize + (gridLength / (gridSize*2));
            startY = (nodeA/gridSize)*gridLength/gridSize + (gridLength / (gridSize*2));
            endX = (nodeB%gridSize)*gridLength/gridSize + (gridLength / (gridSize*2));
            endY = (nodeB/gridSize)*gridLength/gridSize + (gridLength / (gridSize*2));

            pathPoints.offer(new Point(startX,startY));
            pathPoints.offer(new Point(endX,endY));

            while(pathIterator.hasNext())
            {
                nodeA = nodeB;
                nodeB = pathIterator.next();

                startX = (nodeA%gridSize)*gridLength/gridSize + (gridLength / (gridSize*2));
                startY = (nodeA/gridSize)*gridLength/gridSize + (gridLength / (gridSize*2));
                endX = (nodeB%gridSize)*gridLength/gridSize + (gridLength / (gridSize*2));
                endY = (nodeB/gridSize)*gridLength/gridSize + (gridLength / (gridSize*2));

                pathPoints.offer(new Point(startX,startY));
                pathPoints.offer(new Point(endX,endY));
            }
        }

        invalidate();
    }

    protected void onDraw(Canvas canvas)
    {
        Point pointA, pointB;
        Iterator<Point> points = pathPoints.iterator();
        arrows = new LinkedList<Path>();
        double angle;
        float centerlineX, centerlineY, pointX, pointY;
        Path arrow;

        while(points.hasNext())
        {
            pointA = points.next();
            if(points.hasNext())
            {
                pointB = points.next();

                angle = Math.atan2(pointA.y-pointB.y,pointA.x-pointB.x);

                pointX = pointA.x-(float)(Math.cos(angle)*arrowPointRadius);
                pointY = pointA.y-(float)(Math.sin(angle)*arrowPointRadius);

                centerlineX = pointA.x-(float)(Math.cos(angle)*arrowBaseRadius);
                centerlineY = pointA.y-(float)(Math.sin(angle)*arrowBaseRadius);

                arrow = new Path();
                arrow.moveTo(pointX,pointY);
                arrow.lineTo(centerlineX-(float)(arrowBaseHalfLength * Math.cos(angle + Math.PI/2)), centerlineY-(float)(arrowBaseHalfLength * Math.sin(angle + Math.PI/2)));
                arrow.lineTo(centerlineX-(float)(arrowBaseHalfLength * Math.cos(angle - Math.PI/2)), centerlineY-(float)(arrowBaseHalfLength * Math.sin(angle - Math.PI/2)));
                arrow.lineTo(pointX,pointY);
                arrows.offer(arrow);

                canvas.drawLine(pointA.x,pointA.y,pointB.x,pointB.y,pathPaint);
            }
        }

        for(ShapeDrawable e : outerCircles)
            e.draw(canvas);
        for(ShapeDrawable e : middleCircles)
            e.draw(canvas);
        for(ShapeDrawable e : innerCircles)
            e.draw(canvas);
        for(int i = 0; i < arrows.size(); i++)
        {
            if(i == 0 && highlightFirst)
                canvas.drawPath(arrows.get(i),firstArrowPaint);
            else
                canvas.drawPath(arrows.get(i),arrowPaint);
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        outerDiameter = (int) ((double)(w / gridSize) * OUTER_SIZE_RATIO);
        middleDiameter = (int) ((double)outerDiameter * MIDDLE_SIZE_RATIO);
        innerDiameter = (int) ((double)outerDiameter * INNER_SIZE_RATIO);

        outerOffset = w / (gridSize*2) - outerDiameter/2;
        middleOffset = w / (gridSize*2) - middleDiameter/2;
        innerOffset = w / (gridSize*2) - innerDiameter/2;

        arrowPointRadius = (int) ((double)middleDiameter/2*0.9);
        arrowBaseRadius = (int) ((double)middleDiameter/2*0.6);
        arrowBaseHalfLength = (int) ((double)middleDiameter/2*0.3);

        gridLength = w;

        updateDrawableNodes();

        invalidate();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int value = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(value,value);
    }

    /* HO HUM Accessors/Mutators */

    public int getGridSize()
    {
        return gridSize;
    }

    public void setGridSize(int input)
    {
        gridSize = input;
        updateDrawableNodes();
    }

    public void setHighlight(boolean input)
    {
        highlightFirst = input;
    }
}
