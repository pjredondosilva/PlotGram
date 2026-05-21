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
/**
 * Componente de utilidad encargado de la generación, firma y parseo
 * de tokens JWT (JSON Web Tokens) para la autenticación de usuarios.
 */
@Component
public class UtilJwt {

    private final SecretKey claveFirmadoTokens;
    private final JwtParser parser;

    /**
     * Constructor que inicializa la clave de firmado HMAC-SHA a partir del secreto
     * en Base64 y configura el parser con el margen de desfase de reloj.
     *
     * @param secretoBase64 Clave secreta codificada en Base64.
     * @param clockSkewSeconds Margen de tolerancia en segundos para la expiración.
     */
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

    /**
     * Genera un nuevo token JWT a partir del nombre del usuario y sus claims,
     * especificando una validez en minutos.
     *
     * @param usuario Identificador o nombre del usuario.
     * @param claims Metadatos adicionales a incluir en el cuerpo del token.
     * @param tiempoExpiracionMin Tiempo de validez del token en minutos.
     * @return El token JWT generado y firmado en formato String.
     */
    public String crearToken(String usuario, Map<String, ?> claims, int tiempoExpiracionMin) {
        var ahora = LocalDateTime.now().atZone(ZoneId.systemDefault());
        Date exp = Date.from(ahora.plusMinutes(tiempoExpiracionMin).toInstant());
        return crearTokenConExp(usuario, claims, exp);
    }

    /**
     * Genera y firma un token JWT especificando una fecha/hora exacta de expiración.
     *
     * @param usuario Identificador o nombre del usuario.
     * @param claims Metadatos adicionales a incluir.
     * @param expiracion Fecha y hora exactas de expiración.
     * @return El token JWT firmado.
     */
    public String crearTokenConExp(String usuario, Map<String, ?> claims, Date expiracion) {
        return Jwts.builder()
                .claims(claims)
                .subject(usuario)
                .issuedAt(new Date())
                .expiration(expiracion)
                .signWith(claveFirmadoTokens)
                .compact();
    }

    /**
     * Parsea un token JWT, valida su firma y extrae las Claims de su contenido.
     *
     * @param token Token JWT en formato String.
     * @return Las claims del token si es válido.
     * @throws io.jsonwebtoken.JwtException Si el token está caducado, mal firmado o es inválido.
     */
    public Claims extraerContenido(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }
}
