/*
    This file is part of RouteConverter.

    RouteConverter is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    RouteConverter is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RouteConverter; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Copyright (C) 2007 Christian Pesch. All Rights Reserved.
*/

package slash.navigation.base;

import slash.common.type.CompactCalendar;

import static slash.navigation.common.NavigationConversion.mercatorXToWgs84Longitude;
import static slash.navigation.common.NavigationConversion.mercatorYToWgs84Latitude;
import static slash.navigation.common.NavigationConversion.wgs84LatitudeToMercatorY;
import static slash.navigation.common.NavigationConversion.wgs84LongitudeToMercatorX;

/**
 * Represents a Mercator position in a route.
 *
 * @author Christian Pesch
 */

public class MercatorPosition extends BaseNavigationPosition {
    protected Long x, y;
    protected String comment;
    private Double elevation;
    private Double speed;
    private CompactCalendar time;

    public MercatorPosition(Double longitude, Double latitude, Double elevation, Double speed, CompactCalendar time, String comment) {
        this(asX(longitude), asY(latitude), elevation, speed, time, comment);
    }

    public MercatorPosition(Long x, Long y, Double elevation, Double speed, CompactCalendar time, String comment) {
        setElevation(elevation);
        setSpeed(speed);
        setTime(time);
        this.x = x;
        this.y = y;
        setComment(comment);
    }

    public Double getLongitude() {
        return x != null ? mercatorXToWgs84Longitude(x) : null;
    }

    public void setLongitude(Double longitude) {
        this.x = asX(longitude);
    }

    public Double getLatitude() {
        return y != null ? mercatorYToWgs84Latitude(y) : null;
    }

    public void setLatitude(Double latitude) {
        this.y = asY(latitude);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public CompactCalendar getTime() {
        return time;
    }

    public void setTime(CompactCalendar time) {
        this.time = time;
    }


    private static Long asX(Double longitude) {
        return longitude != null ? wgs84LongitudeToMercatorX(longitude) : null;
    }

    private static Long asY(Double latitude) {
        return latitude != null ? wgs84LatitudeToMercatorY(latitude) : null;
    }

    public Long getX() {
        return x;
    }

    public Long getY() {
        return y;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MercatorPosition that = (MercatorPosition) o;

        return !(comment != null ? !comment.equals(that.comment) : that.comment != null) &&
                !(getElevation() != null ? !getElevation().equals(that.getElevation()) : that.getElevation() != null) &&
                !(x != null ? !x.equals(that.x) : that.x != null) &&
                !(y != null ? !y.equals(that.y) : that.y != null) &&
                !(getTime() != null ? !getTime().equals(that.getTime()) : that.getTime() != null);
    }

    public int hashCode() {
        int result;
        result = (x != null ? x.hashCode() : 0);
        result = 31 * result + (y != null ? y.hashCode() : 0);
        result = 31 * result + (getElevation() != null ? getElevation().hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (getTime() != null ? getTime().hashCode() : 0);
        return result;
    }
}