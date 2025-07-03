package sulhoe.ajouhub.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.*;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}") private String corsOrigins; // 쉼표 구분

    @Bean RestTemplate restTemplate() { return new RestTemplate(); }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsOrigins.split(","))
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /** 30분 캐시 필터 — /notice API */
    @Bean Filter cacheControlHeaderFilter() {
        return (req, res, chain) -> {
            if (res instanceof HttpServletResponse r &&
                    req instanceof HttpServletRequest h) {
                if (h.getRequestURI().startsWith("/api/notices"))
                    r.setHeader("Cache-Control","max-age=1800, public");
                else
                    r.setHeader("Cache-Control","no-store");
            }
            chain.doFilter(req,res);
        };
    }
}
