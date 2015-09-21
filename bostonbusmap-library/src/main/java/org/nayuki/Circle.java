package org.nayuki;

import java.util.Collection;

/*
 * Smallest enclosing circle
 *
 * Copyright (c) 2014 Project Nayuki
 * http://www.nayuki.io/page/smallest-enclosing-circle
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING.txt).
 * If not, see <http://www.gnu.org/licenses/>.
 */
public class Circle {

    private static double EPSILON = 1e-12;


    public final Point c;   // Center
    public final double r;  // Radius


    public Circle(Point c, double r) {
        this.c = c;
        this.r = r;
    }


    public boolean contains(Point p) {
        return c.distance(p) <= r + EPSILON;
    }


    public boolean contains(Collection<Point> ps) {
        for (Point p : ps) {
            if (!contains(p))
                return false;
        }
        return true;
    }


    public String toString() {
        return String.format("Circle(x=%g, y=%g, r=%g)", c.x, c.y, r);
    }

}
