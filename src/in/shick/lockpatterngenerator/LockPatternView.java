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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.*;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

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
    public static final int INCORRECT_COLOR = 0xffdd1111;
    public static final int CORRECT_COLOR = 0xff1111ff;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Handler handler;

    private ShapeDrawable outerCircles[], middleCircles[], innerCircles[];
    private Paint pathPaint, arrowPaint, firstArrowPaint;
    private int gridSize;
    private int gridLength;
    private LinkedList<Point> pathPoints;
    private LinkedList<Integer> pathOrder;
    private LinkedList<Path> arrows;
    private Queue<Integer> normalPath;
    private Queue<Integer> practicePath;
    private boolean highlightFirst;
    private boolean practiceMode;
    private int wildX, wildY;
    private int activeColor;
    private boolean showingResult;
    private int displayDelay;

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

        handler = new Handler();
        displayDelay = 1000;

        gridSize = 3;
        gridLength = 0;

        highlightFirst = false;
        practiceMode = false;
        showingResult = false;

        wildX = -1;
        wildY = -1;

        activeColor = SELECTED_COLOR;

        practicePath = new LinkedList<Integer>();
        normalPath = new LinkedList<Integer>();
        pathOrder = new LinkedList<Integer>();
        pathPoints = new LinkedList<Point>();

        pathPaint = new Paint();
        pathPaint.setColor(PATH_COLOR);

        firstArrowPaint = new Paint();
        firstArrowPaint.setColor(FIRST_SELECTED_COLOR);
        firstArrowPaint.setStyle(Paint.Style.FILL);

        arrowPaint = new Paint();
        arrowPaint.setColor(activeColor);
        arrowPaint.setStyle(Paint.Style.FILL);

        updateDrawableNodes();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(!practiceMode)
        {
            return false;
        }
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                showingResult = false;
                practicePath = new LinkedList<Integer>();
                activeColor = SELECTED_COLOR;
                updatePath(practicePath);
            case MotionEvent.ACTION_MOVE:
                float x = event.getX(), y = event.getY();
                wildX = (int) x;
                wildY = (int) y;
                int cell_length = gridLength/gridSize;
                int xblock = (int) x / (cell_length);
                if(xblock >= gridSize)
                {
                    xblock = gridSize - 1;
                }
                int yblock = (int) y / (cell_length);
                if(yblock >= gridSize)
                {
                    yblock = gridSize - 1;
                }
                int locus_x = (int) ((((float) xblock) + 0.5) * cell_length);
                int locus_y = (int) ((((float) yblock) + 0.5) * cell_length);
                int locus_dist = (int) Math.sqrt(Math.pow(x - locus_x,2) + Math.pow(y - locus_y,2));
                if(locus_dist <= middleDiameter/2)
                {
                    int nodeNum = (yblock * gridSize) + xblock;
                    if(!practicePath.contains(nodeNum))
                    {
                        practicePath.offer(nodeNum);
                        updatePath(practicePath);
                        return true;
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                wildX = -1;
                wildY = -1;
                if(practicePath.size() == 0)
                {
                    return true;
                }
                if(!practicePath.equals(normalPath))
                {
                    activeColor = INCORRECT_COLOR;
                }
                else
                {
                    activeColor = CORRECT_COLOR;
                }
                updatePath(practicePath);
                showingResult = true;
                scheduler.schedule(new Runnable() {
                        public void run()
                        {
                            handler.post(new Runnable() {
                                public void run() {
                                    if(showingResult)
                                    {
                                        activeColor = SELECTED_COLOR;
                                        showingResult = false;
                                        practicePath = new LinkedList<Integer>();
                                        updatePath(practicePath);
                                    }
                                }
                            });
                        }
                    },
                    displayDelay,
                    TimeUnit.MILLISECONDS);
                break;
            default:
                return false;
        }
        return true;
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
        pathPaint.setStrokeCap(Paint.Cap.ROUND);


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

    private void updatePath(Queue<Integer> lockPattern)
    {
        pathOrder = new LinkedList<Integer>();
        pathPoints = new LinkedList<Point>();
        Iterator<Integer> pathIterator = lockPattern.iterator();
        arrowPaint.setColor(activeColor);

        for(ShapeDrawable e : outerCircles)
            e.getPaint().setColor(UNSELECTED_COLOR);

        if(pathIterator.hasNext())
        {
            int e = pathIterator.next().intValue();
            if(highlightFirst && !practiceMode)
                outerCircles[e].getPaint().setColor(FIRST_SELECTED_COLOR);
            else
                outerCircles[e].getPaint().setColor(activeColor);
            pathOrder.offer(new Integer(e));
        }

        while(pathIterator.hasNext())
        {
            int e = pathIterator.next().intValue();
            outerCircles[e].getPaint().setColor(activeColor);
            pathOrder.offer(new Integer(e));
        }

        int nodeA = 0, nodeB = 0;
        int startX = 0, startY = 0, endX = 0, endY = 0;
        pathIterator = lockPattern.iterator();

        if(lockPattern.size() > 0)
        {
            nodeA = pathIterator.next();
            startX = (nodeA%gridSize)*gridLength/gridSize + (gridLength / (gridSize*2));
            startY = (nodeA/gridSize)*gridLength/gridSize + (gridLength / (gridSize*2));
            pathPoints.offer(new Point(startX,startY));

            if(pathIterator.hasNext())
            {
                nodeB = pathIterator.next();
                endX = (nodeB%gridSize)*gridLength/gridSize + (gridLength / (gridSize*2));
                endY = (nodeB/gridSize)*gridLength/gridSize + (gridLength / (gridSize*2));
                pathPoints.offer(new Point(endX,endY));
            }

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
                if(!points.hasNext() && wildX >= 0 && wildY >= 0)
                {
                    canvas.drawLine(pointB.x,pointB.y,wildX,wildY,pathPaint);
                }
            }
            else if(wildX >= 0 && wildY >= 0)
            {
                canvas.drawLine(pointA.x,pointA.y,wildX,wildY,pathPaint);
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
            if(i == 0 && highlightFirst && !practiceMode)
                canvas.drawPath(arrows.get(i),firstArrowPaint);
            else
                canvas.drawPath(arrows.get(i),arrowPaint);
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        outerDiameter = (int) ((double)(gridLength / gridSize) * OUTER_SIZE_RATIO);
        middleDiameter = (int) ((double)outerDiameter * MIDDLE_SIZE_RATIO);
        innerDiameter = (int) ((double)outerDiameter * INNER_SIZE_RATIO);

        outerOffset = gridLength / (gridSize*2) - outerDiameter/2;
        middleOffset = gridLength / (gridSize*2) - middleDiameter/2;
        innerOffset = gridLength / (gridSize*2) - innerDiameter/2;

        arrowPointRadius = (int) ((double)middleDiameter/2*0.9);
        arrowBaseRadius = (int) ((double)middleDiameter/2*0.6);
        arrowBaseHalfLength = (int) ((double)middleDiameter/2*0.3);

        updateDrawableNodes();

        invalidate();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec); 
        int height = MeasureSpec.getSize(heightMeasureSpec); 
        int value = Math.min(width,height);
        if(width == 0)
        {
            value = height;
        }
        else if(height == 0)
        {
            value = width;
        }
        gridLength = value;
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

    public void setPracticeMode(boolean input)
    {
        practiceMode = input;
        if(!practiceMode)
        {
            activeColor = SELECTED_COLOR;
            updatePath(normalPath);
            showingResult = false;
            practicePath = new LinkedList<Integer>();
        }
        else
        {
            updatePath(practicePath);
        }
    }

    public void setPath(Queue<Integer> path)
    {
        normalPath = path;
        if(!practiceMode)
        {
            updatePath(normalPath);
        }
    }
}
