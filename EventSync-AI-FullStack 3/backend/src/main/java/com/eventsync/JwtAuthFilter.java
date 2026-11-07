package com.eventsync;
import jakarta.servlet.FilterChain; import jakarta.servlet.ServletException; import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException; import java.util.List;
@Component public class JwtAuthFilter extends OncePerRequestFilter {
 private final TokenService tokens; private final AppUserRepository users; public JwtAuthFilter(TokenService tokens,AppUserRepository users){this.tokens=tokens;this.users=users;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException {
 String h=req.getHeader("Authorization"); if(h!=null && h.startsWith("Bearer ")) { String[] c=tokens.verify(h.substring(7)); if(c!=null) { var user=users.findByEmailIgnoreCase(c[0]).orElse(null); if(user!=null && user.tokenVersion==Integer.parseInt(c[3]) && user.role.equals(c[1])) { var auth=new UsernamePasswordAuthenticationToken(c[0],null,List.of(new SimpleGrantedAuthority("ROLE_"+c[1]))); auth.setDetails(c); SecurityContextHolder.getContext().setAuthentication(auth); } } } chain.doFilter(req,res); }
}
