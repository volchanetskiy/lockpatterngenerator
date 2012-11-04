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

import in.shick.lockpatterngenerator.external.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PatternGenerator
{
    protected int mGridLength;
    protected int mMinNodes;
    protected int mMaxNodes;
    protected Random mRng;
    protected List<Point> mAllNodes;

    public PatternGenerator()
    {
        mRng = new Random();
        setGridLength(0);
        setMinNodes(0);
        setMaxNodes(0);
    }

    public List<Point> getPattern()
    {
        List<Point> pattern = new ArrayList<Point>();
        if(mMaxNodes < 1)
        {
            return pattern;
        }
        // list for random access, set for fast membership testing
        List<Point> nodeAvailList = new ArrayList<Point>(mAllNodes);
        int pathMaxLen = (int) Math.min(mMaxNodes, Math.pow(mGridLength, 2));
        int pathLen = mRng.nextInt(pathMaxLen - mMinNodes + 1) + mMinNodes;

        Point tail = nodeAvailList.remove(mRng.nextInt(nodeAvailList.size()));
        pattern.add(tail);
        for(int ii = 1; ii < pathLen; ii++)
        {
            // build list of possible points
            List<Point> nodeCandidatesList = new ArrayList<Point>(nodeAvailList);
            for(int i = 0; i < nodeCandidatesList.size(); i++)
            {
                Point delta = new Point(nodeCandidatesList.get(i).x - tail.x, nodeCandidatesList.get(i).y - tail.y);
                // remove this point if there is other unused point between this one and "tail"
                int gcd = Math.abs(computeGcd(delta.x, delta.y));
                if(gcd > 1)
                {
                    for(int j = 1; j < gcd; j++)
                    {
                        Point inside = new Point(tail.x + delta.x / gcd * j,tail.y + delta.y / gcd * j);
                        if(nodeAvailList.contains(inside))
                        {
                            nodeCandidatesList.remove(i);
                            i--;
                            break;
                        }
                    }
                }
            }
            Point next =
                    nodeCandidatesList.get(mRng.nextInt(nodeCandidatesList.size()));
            // remove from consideration and add to pattern
            nodeAvailList.remove(next);
            pattern.add(next);
            tail = next;
        }
        return pattern;
    }

    //
    // Accessors / Mutators
    //

    public void setGridLength(int length)
    {
        // build the prototype set to copy from later
        List<Point> allNodes = new ArrayList<Point>();
        for(int y = 0; y < length; y++)
        {
            for(int x = 0; x < length; x++)
            {
                allNodes.add(new Point(x,y));
            }
        }
        mAllNodes = allNodes;

        mGridLength = length;
    }
    public int getGridLength()
    {
        return mGridLength;
    }

    public void setMinNodes(int nodes)
    {
        mMinNodes = nodes;
    }
    public int getMinNodes()
    {
        return mMinNodes;
    }

    public void setMaxNodes(int nodes)
    {
        mMaxNodes = nodes;
    }
    public int getMaxNodes()
    {
        return mMaxNodes;
    }

    //
    // Helper methods
    //

    public int computeGcd(int a, int b)
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
