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

package slash.navigation.simple;

import slash.common.type.CompactCalendar;
import slash.navigation.base.NavigationPosition;
import slash.navigation.base.RouteCharacteristics;
import slash.navigation.base.SimpleLineBasedFormat;
import slash.navigation.base.SimpleRoute;
import slash.navigation.base.Wgs84Position;
import slash.navigation.base.Wgs84Route;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static slash.navigation.common.NavigationConversion.formatElevationAsString;
import static slash.navigation.common.NavigationConversion.formatSpeedAsString;
import static slash.common.io.Transfer.parseDouble;
import static slash.common.io.Transfer.trim;
import static slash.common.type.CompactCalendar.fromDate;
import static slash.navigation.base.RouteCharacteristics.Track;

/**
 * Reads and writes Haicom Logger (.csv) files.
 * <p/>
 * Format: 1,T,08/12/02,05:40:15,47.91561,N,106.90109,E,1308.4m,97.78,1km/h<br/>
 *         1,T,08/12/02,05:40:15,47.91561,N,106.90109,E,1308.4m,,0km/h<br/>
 *         1,T,,,36.87722,N,111.51194,W,0m,0km/h<br/>
 *
 * @author Christian Pesch
 */

public class HaicomLoggerFormat extends SimpleLineBasedFormat<SimpleRoute> {
    private static final Logger log = Logger.getLogger(HaicomLoggerFormat.class.getName());

    private static final String SEPARATOR = ",";
    private static final String HEADER_LINE = "INDEX,RCR,DATE,TIME,LATITUDE,N/S,LONGITUDE,E/W,ALTITUDE,COURSE,SPEED,";

    private static final DateFormat DATE_AND_TIME_FORMAT = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yy/MM/dd");
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    static {
        DATE_AND_TIME_FORMAT.setTimeZone(CompactCalendar.UTC);
        DATE_FORMAT.setTimeZone(CompactCalendar.UTC);
        TIME_FORMAT.setTimeZone(CompactCalendar.UTC);
    }

    private static final NumberFormat LONGITUDE_NUMBER_FORMAT = DecimalFormat.getNumberInstance(Locale.US);
    private static final NumberFormat LATITUDE_NUMBER_FORMAT = DecimalFormat.getNumberInstance(Locale.US);

    static {
        LONGITUDE_NUMBER_FORMAT.setGroupingUsed(false);
        LONGITUDE_NUMBER_FORMAT.setMinimumFractionDigits(5);
        LONGITUDE_NUMBER_FORMAT.setMaximumFractionDigits(5);
        LONGITUDE_NUMBER_FORMAT.setMinimumIntegerDigits(1);
        LATITUDE_NUMBER_FORMAT.setGroupingUsed(false);
        LATITUDE_NUMBER_FORMAT.setMinimumFractionDigits(5);
        LATITUDE_NUMBER_FORMAT.setMaximumFractionDigits(5);
        LATITUDE_NUMBER_FORMAT.setMinimumIntegerDigits(1);
    }

    private static final Pattern LINE_PATTERN = Pattern.
            compile("^" +
                    "\\d+" + SEPARATOR +
                    "\\w+" + SEPARATOR +
                    "(\\d+/\\d+/\\d+)?" + SEPARATOR +
                    "(\\d+:\\d+:\\d+)?" + SEPARATOR +
                    "([\\d\\.]+)" + SEPARATOR +
                    "([NS])" + SEPARATOR +
                    "([\\d\\.]+)" + SEPARATOR +
                    "([WE])" + SEPARATOR +
                    "(-?[\\d\\.]+)m" + SEPARATOR +
                    "([\\d\\.]*)" + SEPARATOR +
                    "([\\d\\.]+)km/h" +
                    "$");

    public String getExtension() {
        return ".csv";
    }

    public String getName() {
        return "Haicom Logger (*" + getExtension() + ")";
    }

    @SuppressWarnings({"unchecked"})
    public <P extends NavigationPosition> SimpleRoute createRoute(RouteCharacteristics characteristics, String name, List<P> positions) {
        return new Wgs84Route(this, characteristics, (List<Wgs84Position>) positions);
    }

    protected RouteCharacteristics getRouteCharacteristics() {
        return Track;
    }

    protected boolean isValidLine(String line) {
        return isPosition(line) || line.startsWith(HEADER_LINE);
    }

    protected boolean isPosition(String line) {
        Matcher matcher = LINE_PATTERN.matcher(line);
        return matcher.matches();
    }

    CompactCalendar parseDateAndTime(String date, String time) {
        date = trim(date);
        time = trim(time);
        if(date == null || time == null)
            return null;
        String dateAndTime = date + " " + time;
        try {
            Date parsed = DATE_AND_TIME_FORMAT.parse(dateAndTime);
            return fromDate(parsed);
        } catch (ParseException e) {
            log.severe("Could not parse date and time '" + dateAndTime + "'");
        }
        return null;
    }

    protected Wgs84Position parsePosition(String line, CompactCalendar startDate) {
        Matcher matcher = LINE_PATTERN.matcher(line);
        if (matcher.matches()) {
            String date = matcher.group(1);
            String time = matcher.group(2);
            Double latitude = parseDouble(matcher.group(3));
            String northOrSouth = trim(matcher.group(4));
            if("S".equals(northOrSouth))
                latitude = -latitude;
            Double longitude = parseDouble(matcher.group(5));
            String westOrEast = trim(matcher.group(6));
            if("W".equals(westOrEast))
                longitude = -longitude;
            String altitude = matcher.group(7);
            String speed = matcher.group(9);
            return new Wgs84Position(longitude, latitude, parseDouble(altitude), parseDouble(speed), parseDateAndTime(date, time), null);
        }

        throw new IllegalArgumentException("'" + line + "' does not match");
    }

    String formatLongitude(Double aDouble) {
        if (aDouble == null)
            return "";
        return LONGITUDE_NUMBER_FORMAT.format(aDouble);
    }

    String formatLatititude(Double aDouble) {
        if (aDouble == null)
            return "";
        return LATITUDE_NUMBER_FORMAT.format(aDouble);
    }

    String formatTime(CompactCalendar time) {
        if (time == null)
            return "";
        return TIME_FORMAT.format(time.getTime());
    }

    String formatDate(CompactCalendar date) {
        if (date == null)
            return "";
        return DATE_FORMAT.format(date.getTime());
    }

    protected void writeHeader(PrintWriter writer, SimpleRoute route) {
        writer.println(HEADER_LINE);
    }

    protected void writePosition(Wgs84Position position, PrintWriter writer, int index, boolean firstPosition) {
        String longitude = formatLongitude(Math.abs(position.getLongitude()));
        String westOrEast = position.getLongitude() >= 0.0 ? "E" : "W";
        String latitude = formatLatititude(Math.abs(position.getLatitude()));
        String northOrSouth = position.getLatitude() >= 0.0 ? "N" : "S";
        String time = formatTime(position.getTime());
        String date = formatDate(position.getTime());
        String altitude = formatElevationAsString(position.getElevation());
        String speed = formatSpeedAsString(position.getSpeed());
        writer.println((index + 1) + SEPARATOR + "T" + SEPARATOR +
                date + SEPARATOR + time + SEPARATOR +
                latitude + SEPARATOR + northOrSouth + SEPARATOR + longitude + SEPARATOR + westOrEast + SEPARATOR +
                altitude + "m" + SEPARATOR + "0.0" + SEPARATOR + speed + "km/h");
    }
}