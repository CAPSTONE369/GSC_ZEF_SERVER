package web.fridge.domain.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import web.fridge.domain.member.entity.Member;
import web.fridge.domain.member.repository.MemberRepository;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;


@RequiredArgsConstructor
@Slf4j
@Component
public class JwtService {

    private final MemberRepository memberRepository;
    // private final RedisService redisService;

    @Value("${spring.jwt.secret-key}")
    private String SECRET_KEY;

    // 토큰 유효시간 30분
    private Long accessTokenValidTime = 30 * 60 * 1000L;
    private Long refreshTokenValidTime = 1000L * 60 * 60 * 24;

    @PostConstruct
    protected void init(){
        SECRET_KEY = Base64.getEncoder().encodeToString(SECRET_KEY.getBytes());
    }

    public String createToken(String value, Long tokenValidTime){
        // JWT payload에 저장되는 정보단위. user 식별값
        Claims claims = Jwts.claims().setSubject(value);

        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidTime))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String createAccessToken(Long memberId){
        return this.createToken(memberRepository.findById(memberId).get().getEmail(), accessTokenValidTime);
    }

    public String createRefreshToken(String email){
        String refreshToken = createToken(email, refreshTokenValidTime);
        // redisService.setValues(email, refreshToken, Duration.ofMillis(refreshTokenValidTime));
        return refreshToken;
    }

    public Authentication getAuthentication(String token){
        Member member = memberRepository.findByEmail(getMemberInfo(token))
                .orElseThrow(() -> new IllegalArgumentException("토큰으로 유저 정보를 확인할 수 없습니다."));
        return new UsernamePasswordAuthenticationToken(member, "");
    }

    public String getMemberInfo(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveAccessToken(HttpServletRequest request){
        return request.getHeader("Authorization");
    }

    /*
    public String resolveRefreshToken(HttpServletRequest request){
        return request.getHeader("refresh-token");
    }
     */

    public boolean validateToken(String jwtToken){
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(jwtToken);
            log.info(claims.toString());
            log.info(claims.getBody().getExpiration().toString());
            return claims.getBody().getExpiration().after(new Date());
        }
        catch (Exception e){
            return false;
        }
    }

}