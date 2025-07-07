package sulhoe.ajouhub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

// 스크래핑할 공지사항 링크들을 카테고리별로 매핑
@Configuration
@ConfigurationProperties(prefix = "notice") // application.properties에서 notice. 이하
@Getter
@Setter
public class NoticeConfig {
    private Map<String, String> urls;
    // 게시일을 별도로 확인해야 할 카테고리 목록
    private Set<String> categoriesRequirePostedDate;
}