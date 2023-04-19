package com.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.compiler.ast.Variable;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 拦截请求，只有登录等才能访问
 */

@Slf4j
@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器，能匹配通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //获取当前的请求rul
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}", requestURI);

        //设置不需要拦截的rul
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**"
        };
        //判断是否需要放行
        Boolean check = check(urls, requestURI);

        //不需要处理，放行
        if (check) {
            log.info("本次请求为:{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //判断登录状态
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录，用户Id为{}", request.getSession().getAttribute("employee"));

            //获取用户id，存入线程中，因为一个请求的线程id是相同的
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }

        log.info("用户未登录。");
        //还没有登录,通过输出流的方式，向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

    }


    /**
     * 路径匹配，检查本次请求是否放行
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public Boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;

    }


}



