public class LocalGregorianCalendar extends BaseCalendar {
    private String name;
    private Era[] eras;
    public static class Date extends BaseCalendar.Date {
        protected Date() {
            super();
        }
        protected Date(TimeZone zone) {
            super(zone);
        }
        private int gregorianYear = FIELD_UNDEFINED;
        public Date setEra(Era era) {
            if (getEra() != era) {
                super.setEra(era);
                gregorianYear = FIELD_UNDEFINED;
            }
            return this;
        }
        public Date addYear(int localYear) {
            super.addYear(localYear);
            gregorianYear += localYear;
            return this;
        }
        public Date setYear(int localYear) {
            if (getYear() != localYear) {
                super.setYear(localYear);
                gregorianYear = FIELD_UNDEFINED;
            }
            return this;
        }
        public int getNormalizedYear() {
            return gregorianYear;
        }
        public void setNormalizedYear(int normalizedYear) {
            this.gregorianYear = normalizedYear;
        }
        void setLocalEra(Era era) {
            super.setEra(era);
        }
        void setLocalYear(int year) {
            super.setYear(year);
        }
        public String toString() {
            String time = super.toString();
            time = time.substring(time.indexOf('T'));
            StringBuffer sb = new StringBuffer();
            Era era = getEra();
            if (era != null) {
                String abbr = era.getAbbreviation();
                if (abbr != null) {
                    sb.append(abbr);
                }
            }
            sb.append(getYear()).append('.');
            CalendarUtils.sprintf0d(sb, getMonth(), 2).append('.');
            CalendarUtils.sprintf0d(sb, getDayOfMonth(), 2);
            sb.append(time);
            return sb.toString();
        }
    }
    static LocalGregorianCalendar getLocalGregorianCalendar(String name) {
        Properties calendarProps = null;
        try {
            String homeDir = AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("java.home"));
            final String fname = homeDir + File.separator + "lib" + File.separator
                                 + "calendars.properties";
            calendarProps = (Properties) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IOException {
                    Properties props = new Properties();
                    try (FileInputStream fis = new FileInputStream(fname)) {
                        props.load(fis);
                    }
                    return props;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new RuntimeException(e.getException());
        }
        String props = calendarProps.getProperty("calendar." + name + ".eras");
        if (props == null) {
            return null;
        }
        List<Era> eras = new ArrayList<Era>();
        StringTokenizer eraTokens = new StringTokenizer(props, ";");
        while (eraTokens.hasMoreTokens()) {
            String items = eraTokens.nextToken().trim();
            StringTokenizer itemTokens = new StringTokenizer(items, ",");
            String eraName = null;
            boolean localTime = true;
            long since = 0;
            String abbr = null;
            while (itemTokens.hasMoreTokens()) {
                String item = itemTokens.nextToken();
                int index = item.indexOf('=');
                if (index == -1) {
                    return null;
                }
                String key = item.substring(0, index);
                String value = item.substring(index + 1);
                if ("name".equals(key)) {
                    eraName = value;
                } else if ("since".equals(key)) {
                    if (value.endsWith("u")) {
                        localTime = false;
                        since = Long.parseLong(value.substring(0, value.length() - 1));
                    } else {
                        since = Long.parseLong(value);
                    }
                } else if ("abbr".equals(key)) {
                    abbr = value;
                } else {
                    throw new RuntimeException("Unknown key word: " + key);
                }
            }
            Era era = new Era(eraName, abbr, since, localTime);
            eras.add(era);
        }
        Era[] eraArray = new Era[eras.size()];
        eras.toArray(eraArray);
        return new LocalGregorianCalendar(name, eraArray);
    }
    private LocalGregorianCalendar(String name, Era[] eras) {
        this.name = name;
        this.eras = eras;
        setEras(eras);
    }
    public String getName() {
        return name;
    }
    public Date getCalendarDate() {
        return getCalendarDate(System.currentTimeMillis(), newCalendarDate());
    }
    public Date getCalendarDate(long millis) {
        return getCalendarDate(millis, newCalendarDate());
    }
    public Date getCalendarDate(long millis, TimeZone zone) {
        return getCalendarDate(millis, newCalendarDate(zone));
    }
    public Date getCalendarDate(long millis, CalendarDate date) {
        Date ldate = (Date) super.getCalendarDate(millis, date);
        return adjustYear(ldate, millis, ldate.getZoneOffset());
    }
    private Date adjustYear(Date ldate, long millis, int zoneOffset) {
        int i;
        for (i = eras.length - 1; i >= 0; --i) {
            Era era = eras[i];
            long since = era.getSince(null);
            if (era.isLocalTime()) {
                since -= zoneOffset;
            }
            if (millis >= since) {
                ldate.setLocalEra(era);
                int y = ldate.getNormalizedYear() - era.getSinceDate().getYear() + 1;
                ldate.setLocalYear(y);
                break;
            }
        }
        if (i < 0) {
            ldate.setLocalEra(null);
            ldate.setLocalYear(ldate.getNormalizedYear());
        }
        ldate.setNormalized(true);
        return ldate;
    }
    public Date newCalendarDate() {
        return new Date();
    }
    public Date newCalendarDate(TimeZone zone) {
        return new Date(zone);
    }
    public boolean validate(CalendarDate date) {
        Date ldate = (Date) date;
        Era era = ldate.getEra();
        if (era != null) {
            if (!validateEra(era)) {
                return false;
            }
            ldate.setNormalizedYear(era.getSinceDate().getYear() + ldate.getYear());
        } else {
            ldate.setNormalizedYear(ldate.getYear());
        }
        return super.validate(ldate);
    }
    private boolean validateEra(Era era) {
        for (int i = 0; i < eras.length; i++) {
            if (era == eras[i]) {
                return true;
            }
        }
        return false;
    }
    public boolean normalize(CalendarDate date) {
        if (date.isNormalized()) {
            return true;
        }
        normalizeYear(date);
        Date ldate = (Date) date;
        super.normalize(ldate);
        boolean hasMillis = false;
        long millis = 0;
        int year = ldate.getNormalizedYear();
        int i;
        Era era = null;
        for (i = eras.length - 1; i >= 0; --i) {
            era = eras[i];
            if (era.isLocalTime()) {
                CalendarDate sinceDate = era.getSinceDate();
                int sinceYear = sinceDate.getYear();
                if (year > sinceYear) {
                    break;
                }
                if (year == sinceYear) {
                    int month = ldate.getMonth();
                    int sinceMonth = sinceDate.getMonth();
                    if (month > sinceMonth) {
                        break;
                    }
                    if (month == sinceMonth) {
                        int day = ldate.getDayOfMonth();
                        int sinceDay = sinceDate.getDayOfMonth();
                        if (day > sinceDay) {
                            break;
                        }
                        if (day == sinceDay) {
                            long timeOfDay = ldate.getTimeOfDay();
                            long sinceTimeOfDay = sinceDate.getTimeOfDay();
                            if (timeOfDay >= sinceTimeOfDay) {
                                break;
                            }
                            --i;
                            break;
                        }
                    }
                }
            } else {
                if (!hasMillis) {
                    millis  = super.getTime(date);
                    hasMillis = true;
                }
                long since = era.getSince(date.getZone());
                if (millis >= since) {
                    break;
                }
            }
        }
        if (i >= 0) {
            ldate.setLocalEra(era);
            int y = ldate.getNormalizedYear() - era.getSinceDate().getYear() + 1;
            ldate.setLocalYear(y);
        } else {
            ldate.setEra(null);
            ldate.setLocalYear(year);
            ldate.setNormalizedYear(year);
        }
        ldate.setNormalized(true);
        return true;
    }
    void normalizeMonth(CalendarDate date) {
        normalizeYear(date);
        super.normalizeMonth(date);
    }
    void normalizeYear(CalendarDate date) {
        Date ldate = (Date) date;
        Era era = ldate.getEra();
        if (era == null || !validateEra(era)) {
            ldate.setNormalizedYear(ldate.getYear());
        } else {
            ldate.setNormalizedYear(era.getSinceDate().getYear() + ldate.getYear() - 1);
        }
    }
    public boolean isLeapYear(int gregorianYear) {
        return CalendarUtils.isGregorianLeapYear(gregorianYear);
    }
    public boolean isLeapYear(Era era, int year) {
        if (era == null) {
            return isLeapYear(year);
        }
        int gyear = era.getSinceDate().getYear() + year - 1;
        return isLeapYear(gyear);
    }
    public void getCalendarDateFromFixedDate(CalendarDate date, long fixedDate) {
        Date ldate = (Date) date;
        super.getCalendarDateFromFixedDate(ldate, fixedDate);
        adjustYear(ldate, (fixedDate - EPOCH_OFFSET) * DAY_IN_MILLIS, 0);
    }
}
