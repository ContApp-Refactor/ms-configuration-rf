package co.unicauca.edu.co.contables.keycloak.config;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Advice de sanitización de entrada para el módulo keycloak.
 * <p>
 * Equivale al {@code InputSanitizerAdvice} del origen
 * ({@code com.security.keycloak.config}). En el microservicio original este
 * advice se aplicaba a todos los controladores sin restricción de paquete.
 * En el monolito, un {@code @ControllerAdvice} sin {@code basePackages}
 * afectaría a los ~15 módulos, cambiando el comportamiento transversal de
 * binding (trim + blank→null) en dominios ajenos.
 * </p>
 * <p>
 * Por eso se acota con {@code @ControllerAdvice(basePackages =
 * "co.unicauca.edu.co.contables.keycloak")}: replica exactamente el alcance
 * del origen solo para este módulo. No se añade {@code @Order} porque el
 * origen tampoco lo tenía y el advice no compite por precedencia de
 * excepciones.
 * </p>
 */
@ControllerAdvice(basePackages = "co.unicauca.edu.co.contables.keycloak")
public class KeycloakInputSanitizerAdvice {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

}