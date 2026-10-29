package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.controller.BillingPurchaseInvoiceController;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.controller.BillingPurchaseReturnController;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.controller.BillingSaleInvoiceController;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.controller.BillingSaleReturnController;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.support.BillingInvoiceEndpointSupport;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.support.BillingPermissions;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.support.BillingReturnEndpointSupport;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Matriz de seguridad de billing (H9, SEC-1..3): los cuatro controladores (BFV, BFC, BNC, BDC) por los
 * seis alcances (R, C, U, D, EM, AN). Verifica (1) por reflexion que todo handler lleva
 * {@code @PreAuthorize} con {@code hasAuthority} (nada publico, sin permitAll/anonymous), (2) que sin
 * credenciales cada endpoint responde 401, (3) que con TODOS los permisos salvo el requerido responde
 * 403 y el caso de uso nunca se invoca, y (4) que con el permiso exacto la peticion pasa la
 * autorizacion. Los soportes de endpoints se mockean: aqui solo se prueba la capa de seguridad. Los
 * cuerpos son validos para que la validacion (400) no oculte la autorizacion.
 */
@WebMvcTest(controllers = {BillingSaleInvoiceController.class, BillingPurchaseInvoiceController.class,
        BillingSaleReturnController.class, BillingPurchaseReturnController.class})
@ContextConfiguration(classes = {BillingSaleInvoiceController.class, BillingPurchaseInvoiceController.class,
        BillingSaleReturnController.class, BillingPurchaseReturnController.class,
        BillingWebMvcTestConfig.class})
@ActiveProfiles("test")
class BillingSecurityWebMvcTest {

    private static final List<String> RESOURCES = List.of("BFV", "BFC", "BNC", "BDC");
    private static final List<String> SCOPES = List.of("R", "C", "U", "D", "EM", "AN");
    private static final List<Class<?>> CONTROLLERS = List.of(BillingSaleInvoiceController.class,
            BillingPurchaseInvoiceController.class, BillingSaleReturnController.class,
            BillingPurchaseReturnController.class);

    private static final String INVOICE_BODY = """
            {"lines":[{"productId":"1","cantidad":1}]}
            """;
    private static final String RETURN_BODY = """
            {"invoiceId":"%s","date":"2026-01-20","reason":"defectuoso",
             "lines":[{"sourceLineNo":1,"cantidad":1}]}
            """.formatted(uuid(1));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BillingInvoiceEndpointSupport invoiceSupport;
    @MockitoBean
    private BillingReturnEndpointSupport returnSupport;

    /** Un endpoint del modulo y el permiso (recurso#alcance) que lo protege. */
    record Endpoint(String resource, String scope, HttpMethod method, String path, String body) {

        String permission() {
            return resource + "#" + scope;
        }

        @Override
        public String toString() {
            return method + " " + path + " [" + permission() + "]";
        }
    }

    // ---------- catalogo de endpoints ----------

    private static List<Endpoint> endpoints() {
        List<Endpoint> all = new ArrayList<>();
        all.addAll(invoiceEndpoints("BFV", BillingSaleInvoiceController.BASE_PATH));
        all.addAll(invoiceEndpoints("BFC", BillingPurchaseInvoiceController.BASE_PATH));
        all.addAll(returnEndpoints("BNC", BillingSaleReturnController.BASE_PATH));
        all.addAll(returnEndpoints("BDC", BillingPurchaseReturnController.BASE_PATH));
        return all;
    }

    private static List<Endpoint> invoiceEndpoints(String resource, String base) {
        String id = "/" + uuid(2);
        return List.of(
                new Endpoint(resource, "C", HttpMethod.POST, base, INVOICE_BODY),
                new Endpoint(resource, "U", HttpMethod.PUT, base + id, INVOICE_BODY),
                new Endpoint(resource, "D", HttpMethod.DELETE, base + id, null),
                new Endpoint(resource, "C", HttpMethod.POST, base + id + "/duplicate", null),
                new Endpoint(resource, "EM", HttpMethod.POST, base + id + "/emit", null),
                new Endpoint(resource, "AN", HttpMethod.POST, base + id + "/annul", null),
                new Endpoint(resource, "R", HttpMethod.GET, base + id, null),
                new Endpoint(resource, "R", HttpMethod.GET, base, null),
                // sub-recursos de devoluciones de la factura: permiso de consulta de la factura
                new Endpoint(resource, "R", HttpMethod.GET, base + id + "/return-availability", null),
                new Endpoint(resource, "R", HttpMethod.GET, base + id + "/returns", null),
                new Endpoint(resource, "R", HttpMethod.GET, base + "/with-returns", null));
    }

    private static List<Endpoint> returnEndpoints(String resource, String base) {
        String id = "/" + uuid(2);
        return List.of(
                new Endpoint(resource, "C", HttpMethod.POST, base, RETURN_BODY),
                new Endpoint(resource, "U", HttpMethod.PUT, base + id, RETURN_BODY),
                new Endpoint(resource, "D", HttpMethod.DELETE, base + id, null),
                new Endpoint(resource, "EM", HttpMethod.POST, base + id + "/emit", null),
                new Endpoint(resource, "AN", HttpMethod.POST, base + id + "/annul", null),
                new Endpoint(resource, "R", HttpMethod.GET, base + id, null),
                new Endpoint(resource, "R", HttpMethod.GET, base, null));
    }

    static Stream<Arguments> endpointArguments() {
        return endpoints().stream().map(Arguments::of);
    }

    private static List<String> allPermissions() {
        return RESOURCES.stream().flatMap(r -> SCOPES.stream().map(s -> r + "#" + s)).toList();
    }

    private MockHttpServletRequestBuilder build(Endpoint endpoint) {
        MockHttpServletRequestBuilder builder = request(endpoint.method(), endpoint.path())
                .param("enterpriseId", ENTERPRISE_ID);
        if (endpoint.body() != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(endpoint.body());
        }
        return builder;
    }

    // ---------- SEC-1: reflexion, nada publico ----------

    private static List<Method> handlers(Class<?> controller) {
        return java.util.Arrays.stream(controller.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> AnnotatedElementUtils.hasAnnotation(m, RequestMapping.class))
                .toList();
    }

    @Test
    void SEC_1_every_handler_of_the_four_controllers_has_pre_authorize_has_authority() {
        for (Class<?> controller : CONTROLLERS) {
            List<Method> handlers = handlers(controller);
            assertThat(handlers).as("handlers of %s", controller.getSimpleName()).isNotEmpty();
            for (Method handler : handlers) {
                PreAuthorize annotation = handler.getAnnotation(PreAuthorize.class);
                assertThat(annotation)
                        .as("@PreAuthorize on %s.%s", controller.getSimpleName(), handler.getName())
                        .isNotNull();
                assertThat(annotation.value())
                        .as("expression on %s.%s", controller.getSimpleName(), handler.getName())
                        .startsWith("hasAuthority('")
                        .doesNotContain("permitAll", "isAnonymous", "or ", "||");
            }
        }
    }

    @Test
    void SEC_1_handler_counts_match_the_published_api_surface() {
        assertThat(handlers(BillingSaleInvoiceController.class)).hasSize(11);
        assertThat(handlers(BillingPurchaseInvoiceController.class)).hasSize(11);
        assertThat(handlers(BillingSaleReturnController.class)).hasSize(7);
        assertThat(handlers(BillingPurchaseReturnController.class)).hasSize(7);
    }

    @Test
    void SEC_1_every_handler_authority_belongs_to_its_own_resource_and_is_in_the_matrix() {
        String[] resourceByController = {"BFV", "BFC", "BNC", "BDC"};
        Set<String> declared = new LinkedHashSet<>();
        for (int i = 0; i < CONTROLLERS.size(); i++) {
            for (Method handler : handlers(CONTROLLERS.get(i))) {
                String expression = handler.getAnnotation(PreAuthorize.class).value();
                String authority = expression.substring("hasAuthority('".length(), expression.length() - 2);
                assertThat(authority).startsWith(resourceByController[i] + "#");
                assertThat(allPermissions()).contains(authority);
                declared.add(authority);
            }
        }
        // los 24 permisos de la matriz se usan al menos una vez
        assertThat(declared).containsExactlyInAnyOrderElementsOf(allPermissions());
    }

    @Test
    void SEC_2_permission_constants_cover_the_full_matrix() throws IllegalAccessException {
        Set<String> constants = new LinkedHashSet<>();
        for (java.lang.reflect.Field field : BillingPermissions.class.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()) && field.getType() == String.class) {
                String value = (String) field.get(null);
                constants.add(value.substring("hasAuthority('".length(), value.length() - 2));
            }
        }
        assertThat(constants).containsExactlyInAnyOrderElementsOf(allPermissions());
    }

    @Test
    void SEC_2_endpoint_catalog_covers_every_resource_and_scope_pair() {
        Set<String> covered = new LinkedHashSet<>();
        endpoints().forEach(e -> covered.add(e.permission()));
        assertThat(covered).containsExactlyInAnyOrderElementsOf(allPermissions());
    }

    // ---------- SEC-1: sin credenciales -> 401 ----------

    @ParameterizedTest(name = "SEC_1 anonymous {0} -> 401")
    @MethodSource("endpointArguments")
    void SEC_1_anonymous_request_gets_401_and_never_reaches_the_use_cases(Endpoint endpoint) throws Exception {
        mockMvc.perform(build(endpoint)).andExpect(status().isUnauthorized());

        verifyNoInteractions(invoiceSupport, returnSupport);
    }

    @Test
    void SEC_1_unknown_billing_path_is_not_public_either() throws Exception {
        mockMvc.perform(request(HttpMethod.GET, "/api/billing/unknown")).andExpect(status().isUnauthorized());
    }

    // ---------- SEC-2: permiso equivocado -> 403 ----------

    @ParameterizedTest(name = "SEC_2 all permissions but {0} -> 403")
    @MethodSource("endpointArguments")
    void SEC_2_user_with_every_other_permission_gets_403_and_use_case_is_not_called(Endpoint endpoint)
            throws Exception {
        String[] others = allPermissions().stream().filter(p -> !p.equals(endpoint.permission()))
                .toArray(String[]::new);

        mockMvc.perform(build(endpoint).with(user("tester").authorities(AuthorityUtils.createAuthorityList(others))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(invoiceSupport, returnSupport);
    }

    @ParameterizedTest(name = "SEC_2 authenticated without authorities {0} -> 403")
    @MethodSource("endpointArguments")
    void SEC_2_authenticated_user_without_authorities_gets_403(Endpoint endpoint) throws Exception {
        mockMvc.perform(build(endpoint).with(user("tester").authorities(AuthorityUtils.NO_AUTHORITIES)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(invoiceSupport, returnSupport);
    }

    @ParameterizedTest(name = "SEC_2 same scope on another resource {0} -> 403")
    @MethodSource("endpointArguments")
    void SEC_2_same_scope_of_a_different_resource_does_not_grant_access(Endpoint endpoint) throws Exception {
        String[] sameScopeOtherResources = RESOURCES.stream().filter(r -> !r.equals(endpoint.resource()))
                .map(r -> r + "#" + endpoint.scope()).toArray(String[]::new);

        mockMvc.perform(build(endpoint)
                        .with(user("tester").authorities(AuthorityUtils.createAuthorityList(sameScopeOtherResources))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(invoiceSupport, returnSupport);
    }

    @ParameterizedTest(name = "SEC_2 read-only user on {0}")
    @MethodSource("endpointArguments")
    void SEC_2_consult_only_user_can_only_pass_read_endpoints(Endpoint endpoint) throws Exception {
        var result = mockMvc.perform(build(endpoint)
                .with(user("tester").authorities(AuthorityUtils.createAuthorityList(endpoint.resource() + "#R"))));

        if ("R".equals(endpoint.scope())) {
            result.andExpect(status().is2xxSuccessful());
        } else {
            result.andExpect(status().isForbidden());
            verifyNoInteractions(invoiceSupport, returnSupport);
        }
    }

    // ---------- SEC-3: permiso exacto -> pasa ----------

    @ParameterizedTest(name = "SEC_3 exact permission {0} -> authorized")
    @MethodSource("endpointArguments")
    void SEC_3_user_with_exactly_the_required_permission_passes_authorization(Endpoint endpoint)
            throws Exception {
        mockMvc.perform(build(endpoint)
                        .with(user("tester").authorities(AuthorityUtils.createAuthorityList(endpoint.permission()))))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void SEC_3_consult_only_user_cannot_emit_an_invoice_so_it_stays_in_draft() throws Exception {
        for (String base : List.of(BillingSaleInvoiceController.BASE_PATH,
                BillingPurchaseInvoiceController.BASE_PATH)) {
            String resource = base.contains("sale") ? "BFV" : "BFC";
            mockMvc.perform(request(HttpMethod.POST, base + "/" + uuid(2) + "/emit")
                            .param("enterpriseId", ENTERPRISE_ID)
                            .with(user("tester").authorities(AuthorityUtils.createAuthorityList(resource + "#R"))))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(invoiceSupport);
    }
}
