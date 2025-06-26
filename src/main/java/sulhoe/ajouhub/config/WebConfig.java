package sulhoe.ajouhub.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

// Web Config
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // RestTemplate 빈 정의
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // http://10.0.2.2:8080은 설치된 애플리케이션 내부 url
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://10.0.2.2:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 허용할 헤더
                .allowCredentials(true);
    }

    // 캐시 제어를 위한 필터
    @Bean
    public Filter cacheControlHeaderFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                if (response instanceof HttpServletResponse httpServletResponse) {
                    HttpServletRequest httpRequest = (HttpServletRequest) request;

                    // 모든 notices API에 대한 공통 캐싱 정책
                    // 모든 공지사항은 30분간 캐시됨
                    if (httpRequest.getRequestURI().startsWith("/api/notices")) {
                        httpServletResponse.setHeader("Cache-Control", "max-age=1800, public"); // 30분 캐싱
                    } else {
                        httpServletResponse.setHeader("Cache-Control", "no-store");
                    }
                }
                chain.doFilter(request, response);
            }

            @Override
            public void init(FilterConfig filterConfig) {
                // 초기화 필요 시 추가
            }

            @Override
            public void destroy() {
                // 자원 정리 필요 시 추가
            }
        };
    }

    // UTF-8 인코딩 필터
    @Bean
    public Filter utf8CharsetFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                if (response instanceof HttpServletResponse httpServletResponse) {
                    HttpServletRequest httpRequest = (HttpServletRequest) request;

                    // API 경로가 아닌 경우에만 Content-Type 설정
                    if (!httpRequest.getRequestURI().startsWith("/api/")) {
                        httpServletResponse.setContentType("text/html; charset=UTF-8");
                    }
                }
                chain.doFilter(request, response);
            }

            @Override
            public void init(FilterConfig filterConfig) {
                // 초기화 필요 시 추가
            }

            @Override
            public void destroy() {
                // 자원 정리 필요 시 추가
            }
        };
    }
}