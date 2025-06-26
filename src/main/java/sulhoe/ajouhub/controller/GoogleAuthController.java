package sulhoe.ajouhub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sulhoe.ajouhub.dto.login.LoginRequestDto;
import sulhoe.ajouhub.dto.login.LoginResponseDto;
import sulhoe.ajouhub.dto.user.OAuthUserInfo;
import sulhoe.ajouhub.entity.User;
import sulhoe.ajouhub.repository.UserRepository;
import sulhoe.ajouhub.service.GoogleOAuthService;
import sulhoe.ajouhub.config.JwtTokenProvider;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleOAuthService googleOAuthService;
    private final JwtTokenProvider   jwtTokenProvider;
    private final UserRepository     userRepository;

    @PostMapping("/callback")
    public ResponseEntity<LoginResponseDto> handleGoogleCallback(@RequestBody LoginRequestDto request) {
        OAuthUserInfo userInfo = googleOAuthService.getUserInfoFromCode(request.code());

        User user = userRepository.findByEmail(userInfo.email())
                .orElseGet(() -> userRepository.save(new User(userInfo.email(), userInfo.department())));

        String accessToken  = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        return ResponseEntity.ok(new LoginResponseDto(accessToken, refreshToken));
    }
}
