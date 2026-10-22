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
import co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper.SubjectDomainMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.ISubjectService;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.controller.SubjectController;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.EnterpriseGlobalExceptionHandler;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.SubjectCodeAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.SubjectNotFoundException;

/**
 * Contrato REST de materias y orden de los @ControllerAdvice.
 *
 * GlobalExceptionHandler (global) declara @ExceptionHandler(Exception.class)
 * sin basePackages, y ExceptionHandlerExceptionResolver ordena los advice
 * con AnnotationAwareOrderComparator. Si el global quedara primero,
 * SubjectNotFoundException y SubjectCodeAlreadyExistsException devolverían
 * 500 en vez de 404/409.
 */
class SubjectControllerAdviceTest {

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

    @Test
    void getAllDevuelve200ConElContratoDelFrontend() throws Exception {
        ISubjectService service = mock(ISubjectService.class);
        when(service.getAll()).thenReturn(List.of(Subject.builder()
                .code("MAT01")
                .name("CONTABILIDAD I")
                .build()));

        mockMvc(service).perform(get("/api/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("MAT01"))
                .andExpect(jsonPath("$[0].name").value("CONTABILIDAD I"));
    }

    @Test
    void getAllAdmiteBarraFinal() throws Exception {
        ISubjectService service = mock(ISubjectService.class);
        when(service.getAll()).thenReturn(List.of());

        mockMvc(service).perform(get("/api/subjects/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void materiaInexistenteDevuelve404YNo500() throws Exception {
        ISubjectService service = mock(ISubjectService.class);
        when(service.getByCode("NOEXISTE")).thenThrow(new SubjectNotFoundException("NOEXISTE"));

        mockMvc(service).perform(get("/api/subjects/NOEXISTE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void codigoDuplicadoDevuelve409YNo500() throws Exception {
        ISubjectService service = mock(ISubjectService.class);
        when(service.create(any())).thenThrow(new SubjectCodeAlreadyExistsException("MAT01"));

        mockMvc(service).perform(post("/api/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"MAT01\",\"name\":\"CONTABILIDAD I\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    private MockMvc mockMvc(ISubjectService service) {
        SubjectDomainMapper mapper = Mappers.getMapper(SubjectDomainMapper.class);
        return MockMvcBuilders.standaloneSetup(new SubjectController(service, mapper))
                .setControllerAdvice(new GlobalExceptionHandler(), new EnterpriseGlobalExceptionHandler())
                .build();
    }
}
