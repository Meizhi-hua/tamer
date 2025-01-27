public abstract class ASN1Time extends ASN1StringType {
    public ASN1Time(int tagNumber) {
        super(tagNumber);
    }
    public Object getDecodedObject(BerInputStream in) throws IOException {
        GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT")); 
        c.set(Calendar.YEAR, in.times[0]);
        c.set(Calendar.MONTH, in.times[1]-1);
        c.set(Calendar.DAY_OF_MONTH, in.times[2]);
        c.set(Calendar.HOUR_OF_DAY, in.times[3]);
        c.set(Calendar.MINUTE, in.times[4]);
        c.set(Calendar.SECOND, in.times[5]);
        c.set(Calendar.MILLISECOND, in.times[6]);
        return c.getTime();
    }
}
