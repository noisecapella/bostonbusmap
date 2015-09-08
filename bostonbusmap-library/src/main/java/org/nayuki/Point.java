package org.nayuki;

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
public class Point {

    public final double x;
    public final double y;


    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }


    public Point subtract(Point p) {
        return new Point(x - p.x, y - p.y);
    }


    public double distance(Point p) {
        return Math.hypot(x - p.x, y - p.y);
    }


    // Signed area / determinant thing
    public double cross(Point p) {
        return x * p.y - y * p.x;
    }


    // Magnitude squared
    public double norm() {
        return x * x + y * y;
    }


    public String toString() {
        return String.format("Point(%g, %g)", x, y);
    }

}
