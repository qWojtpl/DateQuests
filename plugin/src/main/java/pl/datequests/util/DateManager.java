package pl.datequests.util;

import lombok.Getter;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.util.Calendar.*;

@Getter
public class DateManager {

    private Calendar fakeCalendar = null;
    private final String[] dayNames = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

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
        Calendar cal;
        if(fakeCalendar != null) {
            cal = fakeCalendar;
        } else {
            cal = Calendar.getInstance();
        }
        return cal;
    }

    public void createFakeCalendar(int year, int month, int day, int hour, int minute, int second) {
        removeFakeCalendar();
        fakeCalendar = Calendar.getInstance();
        fakeCalendar.set(YEAR, year);
        fakeCalendar.set(MONTH, month);
        fakeCalendar.set(DAY_OF_MONTH, day);
        fakeCalendar.set(HOUR_OF_DAY, hour);
        fakeCalendar.set(MINUTE, minute);
        fakeCalendar.set(SECOND, second);
    }

    public void removeFakeCalendar() {
        fakeCalendar = null;
    }

    public boolean isUsingFakeCalendar() {
        return (fakeCalendar != null);
    }

    @SneakyThrows
    public static long calculateDays(String startDate, String endDate, String separator) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy" + separator + "MM" + separator + "dd", Locale.ENGLISH);
        Date dStart = sdf.parse(startDate);
        Date dEnd = sdf.parse(endDate);
        long diffInMillis = Math.abs(dEnd.getTime() - dStart.getTime());
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }

}
