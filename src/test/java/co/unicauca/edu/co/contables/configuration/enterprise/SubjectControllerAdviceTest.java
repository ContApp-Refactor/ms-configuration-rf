package co.unicauca.edu.co.contables.configuration.enterprise;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.unicauca.edu.co.contables.commons.exceptions.GlobalExceptionHandler;
import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.ISubjectManagerPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.SubjectController;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.exception.EnterpriseGlobalExceptionHandler;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.exception.SubjectCodeAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.exception.SubjectNotFoundException;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.ISubjectRestMapper;

/**
 * Contrato REST de materias y <strong>orden de los {@code @ControllerAdvice}</strong>.
 *
 * <p>{@code GlobalExceptionHandler} (global) declara
 * {@code @ExceptionHandler(Exception.class)} <strong>sin</strong>
 * {@code basePackages}, y {@code ExceptionHandlerExceptionResolver} ordena los
 * advice con {@link AnnotationAwareOrderComparator}. Si el global quedara
 * primero, {@code SubjectNotFoundException} y
 * {@code SubjectCodeAlreadyExistsException} devolverían <strong>500</strong> en
 * vez de 404/409 y los {@code @ResponseStatus} del microservicio de origen
 * quedarían inertes.
 *
 * <p>El test arranca con los dos advice y el <strong>global primero</strong> a
 * propósito: solo pasa si el {@code @Order(HIGHEST_PRECEDENCE)} del advice de
 * enterprise manda, que es exactamente lo que hace Spring en la aplicación real.
 */
class SubjectControllerAdviceTest {

    /**
     * El orden real que usa Spring para elegir advice (mismo comparador que
     * {@code ExceptionHandlerExceptionResolver}) deja el de enterprise primero.
     */
    @Test
    void elAdviceDeEnterpriseSeOrdenaAntesDelGlobal() {
        List<Object> advices = new java.util.ArrayList<>(List.of(
                new GlobalExceptionHandler(),
                new EnterpriseGlobalExceptionHandler()));
        AnnotationAwareOrderComparator.sort(advices);

        org.assertj.core.api.Assertions.assertThat(advices.get(0))
                .isInstanceOf(EnterpriseGlobalExceptionHandler.class)
                .as("el advice acotado debe ir antes que el global");
    }

    /**
     * El listado conserva el contrato que consume el frontend: {@code [{code,name}]}.
     */
    @Test
    void getAllDevuelve200ConElContratoDelFrontend() throws Exception {
        ISubjectManagerPort port = mock(ISubjectManagerPort.class);
        when(port.getAll()).thenReturn(List.of(Subject.builder()
                .code("MAT01")
                .name("CONTABILIDAD I")
                .build()));

        mockMvc(port).perform(get("/api/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("MAT01"))
                .andExpect(jsonPath("$[0].name").value("CONTABILIDAD I"));
    }

    /**
     * La barra final que usa el frontend ({@code subjects/}) también lista.
     */
    @Test
    void getAllAdmiteBarraFinal() throws Exception {
        ISubjectManagerPort port = mock(ISubjectManagerPort.class);
        when(port.getAll()).thenReturn(List.of());

        mockMvc(port).perform(get("/api/subjects/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    /**
     * Regresión del orden de advice: tiene que salir 404, no 500.
     */
    @Test
    void materiaInexistenteDevuelve404YNo500() throws Exception {
        ISubjectManagerPort port = mock(ISubjectManagerPort.class);
        when(port.getByCode("NOEXISTE")).thenThrow(new SubjectNotFoundException("NOEXISTE"));

        mockMvc(port).perform(get("/api/subjects/NOEXISTE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    /**
     * Regresión del orden de advice: código duplicado tiene que salir 409, no 500.
     */
    @Test
    void codigoDuplicadoDevuelve409YNo500() throws Exception {
        ISubjectManagerPort port = mock(ISubjectManagerPort.class);
        when(port.create(any())).thenThrow(new SubjectCodeAlreadyExistsException("MAT01"));

        mockMvc(port).perform(post("/api/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"MAT01\",\"name\":\"CONTABILIDAD I\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    /**
     * Construye el MockMvc con los <strong>dos</strong> advice reales y el global
     * primero, replicando el peor caso de orden.
     *
     * @param port el puerto de entrada simulado
     * @return el MockMvc listo
     */
    private MockMvc mockMvc(ISubjectManagerPort port) {
        ISubjectRestMapper restMapper = Mappers.getMapper(ISubjectRestMapper.class);
        return MockMvcBuilders.standaloneSetup(new SubjectController(port, restMapper))
                .setControllerAdvice(new GlobalExceptionHandler(), new EnterpriseGlobalExceptionHandler())
                .build();
    }
}
