package co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @brief Data Transfer Object (DTO) for Third Updated Event.
 */

@Getter
@Setter
@Builder
public class ThirdUpdatedEventDto {
    private Long thirdId;
    private String entId;
    private String fullName;
    private String email;
    private Boolean state;
}
