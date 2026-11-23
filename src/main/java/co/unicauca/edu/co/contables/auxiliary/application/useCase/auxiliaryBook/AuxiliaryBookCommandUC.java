package co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.auxiliaryBook.IAuxiliaryBookCommandPort;
import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IAccountingInfoClient;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.AuxiliaryBookProcessor;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EState;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ETypeEvent;
import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBook.IAuxiliaryBookCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookHistory.IAuxiliaryBookHistoryCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookLog.IAuxiliaryBookLogCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IMessageServicePort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.config.i18n.MessageKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Caso de uso de escritura para Libros Auxiliares.
 *
 * Implementa el puerto de entrada {@link IAuxiliaryBookCommandPort} y
 * coordina el registro de un libro auxiliar, la creación de su historial
 * y logs asociados, y la generación de la información contable delegando
 * en {@link AuxiliaryBookProcessor} la lógica específica por tipo de libro.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuxiliaryBookCommandUC implements IAuxiliaryBookCommandPort {

    private final IAuxiliaryBookCommandRepositoryPort abCommandRepositoryPort;
    private final IAuxiliaryBookLogCommandRepositoryPort abLogCommandRepositoryPort;
    private final IAuxiliaryBookHistoryCommandRepositoryPort abHistoryCommandRepositoryPort;

    private final IAccountingInfoClient accountingInfoQueryPort;
    private final AuxiliaryBookProcessor auxiliaryBookProcessor;

    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageServicePort;

    @Override
    public AuxiliaryBook registerAuxiliaryBook(AuxiliaryBook auxiliaryBook) {

        if(auxiliaryBook == null){
            this.formatterResultOutputPort.returnErrorGenericResponse(400, this.messageServicePort.getMessage(
                    MessageKeys.ERROR_NULL_VALUE,
                    "AuxiliaryBook"
            ));
        }

        if (auxiliaryBook.getPublicId() == null || auxiliaryBook.getPublicId().isEmpty()) {
            auxiliaryBook.setPublicId(UUID.randomUUID().toString());
        }

        AuxiliaryBook abRegistered = abCommandRepositoryPort.registerAuxiliaryBook(auxiliaryBook);

        AuxiliaryBookHistory newHistory = AuxiliaryBookHistory.builder()
                .publicId(UUID.randomUUID().toString())
                .auxiliaryBook(abRegistered)
                .state(EState.GENERATED)
                .build();

        this.abHistoryCommandRepositoryPort.registerAuxiliaryBookHistory(newHistory);

        String logMessage = "Auxiliary book registered successfully.";
        this.createLog(abRegistered, ETypeEvent.REGISTERED, logMessage);

        return abRegistered;
    }

    @Override
    public List<?> genereteAuxiliaryBookInfo(AuxiliaryBook auxiliaryBook) {

        log.info("Starting generation for book: {}", auxiliaryBook.getPublicId());
        this.createLog(auxiliaryBook, ETypeEvent.GENERATING, "Starting data generation process.");

        try {
            List<?> resultData = this.auxiliaryBookProcessor.processAuxiliaryBookData(this.accountingInfoQueryPort, auxiliaryBook);

            String successMessage = "Data generation successful. " + (resultData != null ? resultData.size() : 0) + " items processed.";
            this.createLog(auxiliaryBook, ETypeEvent.SUCCESS_GENERATION, successMessage);

            return resultData;

        } catch (IllegalArgumentException e) {
            log.error("Invalid argument for book: {}", auxiliaryBook.getPublicId(), e);
            this.createLog(auxiliaryBook, ETypeEvent.ERROR_GENERATION, "Invalid argument provided.");
            throw e;   // ← deja que GlobalExceptionHandler lo convierta en 400
        } catch (Exception e) {
            log.error("Unexpected error for book: {}", auxiliaryBook.getPublicId(), e);
            this.createLog(auxiliaryBook, ETypeEvent.ERROR_GENERATION, "Unexpected error occurred.");

            this.formatterResultOutputPort.returnErrorGenericResponse(500, this.messageServicePort.getMessage(
                    MessageKeys.ERROR_GENERIC,
                    "auxiliary book generation",                                           // {0}
                    e.getMessage() != null ? e.getMessage() : "unknown cause"              // {1}
            ));
        }
        return null;
    }

    /**
     * Método helper privado para estandarizar la creación de logs.
     * Cada entrada de log tendrá su propio publicId único.
     */
    private void createLog(AuxiliaryBook book, ETypeEvent event, String message) {
        AuxiliaryBookLog newLog = AuxiliaryBookLog.builder()
                .publicId(UUID.randomUUID().toString()) // <-- Asigna UUID al Log
                .auxiliaryBook(book)
                .ETypeEvent(event)
                .message(message)
                .build();

        abLogCommandRepositoryPort.registerAuxiliaryBookLog(newLog);
    }
}