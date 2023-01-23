package com.keeper.homepage.global.config.security.filter;

import static com.keeper.homepage.global.config.security.data.JwtType.ACCESS_TOKEN;
import static com.keeper.homepage.global.config.security.data.JwtType.REFRESH_TOKEN;
import static com.keeper.homepage.global.config.security.data.JwtValidationType.EXPIRED;

import com.keeper.homepage.global.config.security.JwtTokenProvider;
import com.keeper.homepage.global.config.security.data.JwtType;
import com.keeper.homepage.global.config.security.data.JwtValidationType;
import com.keeper.homepage.global.config.security.data.TokenValidationResultDto;
import com.keeper.homepage.global.util.redis.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
@RequiredArgsConstructor
@Component
public class RefreshTokenFilter extends GenericFilterBean {

  private final JwtTokenProvider jwtTokenProvider;
  private final RedisUtil redisUtil;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    final var accessTokenDto = jwtTokenProvider.tryCheckTokenValid(httpRequest, ACCESS_TOKEN);
    final var refreshTokenDto = jwtTokenProvider.tryCheckTokenValid(httpRequest, REFRESH_TOKEN);
    boolean isAccessExpiredAndRefreshValid = isAccessTokenExpired(accessTokenDto.getResultType()) &&
        isRefreshTokenValid(refreshTokenDto) &&
        isTokenInRedis(refreshTokenDto);

    if (isAccessExpiredAndRefreshValid) {
      Authentication auth = jwtTokenProvider.getAuthentication(accessTokenDto.getToken());
      SecurityContextHolder.getContext().setAuthentication(auth);
    }

    filterChain.doFilter(request, response);

    if (isAccessExpiredAndRefreshValid) {
      long authId = jwtTokenProvider.getAuthId(refreshTokenDto.getToken());
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      String newRefreshToken = setTokenInCookie(REFRESH_TOKEN, authId, httpResponse);
      setTokenInCookie(ACCESS_TOKEN, authId, httpResponse);
      redisUtil.setDataExpire(newRefreshToken, "", REFRESH_TOKEN.getExpiredMillis());
    }
  }

  private boolean isTokenInRedis(TokenValidationResultDto refreshTokenDto) {
    return redisUtil.getData(refreshTokenDto.getToken()).isPresent();
  }

  private static boolean isAccessTokenExpired(JwtValidationType resultType) {
    return EXPIRED.equals(resultType);
  }

  private boolean isRefreshTokenValid(TokenValidationResultDto refreshTokenDto) {
    return refreshTokenDto.isValid();
  }

  private String setTokenInCookie(JwtType jwtType, long authId, HttpServletResponse httpResponse) {
    Cookie cookie = new Cookie(jwtType.getTokenName(),
        jwtTokenProvider.createAccessToken(jwtType, authId));
    cookie.setHttpOnly(true);
    cookie.setMaxAge((int) jwtType.getExpiredMillis() / 1000);
    httpResponse.addCookie(cookie);
    return cookie.getValue();
  }
}
