package es.plotgram.backend.seguridad.AutenticacionPorTokens;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

public class UtilJwt {

    private static final SecretKey claveFirmadoTokens = Jwts.SIG.HS256.key().build();

    public static String crearToken(String usuario, Map<String, ?> claims, int tiempoExpiracionMin) {
        var ahora = LocalDateTime.now().atZone(ZoneId.systemDefault());
        Date exp = Date.from(ahora.plusMinutes(tiempoExpiracionMin).toInstant());
        return crearTokenConExp(usuario, claims, exp);
    }

    public static String crearTokenConExp(String usuario, Map<String, ?> claims, Date expiracion) {
        return Jwts.builder()
                .claims(claims)
                .subject(usuario)
                .issuedAt(new Date())
                .expiration(expiracion)
                .signWith(claveFirmadoTokens)
                .compact();
    }

    public static Claims extraerContenido(String token) {
        return Jwts.parser()
                .verifyWith(claveFirmadoTokens)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
