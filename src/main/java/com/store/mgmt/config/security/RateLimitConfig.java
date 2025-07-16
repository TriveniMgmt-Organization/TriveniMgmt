package com.store.mgmt.config.security;

//import io.github.bucket4j.Bandwidth;
//import io.github.bucket4j.Bucket;
//import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {
//public class RateLimitConfig implements WebMvcConfigurer {
//
//    @Bean
//    public Filter rateLimitFilter() {
//        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
//        Bucket bucket = Bucket.builder().addLimit(limit).build();
//
//        return new Filter() {
//            @Override
//            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
//                if (bucket.tryConsume(1)) {
//                    try {
//                        chain.doFilter(request, response);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                } else {
//                    HttpServletResponse httpResponse = (HttpServletResponse) response;
//                    httpResponse.setStatus(429);
//                    try {
//                        httpResponse.getWriter().write("Too Many Requests");
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        };
//    }
//
//    @Bean
//    public FilterRegistrationBean<Filter> rateLimitFilterRegistration(Filter rateLimitFilter) {
//        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
//        registrationBean.setFilter(rateLimitFilter);
//        registrationBean.addUrlPatterns("/api/v1/auth/login", "/api/v1/auth/refresh");
//        registrationBean.setOrder(1);
//        return registrationBean;
//    }
}