package ru.doccloud.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * @author Petri Kainulainen
 */
public class DateHelper {

    public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static java.util.Date parseFully(DateFormat format, String text)
            throws ParseException {
        ParsePosition position = new ParsePosition(0);
        java.util.Date date = format.parse(text, position);
        if (position.getIndex() == text.length()) {
            return date;
        }
        if (date == null) {
            throw new ParseException("Date could not be parsed: " + text,
                    position.getErrorIndex());
        }
        throw new ParseException("Date was parsed incompletely: " + text,
                position.getIndex());
    }
}
