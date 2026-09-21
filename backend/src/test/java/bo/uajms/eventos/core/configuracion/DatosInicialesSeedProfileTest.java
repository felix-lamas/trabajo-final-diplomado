package bo.uajms.eventos.core.configuracion;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.StandardEnvironment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatosInicialesSeedProfileTest {

    @Test
    void debeActivarseExclusivamenteConElPerfilDemo() {
        Profile profile = DatosInicialesSeed.class.getAnnotation(Profile.class);

        assertNotNull(profile, "El seed demo debe estar restringido por un perfil Spring");
        assertArrayEquals(new String[]{"demo"}, profile.value());
    }

    @Test
    void noDebeRegistrarseEnPerfilesNormales() {
        assertFalse(esCandidatoConPerfil("dev"));
        assertFalse(esCandidatoConPerfil("prod"));
    }

    @Test
    void debeRegistrarseCuandoElPerfilDemoSeActivaExplicitamente() {
        assertTrue(esCandidatoConPerfil("demo"));
    }

    private boolean esCandidatoConPerfil(String perfil) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.setActiveProfiles(perfil);

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(true, environment);

        return scanner.findCandidateComponents("bo.uajms.eventos.core.configuracion")
                .stream()
                .anyMatch(bean -> DatosInicialesSeed.class.getName().equals(bean.getBeanClassName()));
    }
}
