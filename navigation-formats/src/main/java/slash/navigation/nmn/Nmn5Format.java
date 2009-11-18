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

package slash.navigation.nmn;

import slash.navigation.Wgs84Position;
import slash.common.io.CompactCalendar;
import slash.common.io.Transfer;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads and writes Navigon Mobile Navigator 5 (.rte) files.
 * <p/>
 * Format: -|-|17|4353|72574|BAD URACH|72574|SHELL|-|-|-|9.38107|48.49711|617|-|9.39698|48.49193|
 *
 * @author Christian Pesch
 */

public class Nmn5Format extends NmnFormat {

    private static final Pattern LINE_PATTERN = Pattern.
            compile(WILDCARD + SEPARATOR + WILDCARD + SEPARATOR +
                    WILDCARD + SEPARATOR + WILDCARD + SEPARATOR + WILDCARD + SEPARATOR +
                    "(" + WILDCARD + ")" + SEPARATOR +
                    WILDCARD + SEPARATOR +
                    "(" + WILDCARD + ")" + SEPARATOR +
                    "(" + WILDCARD + ")" + SEPARATOR +
                    WILDCARD + SEPARATOR + WILDCARD + SEPARATOR +
                    "(" + POSITION + ")" + SEPARATOR + "(" + POSITION + ")" + SEPARATOR +
                    WILDCARD + SEPARATOR + WILDCARD + SEPARATOR + WILDCARD + SEPARATOR + WILDCARD + SEPARATOR);

    public String getName() {
        return "Navigon Mobile Navigator 5 (*" + getExtension() + ")";
    }


    protected boolean isPosition(String line) {
        Matcher matcher = LINE_PATTERN.matcher(line);
        return matcher.matches();
    }

    private static String parseForNmn5(String string) {
        String result = Transfer.trim(string);
        if (result != null && "-".equals(result))
            result = null;
        if (result != null && result.length() > 2 && result.toUpperCase().equals(result))
            result = Transfer.toMixedCase(result);
        return result;
    }

    protected NmnPosition parsePosition(String line, CompactCalendar startDate) {
        Matcher lineMatcher = LINE_PATTERN.matcher(line);
        if (!lineMatcher.matches())
            throw new IllegalArgumentException("'" + line + "' does not match");

        String city = parseForNmn5(lineMatcher.group(1));
        String street = parseForNmn5(lineMatcher.group(2));
        String number = parseForNmn5(lineMatcher.group(3));
        String longitude = lineMatcher.group(4);
        String latitude = lineMatcher.group(5);
        return new NmnPosition(Transfer.parseDouble(longitude), Transfer.parseDouble(latitude), null, city, street, number);
    }

    private static String formatForNmn5(String string) {
        return string != null ? escapeSeparator(string) : "-";
    }

    protected void writePosition(Wgs84Position position, PrintWriter writer, int index, boolean firstPosition) {
        NmnPosition nmnPosition = (NmnPosition) position;
        String longitude = Transfer.formatPositionAsString(nmnPosition.getLongitude());
        String latitude = Transfer.formatPositionAsString(nmnPosition.getLatitude());
        String city = formatForNmn5(nmnPosition.isUnstructured() ? nmnPosition.getComment() : nmnPosition.getCity());
        String street = formatForNmn5(nmnPosition.isUnstructured() ? null : nmnPosition.getStreet());
        String number = formatForNmn5(nmnPosition.isUnstructured() ? null : nmnPosition.getNumber());
        writer.println("-" + SEPARATOR_CHAR + "-" + SEPARATOR_CHAR +
                "-" + SEPARATOR_CHAR + "-" + SEPARATOR_CHAR + "-" + SEPARATOR_CHAR +
                city + "" + SEPARATOR_CHAR +
                "-" + SEPARATOR_CHAR +
                street + SEPARATOR_CHAR +
                number + SEPARATOR_CHAR +
                "-" + SEPARATOR_CHAR + "-" + SEPARATOR_CHAR +
                longitude + SEPARATOR_CHAR + latitude + SEPARATOR_CHAR +
                "-" + SEPARATOR_CHAR + "-" + SEPARATOR_CHAR +
                longitude + SEPARATOR_CHAR + latitude + SEPARATOR_CHAR);
    }
}