package konkuk.corkCharge.global.oauth.jwt;

import io.jsonwebtoken.*;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.JWT_EXPIRED;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.JWT_INVALID;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProvider jwtProvider;

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtProvider.getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long extractUserIdFromToken(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    public Long authenticateTokenAndGetUserId(String token) {
        try {
            Claims claims = getClaims(token); // 만료면 여기서 ExpiredJwtException
            return Long.valueOf(claims.getSubject());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new CustomException(JWT_EXPIRED);
        } catch (io.jsonwebtoken.MalformedJwtException | io.jsonwebtoken.security.SignatureException e) {
            throw new CustomException(JWT_INVALID);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new CustomException(JWT_INVALID);
        }
    }

}
