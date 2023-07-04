package pl.datequests.util;

import lombok.Getter;

import java.util.Calendar;

import static java.util.Calendar.*;

@Getter
public class DateManager {

    private final String[] dayNames = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private final String[] monthNames = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    public int getDaysOfMonth() {
        return getCalendar().getActualMaximum(DAY_OF_MONTH);
    }

    public String getDayName() {
        return dayNames[getDayOfWeek() - 1];
    }

    public int getDayOfWeek() {
        return getCalendar().get(DAY_OF_WEEK);
    }

    public int getYear() {
        return getCalendar().get(YEAR);
    }

    public int getMonth() {
        return getCalendar().get(MONTH)+1;
    }

    public String getMonthName() {
        return monthNames[getMonth() - 1];
    }

    public int getDay() {
        return getCalendar().get(DAY_OF_MONTH);
    }

    public int getHour() {
        return getCalendar().get(HOUR_OF_DAY);
    }

    public int getMinute() {
        return getCalendar().get(MINUTE);
    }

    public int getSecond() {
        return getCalendar().get(SECOND);
    }

    public String getFormattedDate(String format) {
        format = format.replace("%Y", String.valueOf(getYear()));
        format = format.replace("%M", String.valueOf(getMonth()));
        format = format.replace("%D", String.valueOf(getDay()));
        format = format.replace("%h", String.valueOf(getHour()));
        format = format.replace("%m", String.valueOf(getMinute()));
        format = format.replace("%s", String.valueOf(getSecond()));
        return format;
    }

    public Calendar getCalendar() {
        return Calendar.getInstance();
    }

}
