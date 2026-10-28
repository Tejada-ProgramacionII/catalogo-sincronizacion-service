package ar.edu.um.programacion2.catalogo.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import ar.edu.um.programacion2.catalogo.profesional.application.service.ProfesionalService;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Pagina;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;

@SpringBootTest
class SecurityConfigTest {

    private static final String URL = "/api/profesionales";
    private static final String OTRO_SECRETO = Base64.getEncoder()
            .encodeToString("otra-clave-que-no-es-la-de-turnos-ni-la-del-catalogo-para-tests-64b".getBytes());

    @Autowired
    private WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    @Value("${jwt.secret}")
    private String secreto;

    @MockitoBean
    private ProfesionalService profesionalService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
    }

    private static String token(String secretoBase64, Instant venceEn) {
        SecretKeySpec clave = new SecretKeySpec(Base64.getDecoder().decode(secretoBase64), MacAlgorithm.HS512.getName());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("paciente")
                .issuedAt(venceEn.minus(1, ChronoUnit.DAYS))
                .expiresAt(venceEn)
                .claim("auth", List.of("ROLE_USER"))
                .build();
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave))
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS512).build(), claims))
                .getTokenValue();
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void laConsultaDeUnProfesionalSinTokenResponde401() throws Exception {
        mockMvc.perform(get(URL + "/15")).andExpect(status().isUnauthorized());
    }

    @Test
    void conUnTokenValidoResponde200() throws Exception {
        when(profesionalService.buscarProfesionales(any(), anyInt(), anyInt()))
                .thenReturn(Pagina.<Profesional>builder().contenido(List.of()).numero(0).tamanio(20).build());

        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + token(secreto, Instant.now().plus(1, ChronoUnit.HOURS))))
                .andExpect(status().isOk());
    }

    @Test
    void conUnTokenFirmadoConOtroSecretoResponde401() throws Exception {
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + token(OTRO_SECRETO, Instant.now().plus(1, ChronoUnit.HOURS))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conUnTokenVencidoResponde401() throws Exception {
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + token(secreto, Instant.now().minus(1, ChronoUnit.HOURS))))
                .andExpect(status().isUnauthorized());
    }
}
