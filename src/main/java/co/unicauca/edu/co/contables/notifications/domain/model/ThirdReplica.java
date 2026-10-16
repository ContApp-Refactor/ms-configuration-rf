package co.unicauca.edu.co.contables.notifications.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Model that represents a third party replica.
 * This class contains information about a third party, such as their ID, full name, and email.
 */

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ThirdReplica {
    private Long id; // ID interno de la tabla de notificaciones 
    private Long thirdPartyId; // El ID que viene del otro microservicio, debe ser único
    private String fullName;
    private String email;
}
