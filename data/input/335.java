public class GetChapterStatsController implements Controller {
    private static final String CURSOR_KEY = "cursor";
    private JdoStatsDao statsDao;
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Cursor cursor = null;
        if (request.getParameter(CURSOR_KEY) != null) {
            cursor = Cursor.fromWebSafeString(request.getParameter(CURSOR_KEY));
        }
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Query query = pm.newQuery(Chapter.class);
        query.setRange(0, 20);
        if (cursor != null) {
            Map<String, Object> extensionMap = new HashMap<String, Object>();
            extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
            query.setExtensions(extensionMap);
        }
        List<Chapter> chapterResults = (List<Chapter>) query.execute();
        List<Chapter> chapters = new ArrayList<Chapter>(chapterResults.size());
        for (Chapter result : chapterResults) {
            if (result.getStatus().compareToIgnoreCase("inactive") != 0) {
                chapters.add(result);
            }
        }
        String cursorString = JDOCursorHelper.getCursor(chapterResults).toWebSafeString();
        TaskOptions options = TaskOptions.Builder.url("/offline/getChapterStats").param(CURSOR_KEY, cursorString);
        countChapters(request, chapters, options);
        countCountries(request, chapters, options);
        if (chapters != null && chapters.size() > 0) {
            Queue queue = QueueFactory.getDefaultQueue();
            queue.add(options);
        }
        query.closeAll();
        pm.close();
        return null;
    }
    public void setStatsDao(JdoStatsDao statsDao) {
        this.statsDao = statsDao;
    }
    private void countChapters(HttpServletRequest request, List<Chapter> chapters, TaskOptions options) {
        final String KEY = "chapterCount";
        int chapterCount = 0;
        if (request.getParameter(KEY) != null) {
            chapterCount = Integer.parseInt(request.getParameter(KEY));
        }
        if (chapters == null || chapters.size() == 0) {
            statsDao.updateField(JdoStatsDao.Field.NUM_CHAPTERS, chapterCount);
        } else {
            for (Chapter chapter : chapters) {
                chapterCount++;
            }
            options = options.param(KEY, "" + chapterCount);
        }
    }
    private void countCountries(HttpServletRequest request, List<Chapter> chapters, TaskOptions options) {
        final String KEY = "countryCount";
        JSONObject countries = null;
        if (request.getParameter(KEY) != null) {
            countries = (JSONObject) JSONValue.parse(request.getParameter(KEY));
        } else {
            countries = new JSONObject();
        }
        if (chapters == null || chapters.size() == 0) {
            statsDao.updateField(JdoStatsDao.Field.NUM_COUNTRIES, countries.size());
        } else {
            for (Chapter chapter : chapters) {
                countries.put(chapter.getCountry(), Boolean.TRUE);
            }
            options = options.param(KEY, countries.toJSONString());
        }
    }
}
