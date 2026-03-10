package es.plotgram.backend.seguridad.AutenticacionPorTokens;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
@Component
public class UtilJwt {

    private final SecretKey claveFirmadoTokens;
    private final JwtParser parser;

    public UtilJwt(
            @Value("${app.auth.jwt.secret}") String secretoBase64,
            @Value("${app.auth.jwt.clockSkewSeconds}") long clockSkewSeconds
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretoBase64);
        this.claveFirmadoTokens = Keys.hmacShaKeyFor(keyBytes);
        this.parser = Jwts.parser()
                .verifyWith(claveFirmadoTokens)
                .clockSkewSeconds(clockSkewSeconds)
                .build();
    }


    public String crearToken(String usuario, Map<String, ?> claims, int tiempoExpiracionMin) {
        var ahora = LocalDateTime.now().atZone(ZoneId.systemDefault());
        Date exp = Date.from(ahora.plusMinutes(tiempoExpiracionMin).toInstant());
        return crearTokenConExp(usuario, claims, exp);
    }

    public String crearTokenConExp(String usuario, Map<String, ?> claims, Date expiracion) {
        return Jwts.builder()
                .claims(claims)
                .subject(usuario)
                .issuedAt(new Date())
                .expiration(expiracion)
                .signWith(claveFirmadoTokens)
                .compact();
    }

    public Claims extraerContenido(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }
}
