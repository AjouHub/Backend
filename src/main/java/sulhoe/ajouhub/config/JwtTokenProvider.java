package sulhoe.ajouhub.config;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    public static final String ACCESS       = "access";
    public static final String REFRESH      = "refresh";
    public static final String TOKEN_TYPE   = "tokenType";
    public static final String TOKEN_HEADER = "Auth-Token";     // 클라이언트가 보내줄 헤더 이름

    @Value("${jwt.key}")
    private String tokenSecretKey;           // application.yml / properties에서 주입

    // 1 시간
    private static final long JWT_ACCESS_EXP  = 1000L * 60 * 60;
    // 7 일
    private static final long JWT_REFRESH_EXP = 1000L * 60 * 60 * 24 * 7;

    /** secret key Base64 인코딩 */
    @PostConstruct
    public void init() {
        tokenSecretKey = Base64.getEncoder()
                .encodeToString(tokenSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    /** AccessToken 생성 */
    public String createAccessToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + JWT_ACCESS_EXP))
                .claim(TOKEN_TYPE, ACCESS)
                .signWith(SignatureAlgorithm.HS256, tokenSecretKey)
                .compact();
    }

    /** RefreshToken 생성 */
    public String createRefreshToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + JWT_REFRESH_EXP))
                .claim(TOKEN_TYPE, REFRESH)
                .signWith(SignatureAlgorithm.HS256, tokenSecretKey)
                .compact();
    }

    /** 토큰에서 회원 email 추출 */
    public String getEmail(String token) {
        return Jwts.parser()
                .setSigningKey(tokenSecretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /** 토큰 유효성 검사 */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(tokenSecretKey).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 토큰");
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 토큰");
        }
        return false;
    }

    /** Http Header 에서 토큰 추출 */
    public String resolveToken(HttpServletRequest request) {
        return request.getHeader(TOKEN_HEADER);
    }
}
