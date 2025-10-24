
package com.shanzhu.em.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.shanzhu.em.constants.RedisConstants;
import com.shanzhu.em.constants.Status;
import com.shanzhu.em.entity.User;
import com.shanzhu.em.exception.BizException;
import com.shanzhu.em.utils.UserHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * jwt拦截器
 * 处理 token 检验等
 *
 * 变更点：
 * 1) 放行非 HandlerMethod（静态资源）
 * 2) 放行 OPTIONS 预检
 * 3) 放行 /login 与 /register（兜底白名单）
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    RedisTemplate<String, User> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1) 不是映射到方法（如静态资源），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 2) 预检请求放行（CORS）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 3) 兜底白名单：登录 / 注册 放行
        String uri = request.getRequestURI();
        if (uri.startsWith("/login") || uri.startsWith("/register")) {
            return true;
        }

        // 4) 开始做 token 校验
        String token = request.getHeader("token");
        if (!StringUtils.hasLength(token)) {
            throw new BizException(Status.TOKEN_ERROR, "token失效,请重新登陆");
        }

        // 通过 token 从 redis 拿用户，写入 ThreadLocal
        User user = redisTemplate.opsForValue().get(RedisConstants.USER_TOKEN_KEY + token);
        if (user == null) {
            throw new BizException(Status.TOKEN_ERROR, "token失效,请重新登陆");
        }
        UserHolder.saveUser(user);
        // 重置过期时间
        redisTemplate.expire(RedisConstants.USER_TOKEN_KEY + token, RedisConstants.USER_TOKEN_TTL, TimeUnit.MINUTES);

        // 验证 JWT
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getUsername())).build();
        try {
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new BizException(Status.TOKEN_ERROR, "token验证失败，请重新登陆");
        }

        return true;
    }
}

