public class SafeSaxTest extends AndroidTestCase {
    private static final String TAG = SafeSaxTest.class.getName();
    private static final String ATOM_NAMESPACE = "http:
    private static final String MEDIA_NAMESPACE = "http:
    private static final String YOUTUBE_NAMESPACE = "http:
    private static final String GDATA_NAMESPACE = "http:
    private static class ElementCounter implements ElementListener {
        int starts = 0;
        int ends = 0;
        public void start(Attributes attributes) {
            starts++;
        }
        public void end() {
            ends++;
        }
    }
    private static class TextElementCounter implements TextElementListener {
        int starts = 0;
        String bodies = "";
        public void start(Attributes attributes) {
            starts++;
        }
        public void end(String body) {
            this.bodies += body;
        }
    }
    @SmallTest
    public void testListener() throws Exception {
        String xml = "<feed xmlns='http:
                + "<entry>\n"
                + "<id>a</id>\n"
                + "</entry>\n"
                + "<entry>\n"
                + "<id>b</id>\n"
                + "</entry>\n"
                + "</feed>\n";
        RootElement root = new RootElement(ATOM_NAMESPACE, "feed");
        Element entry = root.requireChild(ATOM_NAMESPACE, "entry");
        Element id = entry.requireChild(ATOM_NAMESPACE, "id");
        ElementCounter rootCounter = new ElementCounter();
        ElementCounter entryCounter = new ElementCounter();
        TextElementCounter idCounter = new TextElementCounter();
        root.setElementListener(rootCounter);
        entry.setElementListener(entryCounter);
        id.setTextElementListener(idCounter);
        Xml.parse(xml, root.getContentHandler());
        assertEquals(1, rootCounter.starts);
        assertEquals(1, rootCounter.ends);
        assertEquals(2, entryCounter.starts);
        assertEquals(2, entryCounter.ends);
        assertEquals(2, idCounter.starts);
        assertEquals("ab", idCounter.bodies);
    }
    @SmallTest
    public void testMissingRequiredChild() throws Exception {
        String xml = "<feed></feed>";
        RootElement root = new RootElement("feed");
        root.requireChild("entry");
        try {
            Xml.parse(xml, root.getContentHandler());
            fail("expected exception not thrown");
        } catch (SAXException e) {
        }
    }
    @SmallTest
    public void testMixedContent() throws Exception {
        String xml = "<feed><entry></entry></feed>";
        RootElement root = new RootElement("feed");
        root.setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
            }
        });
        try {
            Xml.parse(xml, root.getContentHandler());
            fail("expected exception not thrown");
        } catch (SAXException e) {
        }
    }
    @LargeTest
    public void testPerformance() throws Exception {
        InputStream in = mContext.getResources().openRawResource(R.raw.youtube);
        byte[] xmlBytes;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            xmlBytes = out.toByteArray();
        } finally {
            in.close();
        }
        Log.i("***", "File size: " + (xmlBytes.length / 1024) + "k");
        VideoAdapter videoAdapter = new VideoAdapter();
        ContentHandler handler = newContentHandler(videoAdapter);
        for (int i = 0; i < 2; i++) {
            pureSaxTest(new ByteArrayInputStream(xmlBytes));
            saxyModelTest(new ByteArrayInputStream(xmlBytes));
            saxyModelTest(new ByteArrayInputStream(xmlBytes), handler);
        }
    }
    private static void pureSaxTest(InputStream inputStream) throws IOException, SAXException {
        long start = System.currentTimeMillis();
        VideoAdapter videoAdapter = new VideoAdapter();
        Xml.parse(inputStream, Xml.Encoding.UTF_8, new YouTubeContentHandler(videoAdapter));
        long elapsed = System.currentTimeMillis() - start;
        Log.i(TAG, "pure SAX: " + elapsed + "ms");
    }
    private static void saxyModelTest(InputStream inputStream) throws IOException, SAXException {
        long start = System.currentTimeMillis();
        VideoAdapter videoAdapter = new VideoAdapter();
        Xml.parse(inputStream, Xml.Encoding.UTF_8, newContentHandler(videoAdapter));
        long elapsed = System.currentTimeMillis() - start;
        Log.i(TAG, "Saxy Model: " + elapsed + "ms");
    }
    private static void saxyModelTest(InputStream inputStream, ContentHandler contentHandler)
            throws IOException, SAXException {
        long start = System.currentTimeMillis();
        Xml.parse(inputStream, Xml.Encoding.UTF_8, contentHandler);
        long elapsed = System.currentTimeMillis() - start;
        Log.i(TAG, "Saxy Model (preloaded): " + elapsed + "ms");
    }
    private static class VideoAdapter {
        public void addVideo(YouTubeVideo video) {
        }
    }
    private static ContentHandler newContentHandler(VideoAdapter videoAdapter) {
        return new HandlerFactory().newContentHandler(videoAdapter);
    }
    private static class HandlerFactory {
        YouTubeVideo video;
        public ContentHandler newContentHandler(VideoAdapter videoAdapter) {
            RootElement root = new RootElement(ATOM_NAMESPACE, "feed");
            final VideoListener videoListener = new VideoListener(videoAdapter);
            Element entry = root.getChild(ATOM_NAMESPACE, "entry");
            entry.setElementListener(videoListener);
            entry.getChild(ATOM_NAMESPACE, "id")
                    .setEndTextElementListener(new EndTextElementListener() {
                        public void end(String body) {
                            video.videoId = body;
                        }
                    });
            entry.getChild(ATOM_NAMESPACE, "published")
                    .setEndTextElementListener(new EndTextElementListener() {
                        public void end(String body) {
                            video.dateAdded = new Time(Time.TIMEZONE_UTC);
                            video.dateAdded.parse3339(body);
                        }
                    });
            Element author = entry.getChild(ATOM_NAMESPACE, "author");
            author.getChild(ATOM_NAMESPACE, "name")
                    .setEndTextElementListener(new EndTextElementListener() {
                        public void end(String body) {
                            video.authorName = body;
                        }
                    });
            Element mediaGroup = entry.getChild(MEDIA_NAMESPACE, "group");
            mediaGroup.getChild(MEDIA_NAMESPACE, "thumbnail")
                    .setStartElementListener(new StartElementListener() {
                        public void start(Attributes attributes) {
                            String url = attributes.getValue("", "url");
                            if (video.thumbnailUrl == null && url.length() > 0) {
                                video.thumbnailUrl = url;
                            }
                        }
                    });
            mediaGroup.getChild(MEDIA_NAMESPACE, "content")
                    .setStartElementListener(new StartElementListener() {
                        public void start(Attributes attributes) {
                            String url = attributes.getValue("", "url");
                            if (url != null) {
                                video.videoUrl = url;
                            }
                        }
                    });
            mediaGroup.getChild(MEDIA_NAMESPACE, "player")
                    .setStartElementListener(new StartElementListener() {
                        public void start(Attributes attributes) {
                            String url = attributes.getValue("", "url");
                            if (url != null) {
                                video.playbackUrl = url;
                            }
                        }
                    });
            mediaGroup.getChild(MEDIA_NAMESPACE, "title")
                    .setEndTextElementListener(new EndTextElementListener() {
                        public void end(String body) {
                            video.title = body;
                        }
                    });
            mediaGroup.getChild(MEDIA_NAMESPACE, "category")
                    .setEndTextElementListener(new EndTextElementListener() {
                        public void end(String body) {
                            video.category = body;
                        }
                    });
            mediaGroup.getChild(MEDIA_NAMESPACE, "description")
                    .setEndTextElementListener(new EndTextElementListener() {
                        public void end(String body) {
                            video.description = body;
                        }
                    });
            mediaGroup.getChild(MEDIA_NAMESPACE, "keywords")
                    .setEndTextElementListener(new EndTextElementListener() {
                        public void end(String body) {
                            video.tags = body;
                        }
                    });
            mediaGroup.getChild(YOUTUBE_NAMESPACE, "duration")
                    .setStartElementListener(new StartElementListener() {
                        public void start(Attributes attributes) {
                            String seconds = attributes.getValue("", "seconds");
                            video.lengthInSeconds
                                    = XmlUtils.convertValueToInt(seconds, 0);
                        }
                    });
            mediaGroup.getChild(YOUTUBE_NAMESPACE, "statistics")
                    .setStartElementListener(new StartElementListener() {
                        public void start(Attributes attributes) {
                            String viewCount = attributes.getValue("", "viewCount");
                            video.viewCount
                                    = XmlUtils.convertValueToInt(viewCount, 0);
                        }
                    });
            entry.getChild(GDATA_NAMESPACE, "rating")
                    .setStartElementListener(new StartElementListener() {
                        public void start(Attributes attributes) {
                            String average = attributes.getValue("", "average");
                            video.rating = average == null
                                    ? 0.0f : Float.parseFloat(average);
                        }
                    });
            return root.getContentHandler();
        }
        class VideoListener implements ElementListener {
            final VideoAdapter videoAdapter;
            public VideoListener(VideoAdapter videoAdapter) {
                this.videoAdapter = videoAdapter;
            }
            public void start(Attributes attributes) {
                video = new YouTubeVideo();
            }
            public void end() {
                videoAdapter.addVideo(video);
                video = null;
            }
        }
    }
    private static class YouTubeContentHandler extends DefaultHandler {
        final VideoAdapter videoAdapter;
        YouTubeVideo video = null;
        StringBuilder builder = null;
        public YouTubeContentHandler(VideoAdapter videoAdapter) {
            this.videoAdapter = videoAdapter;
        }
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            if (uri.equals(ATOM_NAMESPACE)) {
                if (localName.equals("entry")) {
                    video = new YouTubeVideo();
                    return;
                }
                if (video == null) {
                    return;
                }
                if (!localName.equals("id")
                        && !localName.equals("published")
                        && !localName.equals("name")) {
                    return;
                }
                this.builder = new StringBuilder();
                return;
            }
            if (video == null) {
                return;
            }
            if (uri.equals(MEDIA_NAMESPACE)) {
                if (localName.equals("thumbnail")) {
                    String url = attributes.getValue("", "url");
                    if (video.thumbnailUrl == null && url.length() > 0) {
                        video.thumbnailUrl = url;
                    }
                    return;
                }
                if (localName.equals("content")) {
                    String url = attributes.getValue("", "url");
                    if (url != null) {
                        video.videoUrl = url;
                    }
                    return;
                }
                if (localName.equals("player")) {
                    String url = attributes.getValue("", "url");
                    if (url != null) {
                        video.playbackUrl = url;
                    }
                    return;
                }
                if (localName.equals("title")
                        || localName.equals("category")
                        || localName.equals("description")
                        || localName.equals("keywords")) {
                    this.builder = new StringBuilder();
                    return;
                }
                return;
            }
            if (uri.equals(YOUTUBE_NAMESPACE)) {
                if (localName.equals("duration")) {
                    video.lengthInSeconds = XmlUtils.convertValueToInt(
                            attributes.getValue("", "seconds"), 0);
                    return;
                }
                if (localName.equals("statistics")) {
                    video.viewCount = XmlUtils.convertValueToInt(
                            attributes.getValue("", "viewCount"), 0);
                    return;
                }
                return;
            }
            if (uri.equals(GDATA_NAMESPACE)) {
                if (localName.equals("rating")) {
                    String average = attributes.getValue("", "average");
                    video.rating = average == null
                            ? 0.0f : Float.parseFloat(average);
                }
            }
        }
        @Override
        public void characters(char text[], int start, int length)
                throws SAXException {
            if (builder != null) {
                builder.append(text, start, length);
            }
        }
        String takeText() {
            try {
                return builder.toString();
            } finally {
                builder = null;
            }
        }
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (video == null) {
                return;
            }
            if (uri.equals(ATOM_NAMESPACE)) {
                if (localName.equals("published")) {
                    video.dateAdded = new Time(Time.TIMEZONE_UTC);
                    video.dateAdded.parse3339(takeText());
                    return;
                }
                if (localName.equals("name")) {
                    video.authorName = takeText();
                    return;
                }
                if (localName.equals("id")) {
                    video.videoId = takeText();
                    return;
                }
                if (localName.equals("entry")) {
                    videoAdapter.addVideo(video);
                    video = null;
                    return;
                }
                return;
            }
            if (uri.equals(MEDIA_NAMESPACE)) {
                if (localName.equals("description")) {
                    video.description = takeText();
                    return;
                }
                if (localName.equals("keywords")) {
                    video.tags = takeText();
                    return;
                }
                if (localName.equals("category")) {
                    video.category = takeText();
                    return;
                }
                if (localName.equals("title")) {
                    video.title = takeText();
                }
            }
        }
    }
    private static class YouTubeVideo {
        public String videoId;     
        public String videoUrl;       
        public String playbackUrl;    
        public String thumbnailUrl;   
        public String title;
        public Bitmap bitmap;      
        public int lengthInSeconds;
        public int viewCount;      
        public float rating;       
        public Boolean triedToLoadThumbnail;
        public String authorName;
        public Time dateAdded;
        public String category;
        public String tags;
        public String description;
    }
}
