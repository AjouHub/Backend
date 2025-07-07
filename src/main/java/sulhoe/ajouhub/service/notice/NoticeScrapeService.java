package sulhoe.ajouhub.service.notice;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sulhoe.ajouhub.config.NoticeConfig;
import sulhoe.ajouhub.dto.notice.NoticeDto;
import sulhoe.ajouhub.entity.Notice;
import sulhoe.ajouhub.repository.NoticeRepository;
import sulhoe.ajouhub.service.firebase.PushNotificationService;

import java.io.IOException;
import java.util.*;

@Service
public class NoticeScrapeService {

    private static final Logger logger = LoggerFactory.getLogger(NoticeScrapeService.class);
    private final NoticeConfig noticeConfig;

    private final PushNotificationService push;
    private final NoticeRepository noticeRepo;
    private final Map<String, Boolean> isFirstLoadMap = new HashMap<>();

    @Autowired
    public NoticeScrapeService(NoticeConfig noticeConfig, PushNotificationService push, NoticeRepository noticeRepo) {
        this.noticeConfig = noticeConfig;
        this.push = push;
        this.noticeRepo = noticeRepo;
    }

    public void scrapeNotices(String url, String type) throws IOException {
        boolean isFirst = isFirstLoadMap.getOrDefault(type, true);
        List<Notice> allNotices = new ArrayList<>();
        List<Notice> newNotices = new ArrayList<>();

        logger.info("[Info] Start scraping: {}", url);

        try {
            Document doc = Jsoup.connect(url).get();
            Elements fixedRows = doc.select("tr.b-top-box");
            addNotices(allNotices, newNotices, fixedRows, type, url, true, fixedRows.size());

            int articleLimit = 10;
            for (int offset = 0; offset < 100; offset += articleLimit) {
                String pagedUrl = url + "?mode=list&&articleLimit=" + articleLimit + "&article.offset=" + offset;
                logger.info("Scraping page: {}", pagedUrl);
                Document pagedDoc = Jsoup.connect(pagedUrl).get();
                Elements generalRows = pagedDoc.select("tr:not(.b-top-box)");
                if (generalRows.isEmpty()) break;
                addNotices(allNotices, newNotices, generalRows, type, url, false, generalRows.size());
            }

            int lastNewCount = newNotices.size();
            logger.info("Total notices: {}, New/Updated: {}", allNotices.size(), lastNewCount);

            if (!isFirst && !newNotices.isEmpty()) {
                List<NoticeDto> dtoList = NoticeDto.toDtoList(newNotices);
                // push.sendPushNotification(dtoList, type);
            }

            isFirstLoadMap.put(type, false);

        } catch (IOException e) {
            logger.error("Scraping failed: {}", url, e);
            throw e;
        }
    }

    private void addNotices(List<Notice> notices, List<Notice> newNotices, Elements rows, String type, String url, boolean isFixed, int limit) {
        int count = Math.min(rows.size(), limit);
        for (int i = 0; i < count; i++) {
            try {
                Element row = rows.get(i);
                String number = isFixed ? "공지" : getElementText(row, 0);
                String category = getElementText(row, 1);
                Element titleElement = row.selectFirst("td a");
                String department = getElementText(row, 4);
                String date = getElementText(row, 5);

                if (titleElement != null) {
                    String title = titleElement.text();
                    String articleNo = titleElement.attr("href").split("articleNo=")[1].split("&")[0];
                    String link = url + "?mode=view&articleNo=" + articleNo;

                    if (noticeConfig.getCategoriesRequirePostedDate().contains(type)) {
                        date = fetchPostedDate(link);
                    }

                    Notice scrapedNotice = new Notice(number, category, title, department, date, link);
                    Optional<Notice> existingOpt = noticeRepo.findByLink(link);

                    if (existingOpt.isEmpty()) {
                        noticeRepo.save(scrapedNotice);
                        newNotices.add(scrapedNotice);
                    } else {
                        Notice existing = existingOpt.get();
                        if (isUpdated(existing, scrapedNotice)) {
                            updateNotice(existing, scrapedNotice);
                            noticeRepo.save(existing);
                            newNotices.add(existing);
                        }
                    }

                    notices.add(scrapedNotice);
                }
            } catch (Exception e) {
                logger.error("Row parse error index={}: {}", i, e.getMessage());
            }
        }
    }

    private boolean isUpdated(Notice oldOne, Notice newOne) {
        return !Objects.equals(oldOne.getTitle(), newOne.getTitle()) || !Objects.equals(oldOne.getDate(), newOne.getDate()) || !Objects.equals(oldOne.getDepartment(), newOne.getDepartment()) || !Objects.equals(oldOne.getCategory(), newOne.getCategory());
    }

    private void updateNotice(Notice oldOne, Notice newOne) {
        oldOne.setTitle(newOne.getTitle());
        oldOne.setDate(newOne.getDate());
        oldOne.setDepartment(newOne.getDepartment());
        oldOne.setCategory(newOne.getCategory());
        oldOne.setNumber(newOne.getNumber());
    }

    private String fetchPostedDate(String link) {
        try {
            Document detailDoc = Jsoup.connect(link).get();
            Element dateElement = detailDoc.selectFirst("li.b-date-box span:contains(작성일) + span");
            return dateElement != null ? dateElement.text() : "Unknown";
        } catch (IOException e) {
            logger.error("fetchPostedDate failed: {}", link, e);
            return "Unknown";
        }
    }

    private String getElementText(Element row, int index) {
        Elements elements = row.select("td");
        return elements.size() > index ? elements.get(index).text() : "";
    }
}
