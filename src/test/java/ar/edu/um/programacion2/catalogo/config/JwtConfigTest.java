package ar.edu.um.programacion2.catalogo.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtConfigTest {

    @Test
    void tomaLasAutoridadesDelClaimAuthSinPrefijo() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS512")
                .subject("paciente")
                .claim("auth", List.of("ROLE_USER"))
                .build();

        assertThat(new JwtConfig().jwtAuthenticationConverter().convert(jwt).getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER");
    }
}
