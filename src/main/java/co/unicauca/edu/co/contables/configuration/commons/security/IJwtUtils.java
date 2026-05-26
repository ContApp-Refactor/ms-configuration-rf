package co.unicauca.edu.co.contables.configuration.commons.security;

import java.util.List;

/** @brief Interfaz para utilidades de JWT */
public interface IJwtUtils {

    String getId();

    String getToken();

    String getUsername();

    List<String> getRealmRoles();
}