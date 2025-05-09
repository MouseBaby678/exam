package com.baymax.exam.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.baymax.exam.common.core.base.SecurityConstants;
import com.nimbusds.jose.JWSObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.text.ParseException;

/**
 * @author ：Baymax
 * @date ：Created in 2022/10/11 19:26
 * @description：将登录用户的JWT转化成用户信息的全局过滤器
 * @modified By：
 * @version:
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(SecurityConstants.JWT_TOKEN_HEADER);
        if(token==null){
            token=exchange.getRequest().getQueryParams().getFirst("token");
        }
        if (StrUtil.isEmpty(token)) {
            return chain.filter(exchange);
        }
        try {
            //从token中解析用户信息并设置到Header中去
            String realToken = token.replace(SecurityConstants.JWT_TOKEN_PREFIX, "");
            // 检查token格式是否正确
            if (realToken.contains(".")) {
                JWSObject jwsObject = JWSObject.parse(realToken);
                String userStr = jwsObject.getPayload().toString();
                log.info("AuthGlobalFilter.filter() user:{}", userStr);
                ServerHttpRequest request = exchange.getRequest().mutate().header(SecurityConstants.USER_TOKEN_HEADER, userStr).build();
                exchange = exchange.mutate().request(request).build();
            } else {
                log.warn("Invalid token format: {}", realToken);
            }
        } catch (ParseException e) {
            log.error("JWT解析异常: {}", e.getMessage());
            // 解析异常时不应阻止请求继续,只记录日志
        } catch (Exception e) {
            log.error("处理token时发生未知异常", e);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
