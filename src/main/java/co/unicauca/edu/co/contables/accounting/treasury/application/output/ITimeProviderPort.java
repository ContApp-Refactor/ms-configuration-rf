package co.unicauca.edu.co.contables.accounting.treasury.application.output;

import java.time.LocalDate;

public interface ITimeProviderPort {
    LocalDate today();
}
