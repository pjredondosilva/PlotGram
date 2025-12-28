package es.plotgram.backend.seguridad.AutenticacionPorTokens;

import es.plotgram.backend.seguridad.Credenciales.ServicioCredencialesUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class FiltroAutenticacionJwt extends OncePerRequestFilter {

    @Autowired
    ServicioCredencialesUsuario servicioCredencialesUsuario;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            Claims claims;
            try {
                String token = authorizationHeader.substring(7);
                claims = UtilJwt.extraerContenido(token);
            } catch (JwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String dni = claims.getSubject();
            UserDetails detallesUsuario = servicioCredencialesUsuario.loadUserByUsername(dni);

            var authenticationToken = new UsernamePasswordAuthenticationToken(
                    detallesUsuario,
                    null,
                    detallesUsuario.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}
