package es.plotgram.backend.seguridad.AutenticacionPorTokens;

import es.plotgram.backend.seguridad.Credenciales.ServicioCredencialesUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class FiltroAutenticacionJwt extends OncePerRequestFilter {

    @Autowired
    ServicioCredencialesUsuario servicioCredencialesUsuario;

    @Autowired
    UtilJwt utilJwt;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extraerToken(request);
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        final Claims claims;
        try {
            claims = utilJwt.extraerContenido(token);
        } catch (JwtException e) {
            limpiarAutenticacionInvalida(response);
            filterChain.doFilter(request, response);
            return;
        }

        String nombre = claims.getSubject();
        if (nombre == null || nombre.isBlank()) {
            limpiarAutenticacionInvalida(response);
            filterChain.doFilter(request, response);
            return;
        }

        final UserDetails detallesUsuario;
        try {
            detallesUsuario = servicioCredencialesUsuario.loadUserByUsername(nombre);
        } catch (UsernameNotFoundException e) {
            limpiarAutenticacionInvalida(response);
            filterChain.doFilter(request, response);
            return;
        }

        var authenticationToken = new UsernamePasswordAuthenticationToken(
                detallesUsuario,
                null,
                detallesUsuario.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if ("pg_token".equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private void limpiarAutenticacionInvalida(HttpServletResponse response) {
        SecurityContextHolder.clearContext();

        Cookie cookie = new Cookie("pg_token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}