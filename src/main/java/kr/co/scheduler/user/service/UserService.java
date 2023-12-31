package kr.co.scheduler.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.co.scheduler.global.entity.AlertUser;
import kr.co.scheduler.global.dtos.kakao.KaKaoOAuthToken;
import kr.co.scheduler.global.dtos.kakao.KakaoProfile;
import kr.co.scheduler.global.dtos.naver.NaverProfile;
import kr.co.scheduler.global.dtos.naver.NaverOAuthToken;
import kr.co.scheduler.global.repository.AlertUserRepository;
import kr.co.scheduler.global.service.ImgService;
import kr.co.scheduler.user.dtos.UserReqDTO;
import kr.co.scheduler.user.dtos.UserResDTO;
import kr.co.scheduler.user.entity.User;
import kr.co.scheduler.user.enums.Role;
import kr.co.scheduler.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final ImgService imgService;
    private final UserRepository userRepository;
    private final AlertUserRepository alertUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${OAUTH_KEY}")
    private String key;
    @Value("${OAUTH_NAVER_KEY}")
    private String client_secret;

    /**
     * selectUser: email 에 해당하는 회원을 찾아 리턴
     */
    public User selectUser(String email) {

        User user = userRepository.findOptionalByEmail(email)
                .orElseThrow(()->{
                    return new IllegalArgumentException("가입된 회원이 아닙니다.");
                });

        return user;
    }

    /**
     * validateCheckedPassword: 비밀번호와 확인용 비밀번호 일치여부 검증하여 boolean 타입 반환
     */
    public boolean validateCheckedPassword(String password, String checkedPassword) {

        if (!password.equals(checkedPassword)) {

            return true;
        }

        return  false;
    }

    /**
     * validateDuplication: 이미 가입된 계정인지 검증하여 boolean 타입 반환
     */
    public boolean validateDuplication(UserReqDTO.CREATE userReqDTO) {

        User user = userRepository.findByEmail(userReqDTO.getEmail());

        if (user != null) {

            return true;
        }

        return false;
    }

    /**
     * validatePrevPassword : 비밀번호 변경 시 현재 비밀번호 검증하여 boolean 타입 반환
     */
    public boolean validatePrevPassword(String email, String prevPassword) {

        User user = userRepository.findOptionalByEmail(email)
                .orElseThrow(()->{
                    return new IllegalArgumentException("가입된 회원이 아닙니다.");
                });

        if (passwordEncoder.matches(prevPassword, user.getPassword())){

            return false;
        }

        return true;
    }

    /**
     * validateHandling: 회원가입에 Validation 적용
     *
     * 1. BindingResult 를 파라미터로 받음
     * 2. Map 객체를 만든 후
     * 3. bindingResult 의 모든 error 에 validKey 와 error.getDefaultMessage 를 붙여 리턴함
     */
    public Map<String, String> validateHandling(BindingResult bindingResult) {

        Map<String, String> validateResult = new HashMap<>();

        for(FieldError error : bindingResult.getFieldErrors()) {

            String validKey = String.format("valid_%s", error.getField());
            validateResult.put(validKey, error.getDefaultMessage());
        }

        return validateResult;
    }

    // ================================== 구분 ================================== //

    /**
     * signUp: 회원가입
     * 컨트롤러에서 Dto 를 받아 User 객체로 빌드하여 저장
     */
    @Transactional
    public void signUp(UserReqDTO.CREATE create) {

            User user = User.builder()
                    .email(create.getEmail())
                    .password(passwordEncoder.encode(create.getPassword()))
                    .name(create.getName())
                    .phone(create.getPhone())
                    .role(Role.USER)
                    .build();

            userRepository.save(user);
    }

    /**
     * searchInfo: 회원정보 조회
     * 1. 세션의 이메일을 통해 가입된 회원인지 검증
     * 2. UserResDTO 에 회원이 조회 가능한 데이터 선별하여 반환
     */
    public UserResDTO searchInfo(String email) {

        final User userInfo = userRepository.findOptionalByEmail(email)
                .orElseThrow(()->{
                    return new IllegalArgumentException("가입되지 않은 회원입니다.");
                });

        return UserResDTO.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .phone(userInfo.getPhone())
                .createdAt(userInfo.getCreatedAt())
                .updatedAt(userInfo.getUpdatedAt())
                .build();
    }

    /**
     * updateInfo: 회원정보 수정
     * 1. 가입된 회원인지 검증
     * 2. UserReqDTO 를 통해 정보를 넘겨 수정함
     */
    @Transactional
    public void updateInfo(UserReqDTO.UPDATE_INFO update, String email) {

        User user = userRepository.findOptionalByEmail(email)
                .orElseThrow(()->{
                    return new IllegalArgumentException("가입된 회원이 아닙니다.");
                });

        user.updateInfo(update.getName(), update.getPhone());
    }

    /**
     * updatePassword: 회원 비밀번호 수정
     * 1. 가입된 회원인지 검증
     * 2. UserReqDTO 를 통해 비밀번호를 넘겨 수정함
     */
    @Transactional
    public void updatePassword(UserReqDTO.UPDATE_PASSWORD update, String email) {

        User user = userRepository.findOptionalByEmail(email)
                .orElseThrow(()->{
                    return new IllegalArgumentException("가입된 회원이 아닙니다.");
                });

        user.updatePassword(passwordEncoder.encode(update.getPassword()));
    }

    // ================================== 구분 ================================== //

    /**
     * oauthSignUp: OAuth2.0 회원가입
     */
    @Transactional
    public void oAuthSignUp(User user) {

        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        user.setRole(Role.USER);

        userRepository.save(user);
    }

    /**
     * kakaoLogin: 카카오 OAuth2.0 회원가입 및 로그인
     * 1. AccessToken 요청
     * 2. 사용자정보 요청
     * 3. 초기 로그인 시 회원가입
     * 4. 기존 회원일 경우 로그인
     */
    public void kakaoLogin(HttpServletRequest request, String code) {

        // AccessToken 요청
        // 1. HttpHeader 오브젝트 생성
        RestTemplate rt1 = new RestTemplate();
        HttpHeaders headers1 = new HttpHeaders();
        headers1.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 2. HttpBody 오브젝트 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "262c86c00bbc08f8657b2bc0851efa0d");
        params.add("redirect_uri", "http://localhost:8080/kakao/callback");
        params.add("code", code);

        // 3. HttpHeader + HttpBody를 하나의 오브젝트에 담기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers1);

        // 4. Http 요청 : exchange 함수는 HttpEntity 오브젝트를 넣게 되어있다.
        ResponseEntity<String> response1 = rt1.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // 5. oauthToken 에 저장
        ObjectMapper objectMapper1 = new ObjectMapper();
        KaKaoOAuthToken oauthToken = null;

        try {
            oauthToken = objectMapper1.readValue(response1.getBody(), KaKaoOAuthToken.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 사용자정보 요청
        // 1. HttpHeader 오브젝트 생성
        RestTemplate rt2 = new RestTemplate();
        HttpHeaders headers2 = new HttpHeaders();
        headers2.add("Authorization", "Bearer "+ oauthToken.getAccess_token());
        headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 2. HttpHeader 를 오브젝트에 담기
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers2);

        // 3. Http 요청
        ResponseEntity<String> response2 = rt2.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        // 4. kakaoProfile 에 저장
        ObjectMapper objectMapper2 = new ObjectMapper();
        KakaoProfile kakaoProfile = null;

        try {
            kakaoProfile = objectMapper2.readValue(response2.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        User kakaoUser = User.builder()
                .email(kakaoProfile.getKakao_account().getEmail())
                .password(key)
                .phone("전화번호를 등록해주세요")
                .name(kakaoProfile.getKakao_account().getProfile().getNickname())
                .oauth("kakao")
                .build();

        // 가입 및 비가입 체크
        User originUser = userRepository.findByEmail(kakaoUser.getEmail());

        if (originUser == null) {
            System.out.println("기존 회원이 아닙니다.");
            oAuthSignUp(kakaoUser);
        }

        // 로그인 처리
        List<GrantedAuthority> collectors = new ArrayList<GrantedAuthority>();
        collectors.add(new SimpleGrantedAuthority("ROLE_USER"));

        // 세션 등록
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(kakaoUser.getEmail(), key, collectors));
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
    }

    /**
     * naverLogin: 네이버 OAuth2.0 회원가입 및 로그인
     * 1. AccessToken 요청
     * 2. 사용자정보 요청
     * 3. 초기 로그인 시 회원가입
     * 4. 기존 회원일 경우 로그인
     */
    public void naverLogin(HttpServletRequest request, String code, String state) {

        // AccessToken 요청
        RestTemplate rt1 = new RestTemplate();
        HttpHeaders headers1 = new HttpHeaders();
        headers1.add("Content-type", "application/x-www-form-urlencoded");

        // 2. HttpBody 오브젝트 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "X7QHnxHieBySSOROJ7m8");
        params.add("code", code);
        params.add("state", state);
        params.add("client_secret", client_secret);

        // 3. HttpHeader + HttpBody를 하나의 오브젝트에 담기
        HttpEntity<MultiValueMap<String, String>> naverTokenRequest = new HttpEntity<>(params, headers1);

        // 4. Http 요청 : exchange 함수는 HttpEntity 오브젝트를 넣게 되어있다.
        ResponseEntity<String> response1 = rt1.exchange(
                "https://nid.naver.com/oauth2.0/token",
                HttpMethod.POST,
                naverTokenRequest,
                String.class
        );

        // 5. oauthToken 에 저장
        ObjectMapper objectMapper1 = new ObjectMapper();
        NaverOAuthToken oauthToken = null;

        try {
            oauthToken = objectMapper1.readValue(response1.getBody(), NaverOAuthToken.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 사용자정보 요청
        // 1. HttpHeader 오브젝트 생성
        RestTemplate rt2 = new RestTemplate();
        HttpHeaders headers2 = new HttpHeaders();
        headers2.add("Authorization", "Bearer "+ oauthToken.getAccess_token());
        headers1.add("Content-type", "application/x-www-form-urlencoded");

        // 2. HttpHeader 를 오브젝트에 담기
        HttpEntity<MultiValueMap<String, String>> naverProfileRequest = new HttpEntity<>(headers2);

        // 3. Http 요청
        ResponseEntity<String> response2 = rt2.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.POST,
                naverProfileRequest,
                String.class
        );

        // 4. NaverProfile 에 저장
        ObjectMapper objectMapper2 = new ObjectMapper();
        NaverProfile naverProfile = null;

        try {
            naverProfile = objectMapper2.readValue(response2.getBody(), NaverProfile.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        User naverUser = User.builder()
                .email(naverProfile.getResponse().getEmail())
                .password(key)
                .phone(naverProfile.getResponse().getMobile())
                .name(naverProfile.getResponse().getName())
                .oauth("naver")
                .build();

        // 가입 및 비가입 체크
        User originUser = userRepository.findByEmail(naverUser.getEmail());

        if (originUser == null) {
            System.out.println("기존 회원이 아닙니다.");
            oAuthSignUp(naverUser);
        }

        // 로그인 처리
        List<GrantedAuthority> collectors = new ArrayList<GrantedAuthority>();
        collectors.add(new SimpleGrantedAuthority("ROLE_USER"));

        // 세션 등록
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(naverUser.getEmail(), key, collectors));
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
    }

    // ================================== 구분 ================================== //

    /**
     * uploadProfileImg : 프로필이미지 등록 및 수정
     */
    @Transactional
    public void uploadProfileImg(String email, MultipartFile uploadImg) {

        String uploadFolder = "C:\\upload\\profile";

        String uploadFileName = imgService.uploadImg(uploadFolder, uploadImg);

        User user = userRepository.findOptionalByEmail(email)
                .orElseThrow(()->{
                    return new IllegalArgumentException("가입된 회원이 아닙니다.");
                });

        user.setProfileImgName(uploadFileName);
        user.setProfileImgPath(uploadFolder + "\\" + uploadFileName);
    }

    /**
     * deleteImg: 프로필이미지 제거
     * 1. 업로드된 프로필이미지 파일을 삭제
     * 2. DB 에서 프로필이미지에 대한 데이터에 null 값을 넣음
     */
    @Transactional
    public void deleteImg(String email) {

        User user = selectUser(email);

        if (user != null) {

            File file = null;

            try {

                file = new File(URLDecoder.decode(user.getProfileImgPath(), StandardCharsets.UTF_8));
                file.delete();
            } catch(Exception e) {

                e.printStackTrace();
            }

            user.setProfileImgName(null);
            user.setProfileImgPath(null);
        }
    }

    // ================================== 구분 ================================== //

    /**
     * updateTargetToken: 로그인한 사용자가 알림 허용 상태라면 FCM TargetToken 등록
     */
    @Transactional
    public void updateTargetToken(String targetToken, User user) {

        user.setTargetToken(targetToken);
    }

    // ================================== 구분 ================================== //

    /**
     * selectAlerts: 알림 조회
     */
    public Page<AlertUser> selectAlerts(Pageable pageable, String email) {

        User user = selectUser(email);
        Page alertUser = null;

        if (user != null) {

            alertUser = alertUserRepository.findPageByUser(pageable, user);
        }

        return alertUser;
    }

    /**
     * deleteAlert: 알림 제거
     */
    public void deleteAlert(Long id) {

        AlertUser alertUser = alertUserRepository.findById(id)
                .orElseThrow(()->{
                    return new IllegalArgumentException("해당 알림을 찾을 수 없습니다.");
        });

        alertUserRepository.delete(alertUser);
    }

    /**
     * deleteAllAlert: 모든 알림 제거
     */
    @Transactional
    public void deleteAllAlert(String email) {

        User user = selectUser(email);
        List<AlertUser> alertUsers = null;

        if (user != null) {

            alertUsers = alertUserRepository.findListByUser(user);

            for (AlertUser alertUser : alertUsers) {

                alertUserRepository.delete(alertUser);
            }
        }
    }

    // ================================== 구분 ================================== //

    /**
     * updateLastLoggedDay: 사용자가 로그인할 경우 마지막으로 로그인한 날짜 정보 등록
     */
    @Transactional
    public void updateLastLoggedDay(String email) {

        User user = selectUser(email);

        if (user != null) {

            user.setLastLoggedDay(LocalDate.now());
        }
    }
}
