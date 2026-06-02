
package com.example.ratelimiter.limiter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

//@Component
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimiterManager manager;
  public RateLimitFilter(RateLimiterManager manager){ this.manager = manager; }

  @Override
  protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
      throws ServletException, IOException {
    String key="ip:"+request.getRemoteAddr();
    if(manager.tryConsume(key,1)){
      chain.doFilter(request,response);
    } else {
      response.setStatus(429);
      response.getWriter().write("Rate limit exceeded");
    }
  }
}
