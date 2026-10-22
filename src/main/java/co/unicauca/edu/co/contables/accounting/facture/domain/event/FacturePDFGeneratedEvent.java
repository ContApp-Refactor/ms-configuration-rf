package co.unicauca.edu.co.contables.accounting.facture.domain.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacturePDFGeneratedEvent {
    private Long factId;
    private LocalDateTime date;

    public FacturePDFGeneratedEvent(Long factId){
        this.factId = factId;
        this.date = LocalDateTime.now();
    }
}
