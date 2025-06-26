package sulhoe.ajouhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import sulhoe.ajouhub.dto.user.OAuthUserInfo;

@Service
public class GoogleOAuthService {

    private final WebClient webClient = WebClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OAuthUserInfo getUserInfoFromCode(String code) {
        String accessToken = exchangeCodeForToken(code);
        return getUserInfo(accessToken);
    }

    private String exchangeCodeForToken(String code) {
        String response = webClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .bodyValue("client_id=YOUR_CLIENT_ID&" +
                        "client_secret=YOUR_CLIENT_SECRET&" +
                        "code=" + code + "&" +
                        "redirect_uri=YOUR_REDIRECT_URI&" +
                        "grant_type=authorization_code")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode node = objectMapper.readTree(response);
            return node.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token", e);
        }
    }

    private OAuthUserInfo getUserInfo(String accessToken) {
        String userInfo = webClient.get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode json = objectMapper.readTree(userInfo);
            String email = json.get("email").asText();

            if (!email.endsWith("@ajou.ac.kr")) {
                throw new IllegalArgumentException("Only @ajou.ac.kr emails are allowed.");
            }

            return new OAuthUserInfo(email, "DefaultDepartment");

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse user info", e);
        }
    }
}
