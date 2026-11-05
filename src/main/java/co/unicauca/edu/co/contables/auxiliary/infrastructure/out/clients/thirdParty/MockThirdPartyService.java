package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.clients.thirdParty;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IThirdPartyInfoClient;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.thirdParty.ThirdParty;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación simulada de {@link IThirdPartyInfoClient} que reemplaza al
 * microservicio real de terceros mientras este no esté disponible.
 *
 * <p>Carga un catálogo desde {@code classpath:mock/third-parties.json} al
 * arrancar y resuelve nombres por ID. Para identificadores no presentes en
 * el catálogo se genera un nombre determinístico ("Tercero N° {id}") con un
 * tipo de documento aproximado según la longitud del ID, de modo que la
 * estrategia {@code ThirdPartyStrategy} siempre obtenga un valor utilizable.</p>
 *
 * <p>Se marca como {@link Primary} para tomar precedencia sobre
 * {@code ThirdPartyService} (cliente WebClient real). Se puede desactivar
 * estableciendo {@code services.third-service.mock-enabled=false}.</p>
 */
@Component
@Primary
@ConditionalOnProperty(
        name = "services.third-service.mock-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MockThirdPartyService implements IThirdPartyInfoClient {

    private static final Logger log = LoggerFactory.getLogger(MockThirdPartyService.class);
    private static final String CATALOG_PATH = "mock/third-parties.json";

    private final ObjectMapper objectMapper;
    private final Map<Long, ThirdParty> catalog = new ConcurrentHashMap<>();

    public MockThirdPartyService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadCatalog() {
        ClassPathResource resource = new ClassPathResource(CATALOG_PATH);
        if (!resource.exists()) {
            log.warn("[MockThirdPartyService] Catálogo no encontrado en classpath:{} — se usará fallback determinístico para todos los IDs.", CATALOG_PATH);
            return;
        }
        try (InputStream in = resource.getInputStream()) {
            List<ThirdParty> entries = objectMapper.readValue(in, new TypeReference<>() {});
            Map<Long, ThirdParty> loaded = new HashMap<>();
            for (ThirdParty tp : entries) {
                if (tp != null && tp.getId() != null) {
                    loaded.put(tp.getId(), tp);
                }
            }
            catalog.putAll(loaded);
            log.info("[MockThirdPartyService] Catálogo cargado con {} terceros desde classpath:{}", catalog.size(), CATALOG_PATH);
        } catch (Exception e) {
            log.error("[MockThirdPartyService] Error cargando catálogo de terceros: {}", e.getMessage(), e);
        }
    }

    @Override
    public ThirdParty getThirdPartyById(Long thirdPartyId) {
        if (thirdPartyId == null) {
            return null;
        }
        return catalog.computeIfAbsent(thirdPartyId, this::buildFallback);
    }

    private ThirdParty buildFallback(Long id) {
        String idStr = String.valueOf(id);
        boolean looksLikeNit = idStr.length() >= 9 && (idStr.startsWith("8") || idStr.startsWith("9"));
        String typeId = looksLikeNit ? "NIT" : "CC";
        String name = "TERCERO N° " + idStr;
        return new ThirdParty(id, name, id, typeId);
    }
}
