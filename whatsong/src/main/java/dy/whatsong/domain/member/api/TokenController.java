package dy.whatsong.domain.member.api;

import dy.whatsong.common.domain.response.ResponseService;
import dy.whatsong.domain.member.entity.Member;
import dy.whatsong.domain.member.domain.KakaoProfile;
import dy.whatsong.domain.member.domain.OAuthToken;
import dy.whatsong.domain.member.service.MemberService;
import dy.whatsong.domain.member.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class TokenController {

    private final TokenService tokenService;
    private final MemberService memberService;

    // 프론트에서 인가 코드 받는 API
    // 인가 코드로 엑세스 토큰 발급 -> 사용자 정보 조회 -> DB 저장 -> jwt 토큰 발급 -> 프론트에 토큰 전달
    @GetMapping("/token")
    public ResponseEntity kakaoLogin(@RequestParam("code") String code) {
        // authorizedCode: 카카오 서버로부터 받은 인가 코드

        // 1. 넘어온 인가 코드를 통해 access_token 발급
        OAuthToken oauthToken = tokenService.getAccessToken(code);

        // 2. 액세스 토큰 -> 카카오 사용자 정보
        KakaoProfile userInfo = tokenService.getUserInfoByToken(oauthToken.getAccess_token());

        // 3. 해당 카카오 사용자 정보를 통해 DB에 존재하는지 판별
        // Res 수정해야함. Json 리턴에 맞게!
        Member member = memberService.checkUserExistence(userInfo.getId());
        if(member != null) {
            // 3.1. 회원 정보 DB 에 존재하면? 토큰받기
            return tokenService.getTokensResponse(member);
        } else {
            // 3.2. 회원 정보 DB 에 존재하지 않으면? 성공 유무 flag + 사용자 정보
            return tokenService.getKakaoProfileResponse(userInfo);
        }
    }

}
