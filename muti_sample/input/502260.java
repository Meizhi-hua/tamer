public final class ReverseGeocoder extends Thread {
    private static final int MAX_COUNTRY_NAME_LENGTH = 8;
    private static final int MAX_LOCALITY_MILE_RANGE = 20;
    private static final Deque<MediaSet> sQueue = new Deque<MediaSet>();
    private static final DiskCache sGeoCache = new DiskCache("geocoder-cache");
    private static final String TAG = "ReverseGeocoder";
    private static Criteria LOCATION_CRITERIA = new Criteria();
    private static Address sCurrentAddress; 
    static {
        LOCATION_CRITERIA.setAccuracy(Criteria.ACCURACY_COARSE);
        LOCATION_CRITERIA.setPowerRequirement(Criteria.NO_REQUIREMENT);
        LOCATION_CRITERIA.setBearingRequired(false);
        LOCATION_CRITERIA.setSpeedRequired(false);
        LOCATION_CRITERIA.setAltitudeRequired(false);
    }
    private Geocoder mGeocoder;
    private final Context mContext;
    public ReverseGeocoder(Context context) {
        super(TAG);
        mContext = context;
        start();
    }
    public void enqueue(MediaSet set) {
        Deque<MediaSet> inQueue = sQueue;
        synchronized (inQueue) {
            inQueue.addFirst(set);
            inQueue.notify();
        }
    }
    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Deque<MediaSet> queue = sQueue;
        mGeocoder = new Geocoder(mContext);
        queue.clear();
        try {
            for (;;) {
                MediaSet set;
                synchronized (queue) {
                    while ((set = queue.pollFirst()) == null) {
                        queue.wait();
                    }
                }
                process(set);
            }
        } catch (InterruptedException e) {
        }
    }
    public void flushCache() {
        sGeoCache.flush();
    }
    public void shutdown() {
        flushCache();
        this.interrupt();
    }
    private boolean process(final MediaSet set) {
        if (!set.mLatLongDetermined) {
            set.mReverseGeocodedLocationComputed = true;
            return false;
        }
        set.mReverseGeocodedLocation = computeMostGranularCommonLocation(set);
        set.mReverseGeocodedLocationComputed = true;
        return true;
    }
    protected String computeMostGranularCommonLocation(final MediaSet set) {
        double setMinLatitude = set.mMinLatLatitude;
        double setMinLongitude = set.mMinLatLongitude;
        double setMaxLatitude = set.mMaxLatLatitude;
        double setMaxLongitude = set.mMaxLatLongitude;
        if (Math.abs(set.mMaxLatLatitude - set.mMinLatLatitude) < Math.abs(set.mMaxLonLongitude - set.mMinLonLongitude)) {
            setMinLatitude = set.mMinLonLatitude;
            setMinLongitude = set.mMinLonLongitude;
            setMaxLatitude = set.mMaxLonLatitude;
            setMaxLongitude = set.mMaxLonLongitude;
        }
        Address addr1 = lookupAddress(setMinLatitude, setMinLongitude);
        Address addr2 = lookupAddress(setMaxLatitude, setMaxLongitude);
        if (addr1 == null)
            addr1 = addr2;
        if (addr2 == null)
            addr2 = addr1;
        if (addr1 == null || addr2 == null) {
            return null;
        }
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        List<String> providers = locationManager.getAllProviders();
        for (int i = 0; i < providers.size(); ++i) {
            String provider = providers.get(i);
            location = (provider != null) ? locationManager.getLastKnownLocation(provider) : null;
            if (location != null)
                break;
        }
        String currentCity = "";
        String currentAdminArea = "";
        String currentCountry = Locale.getDefault().getCountry();
        if (location != null) {
            Address currentAddress = lookupAddress(location.getLatitude(), location.getLongitude());
            if (currentAddress == null) {
                currentAddress = sCurrentAddress;
            } else {
                sCurrentAddress = currentAddress;
            }
            if (currentAddress != null && currentAddress.getCountryCode() != null) {
                currentCity = checkNull(currentAddress.getLocality());
                currentCountry = checkNull(currentAddress.getCountryCode());
                currentAdminArea = checkNull(currentAddress.getAdminArea());
            }
        }
        String closestCommonLocation = null;
        String addr1Locality = checkNull(addr1.getLocality());
        String addr2Locality = checkNull(addr2.getLocality());
        String addr1AdminArea = checkNull(addr1.getAdminArea());
        String addr2AdminArea = checkNull(addr2.getAdminArea());
        String addr1CountryCode = checkNull(addr1.getCountryCode());
        String addr2CountryCode = checkNull(addr2.getCountryCode());
        if (currentCity.equals(addr1Locality) && currentCity.equals(addr2Locality)) {
            String otherCity = currentCity;
            if (currentCity.equals(addr1Locality)) {
                otherCity = addr2Locality;
                if (otherCity.length() == 0) {
                    otherCity = addr2AdminArea;
                    if (!currentCountry.equals(addr2CountryCode)) {
                        otherCity += " " + addr2CountryCode;
                    }
                }
                addr2Locality = addr1Locality;
                addr2AdminArea = addr1AdminArea;
                addr2CountryCode = addr1CountryCode;
            } else {
                otherCity = addr1Locality;
                if (otherCity.length() == 0) {
                    otherCity = addr1AdminArea + " " + addr1CountryCode;
                    ;
                    if (!currentCountry.equals(addr1CountryCode)) {
                        otherCity += " " + addr1CountryCode;
                    }
                }
                addr1Locality = addr2Locality;
                addr1AdminArea = addr2AdminArea;
                addr1CountryCode = addr2CountryCode;
            }
            closestCommonLocation = valueIfEqual(addr1.getAddressLine(0), addr2.getAddressLine(0));
            if (closestCommonLocation != null && !("null".equals(closestCommonLocation))) {
                if (!currentCity.equals(otherCity)) {
                    closestCommonLocation += " - " + otherCity;
                }
                return closestCommonLocation;
            }
            closestCommonLocation = valueIfEqual(addr1.getThoroughfare(), addr2.getThoroughfare());
            if (closestCommonLocation != null && !("null".equals(closestCommonLocation))) {
                return closestCommonLocation;
            }
        }
        closestCommonLocation = valueIfEqual(addr1Locality, addr2Locality);
        if (closestCommonLocation != null && !("".equals(closestCommonLocation))) {
            String adminArea = addr1AdminArea;
            String countryCode = addr1CountryCode;
            if (adminArea != null && adminArea.length() > 0) {
                if (!countryCode.equals(currentCountry)) {
                    closestCommonLocation += ", " + adminArea + " " + countryCode;
                } else {
                    closestCommonLocation += ", " + adminArea;
                }
            }
            return closestCommonLocation;
        }
        if (currentAdminArea.equals(addr1AdminArea) && currentAdminArea.equals(addr2AdminArea)) {
            if ("".equals(addr1Locality)) {
                addr1Locality = addr2Locality;
            }
            if ("".equals(addr2Locality)) {
                addr2Locality = addr1Locality;
            }
            if (!"".equals(addr1Locality)) {
                if (addr1Locality.equals(addr2Locality)) {
                    closestCommonLocation = addr1Locality + ", " + currentAdminArea;
                } else {
                    closestCommonLocation = addr1Locality + " - " + addr2Locality;
                }
                return closestCommonLocation;
            }
        }
        int distance = (int) LocationMediaFilter.toMile(LocationMediaFilter.distanceBetween(setMinLatitude, setMinLongitude,
                setMaxLatitude, setMaxLongitude));
        if (distance < MAX_LOCALITY_MILE_RANGE) {
            closestCommonLocation = getLocalityAdminForAddress(addr1, true);
            if (closestCommonLocation != null) {
                return closestCommonLocation;
            }
            closestCommonLocation = getLocalityAdminForAddress(addr2, true);
            if (closestCommonLocation != null) {
                return closestCommonLocation;
            }
        }
        closestCommonLocation = valueIfEqual(addr1AdminArea, addr2AdminArea);
        if (closestCommonLocation != null && !("".equals(closestCommonLocation))) {
            String countryCode = addr1CountryCode;
            if (!countryCode.equals(currentCountry)) {
                if (countryCode != null && countryCode.length() > 0) {
                    closestCommonLocation += " " + countryCode;
                }
            }
            return closestCommonLocation;
        }
        closestCommonLocation = valueIfEqual(addr1CountryCode, addr2CountryCode);
        if (closestCommonLocation != null && !("".equals(closestCommonLocation))) {
            return closestCommonLocation;
        }
        String addr1Country = addr1.getCountryName();
        String addr2Country = addr2.getCountryName();
        if (addr1Country == null)
            addr1Country = addr1CountryCode;
        if (addr2Country == null)
            addr2Country = addr2CountryCode;
        if (addr1Country == null || addr2Country == null)
            return null;
        if (addr1Country.length() > MAX_COUNTRY_NAME_LENGTH || addr2Country.length() > MAX_COUNTRY_NAME_LENGTH) {
            closestCommonLocation = addr1CountryCode + " - " + addr2CountryCode;
        } else {
            closestCommonLocation = addr1Country + " - " + addr2Country;
        }
        return closestCommonLocation;
    }
    private String checkNull(String locality) {
        if (locality == null)
            return "";
        if (locality.equals("null"))
            return "";
        return locality;
    }
    protected String getReverseGeocodedLocation(final double latitude, final double longitude, final int desiredNumDetails) {
        String location = null;
        int numDetails = 0;
        try {
            Address addr = lookupAddress(latitude, longitude);
            if (addr != null) {
                location = addr.getAddressLine(0);
                if (location != null && !("null".equals(location))) {
                    numDetails++;
                } else {
                    location = addr.getThoroughfare();
                    if (location != null && !("null".equals(location))) {
                        numDetails++;
                    } else {
                        location = addr.getFeatureName();
                        if (location != null && !("null".equals(location))) {
                            numDetails++;
                        }
                    }
                }
                if (numDetails == desiredNumDetails) {
                    return location;
                }
                String locality = addr.getLocality();
                if (locality != null && !("null".equals(locality))) {
                    if (location != null && location.length() > 0) {
                        location += ", " + locality;
                    } else {
                        location = locality;
                    }
                    numDetails++;
                }
                if (numDetails == desiredNumDetails) {
                    return location;
                }
                String adminArea = addr.getAdminArea();
                if (adminArea != null && !("null".equals(adminArea))) {
                    if (location != null && location.length() > 0) {
                        location += ", " + adminArea;
                    } else {
                        location = adminArea;
                    }
                    numDetails++;
                }
                if (numDetails == desiredNumDetails) {
                    return location;
                }
                String countryCode = addr.getCountryCode();
                if (countryCode != null && !("null".equals(countryCode))) {
                    if (location != null && location.length() > 0) {
                        location += ", " + countryCode;
                    } else {
                        location = addr.getCountryName();
                    }
                }
            }
            return location;
        } catch (Exception e) {
            return null;
        }
    }
    private String getLocalityAdminForAddress(final Address addr, final boolean approxLocation) {
        if (addr == null)
            return "";
        String localityAdminStr = addr.getLocality();
        if (localityAdminStr != null && !("null".equals(localityAdminStr))) {
            if (approxLocation) {
            }
            String adminArea = addr.getAdminArea();
            if (adminArea != null && adminArea.length() > 0) {
                localityAdminStr += ", " + adminArea;
            }
            return localityAdminStr;
        }
        return null;
    }
    private Address lookupAddress(final double latitude, final double longitude) {
        try {
            long locationKey = (long) (((latitude + LocationMediaFilter.LAT_MAX) * 2 * LocationMediaFilter.LAT_MAX + (longitude + LocationMediaFilter.LON_MAX)) * LocationMediaFilter.EARTH_RADIUS_METERS);
            byte[] cachedLocation = sGeoCache.get(locationKey, 0);
            Address address = null;
            if (cachedLocation == null || cachedLocation.length == 0) {
                try {
                    List<Address> addresses = mGeocoder.getFromLocation(latitude, longitude, 1);
                    if (!addresses.isEmpty()) {
                        address = addresses.get(0);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(bos, 256));
                        Locale locale = address.getLocale();
                        Utils.writeUTF(dos, locale.getLanguage());
                        Utils.writeUTF(dos, locale.getCountry());
                        Utils.writeUTF(dos, locale.getVariant());
                        Utils.writeUTF(dos, address.getThoroughfare());
                        int numAddressLines = address.getMaxAddressLineIndex();
                        dos.writeInt(numAddressLines);
                        for (int i = 0; i < numAddressLines; ++i) {
                            Utils.writeUTF(dos, address.getAddressLine(i));
                        }
                        Utils.writeUTF(dos, address.getFeatureName());
                        Utils.writeUTF(dos, address.getLocality());
                        Utils.writeUTF(dos, address.getAdminArea());
                        Utils.writeUTF(dos, address.getSubAdminArea());
                        Utils.writeUTF(dos, address.getCountryName());
                        Utils.writeUTF(dos, address.getCountryCode());
                        Utils.writeUTF(dos, address.getPostalCode());
                        Utils.writeUTF(dos, address.getPhone());
                        Utils.writeUTF(dos, address.getUrl());
                        dos.flush();
                        sGeoCache.put(locationKey, bos.toByteArray(), 0);
                        dos.close();
                    }
                } finally {
                }
            } else {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(cachedLocation), 256));
                String language = Utils.readUTF(dis);
                String country = Utils.readUTF(dis);
                String variant = Utils.readUTF(dis);
                Locale locale = null;
                if (language != null) {
                    if (country == null) {
                        locale = new Locale(language);
                    } else if (variant == null) {
                        locale = new Locale(language, country);
                    } else {
                        locale = new Locale(language, country, variant);
                    }
                }
                if (!locale.getLanguage().equals(Locale.getDefault().getLanguage())) {
                    sGeoCache.delete(locationKey);
                    dis.close();
                    return lookupAddress(latitude, longitude);
                }
                address = new Address(locale);
                address.setThoroughfare(Utils.readUTF(dis));
                int numAddressLines = dis.readInt();
                for (int i = 0; i < numAddressLines; ++i) {
                    address.setAddressLine(i, Utils.readUTF(dis));
                }
                address.setFeatureName(Utils.readUTF(dis));
                address.setLocality(Utils.readUTF(dis));
                address.setAdminArea(Utils.readUTF(dis));
                address.setSubAdminArea(Utils.readUTF(dis));
                address.setCountryName(Utils.readUTF(dis));
                address.setCountryCode(Utils.readUTF(dis));
                address.setPostalCode(Utils.readUTF(dis));
                address.setPhone(Utils.readUTF(dis));
                address.setUrl(Utils.readUTF(dis));
                dis.close();
            }
            return address;
        } catch (Exception e) {
        }
        return null;
    }
    private String valueIfEqual(String a, String b) {
        return (a != null && b != null && a.equalsIgnoreCase(b)) ? a : null;
    }
}
