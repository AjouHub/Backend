package sulhoe.ajouhub.service.notice.parser;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import sulhoe.ajouhub.entity.Notice;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("nursingParser")
public class NursingParser implements NoticeParser {
    // href 안의 { no: 12345 } 에서 숫자만 뽑기 위한 정규식
    private static final Pattern NO_PATTERN = Pattern.compile("no\\s*:\\s*(\\d+)");

    @Override
    public String buildPageUrl(String baseUrl, int pageIdx) {
        return baseUrl + "?page=" + (pageIdx + 1);
    }

    @Override
    public Notice parseRow(Element row, boolean isFixed, String baseUrl) {
        Elements cols = row.select("td");
        String number = isFixed ? "공지" : cols.get(0).text();
        String category = cols.get(1).text();
        Element a = cols.get(2).selectFirst("a");
        String title = Objects.requireNonNull(a).text().trim();

        String href = a.attr("href");
        Matcher m = NO_PATTERN.matcher(href);
        if (!m.find())
            throw new IllegalArgumentException("Invalid nursing link href: " + href);

        String articleNo = m.group(1);
        String link = baseUrl + "?mode=view&articleNo=" + articleNo;
        String dept = "none";
        String date = cols.get(3).text();
        return new Notice(number, category, title, dept, date, link);
    }
}
