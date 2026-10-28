package com.reggie.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * TraceId 过滤器
 * 负责生成/传递 TraceId，存入 MDC 实现全链路日志追踪
 * 必须在最早执行（Order=1），确保后续 Filter 和 Controller 都能拿到 traceId
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // 优先级最高，最先执行
@WebFilter(filterName = "TraceIdFilter", urlPatterns = "/*")
public class TraceIdFilter implements Filter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1. 尝试从请求头获取 TraceId（支持链路透传）
        String traceId = request.getHeader(TRACE_ID_HEADER);

        // 2. 如果没有，生成新的 TraceId
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        // 3. 放入 MDC，logback 配置 %X{traceId} 会自动输出
        MDC.put(TRACE_ID_MDC_KEY, traceId);

        // 4. 响应头也带上，方便前端/网关追踪
        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 5. 请求结束清理 MDC，防止线程池复用导致污染
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }
}