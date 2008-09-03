package com.meidusa.amoeba.oracle.accessor;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

abstract class DateTimeCommonAccessor extends Accessor {

    static final int  GREGORIAN_CUTOVER_YEAR = 1582;
    static final long GREGORIAN_CUTOVER      = 0xfffff4e2f964ac00L;
    static final int  JAN_1_1_JULIAN_DAY     = 0x1a4452;
    static final int  EPOCH_JULIAN_DAY       = 0x253d8c;
    static final int  ONE_SECOND             = 1000;
    static final int  ONE_MINUTE             = 60000;
    static final int  ONE_HOUR               = 0x36ee80;
    static final long ONE_DAY                = 0x5265c00L;
    static final int  NUM_DAYS[]             = { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };
    static final int  LEAP_NUM_DAYS[]        = { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335 };
    static final int  ORACLE_CENTURY         = 0;
    static final int  ORACLE_YEAR            = 1;
    static final int  ORACLE_MONTH           = 2;
    static final int  ORACLE_DAY             = 3;
    static final int  ORACLE_HOUR            = 4;
    static final int  ORACLE_MIN             = 5;
    static final int  ORACLE_SEC             = 6;
    static final int  ORACLE_NANO1           = 7;
    static final int  ORACLE_NANO2           = 8;
    static final int  ORACLE_NANO3           = 9;
    static final int  ORACLE_NANO4           = 10;
    static final int  ORACLE_TZ1             = 11;
    static final int  ORACLE_TZ2             = 12;
    static final int  SIZE_DATE              = 7;
    static final int  MAX_TIMESTAMP_LENGTH   = 11;
    static TimeZone   epochTimeZone;
    static long       epochTimeZoneOffset;

    TimeZone          defaultTZ;
    Calendar          defaultCalendar;

    Date getDate() {
        return getDate(getDefaultCalendar());
    }

    Date getDate(Calendar calendar) {
        if (calendar == null) {
            return getDate();
        }
        Date date = null;

        int y = oracleYear(dataBytes);
        int m = oracleMonth(dataBytes);
        int d = oracleDay(dataBytes);
        calendar.set(y, m, d, 0, 0, 0);
        calendar.set(14, 0);
        if (y > 0 && calendar.isSet(0)) {
            calendar.set(0, 1);
        }
        date = new Date(calendar.getTimeInMillis());

        return date;
    }

    Time getTime() {
        Time time = null;

        TimeZone timezone = getDefaultTimeZone();
        if (timezone != epochTimeZone) {
            epochTimeZoneOffset = calculateEpochOffset(timezone);
            epochTimeZone = timezone;
        }
        time = new Time((long) oracleTime(dataBytes) - epochTimeZoneOffset);

        return time;
    }

    Time getTime(Calendar calendar) {
        if (calendar == null) {
            return getTime();
        }
        Time time = null;

        int y = (((dataBytes[0] & 0xff) - 100) * 100 + (dataBytes[1] & 0xff)) - 100;
        calendar.set(1970, 0, 1, oracleHour(dataBytes), oracleMin(dataBytes), oracleSec(dataBytes));
        calendar.set(14, 0);
        if (y > 0 && calendar.isSet(0)) {
            calendar.set(0, 1);
        }
        time = new Time(calendar.getTimeInMillis());

        return time;
    }

    Timestamp getTimestamp() throws SQLException {
        return getTimestamp(getDefaultCalendar());
    }

    Timestamp getTimestamp(Calendar calendar) throws SQLException {
        if (calendar == null) {
            return getTimestamp();
        }
        Timestamp timestamp = null;

        int y = oracleYear(dataBytes);
        int m = oracleMonth(dataBytes);
        int d = oracleDay(dataBytes);
        int h = oracleHour(dataBytes);
        calendar.set(y, m, d, h, oracleMin(dataBytes), oracleSec(dataBytes));
        calendar.set(14, 0);
        if (y > 0 && calendar.isSet(0)) {
            calendar.set(0, 1);
        }
        timestamp = new Timestamp(calendar.getTimeInMillis());
        // short word0 = rowSpaceIndicator[lengthIndex + i];
        // if (word0 >= 11) {
        // timestamp.setNanos(oracleNanos(j));
        // }

        return timestamp;
    }

    TimeZone getDefaultTimeZone() {
        if (defaultTZ == null) {
            defaultTZ = TimeZone.getDefault();
        }
        return defaultTZ;
    }

    Calendar getDefaultCalendar() {
        if (defaultCalendar == null) {
            defaultCalendar = Calendar.getInstance(getDefaultTimeZone());
        }
        return defaultCalendar;
    }

    static final int oracleYear(byte[] dataBytes) {
        int j = (((dataBytes[0] & 0xff) - 100) * 100 + (dataBytes[1] & 0xff)) - 100;
        return j > 0 ? j : j + 1;
    }

    static final int oracleMonth(byte[] dataBytes) {
        return dataBytes[2] - 1;
    }

    static final int oracleDay(byte[] dataBytes) {
        return dataBytes[3];
    }

    static final int oracleHour(byte[] dataBytes) {
        return dataBytes[4] - 1;
    }

    static final int oracleMin(byte[] dataBytes) {
        return dataBytes[5] - 1;
    }

    static final int oracleSec(byte[] dataBytes) {
        return dataBytes[6] - 1;
    }

    static final int oracleTZ1(byte[] dataBytes) {
        return dataBytes[11];
    }

    static final int oracleTZ2(byte[] dataBytes) {
        return dataBytes[12];
    }

    static final int oracleNanos(byte[] dataBytes) {
        int j = (dataBytes[7] & 0xff) << 24;
        j |= (dataBytes[8] & 0xff) << 16;
        j |= (dataBytes[9] & 0xff) << 8;
        j |= dataBytes[10] & 0xff & 0xff;
        return j;
    }

    static final int oracleTime(byte[] dataBytes) {
        int j = oracleHour(dataBytes);
        j *= 60;
        j += oracleMin(dataBytes);
        j *= 60;
        j += oracleSec(dataBytes);
        j *= 1000;
        return j;
    }

    static final long computeJulianDay(boolean flag, int i, int j, int k) {
        boolean flag1 = i % 4 == 0;
        int l = i - 1;
        long l1 = 365L * (long) l + floorDivide(l, 4L) + 0x1a444fL;
        if (flag) {
            flag1 = flag1 && (i % 100 != 0 || i % 400 == 0);
            l1 += (floorDivide(l, 400L) - floorDivide(l, 100L)) + 2L;
        }
        return l1 + (long) k + (long) (flag1 ? LEAP_NUM_DAYS[j] : NUM_DAYS[j]);
    }

    static final long floorDivide(long l, long l1) {
        return l < 0L ? (l + 1L) / l1 - 1L : l / l1;
    }

    static final long julianDayToMillis(long l) {
        return (l - 0x253d8cL) * 0x5265c00L;
    }

    static final long zoneOffset(TimeZone timezone, int i, int j, int k, int l, int i1) {
        return (long) timezone.getOffset(i >= 0 ? 1 : 0, i, j, k, l, i1);
    }

    static long getMillis(int i, int j, int k, int l, TimeZone timezone) {
        boolean flag = i >= 1582;
        long l1 = computeJulianDay(flag, i, j, k);
        long l2 = (l1 - 0x253d8cL) * 0x5265c00L;
        if (flag != (l2 >= 0xfffff4e2f964ac00L)) {
            l1 = computeJulianDay(!flag, i, j, k);
            l2 = (l1 - 0x253d8cL) * 0x5265c00L;
        }
        l2 += l;
        return l2 - zoneOffset(timezone, i, j, k, julianDayToDayOfWeek(l1), l);
    }

    static final int julianDayToDayOfWeek(long l) {
        int i = (int) ((l + 1L) % 7L);
        return i + (i >= 0 ? 1 : 8);
    }

    static long calculateEpochOffset(TimeZone timezone) {
        return zoneOffset(timezone, 1970, 0, 1, 5, 0);
    }

}
