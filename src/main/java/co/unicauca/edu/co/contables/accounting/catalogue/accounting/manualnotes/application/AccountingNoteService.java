package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.application;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.dto.AccountingNoteDetailRequest;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.dto.AccountingNoteRequest;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.exception.AccountingNoteNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.exception.DuplicateAccountingNoteException;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.entity.AccountingNoteDetailEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.entity.AccountingNoteEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.entity.DocumentTypeEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.repository.AccountingNoteRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.repository.RegcontableDocumentTypeRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port.DocumentTypePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

@Service
public class AccountingNoteService {

    private final RegcontableValidationFacade validationFacade;
    private final AccountingNoteRepository noteRepository;
    private final RegcontableDocumentTypeRepository documentTypeRepository;
    private final DocumentTypePort documentTypePort;

    public AccountingNoteService(RegcontableValidationFacade validationFacade,
                                 AccountingNoteRepository noteRepository,
                                 RegcontableDocumentTypeRepository documentTypeRepository,
                                 DocumentTypePort documentTypePort) {
        this.validationFacade = validationFacade;
        this.noteRepository = noteRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.documentTypePort = documentTypePort;
    }

    @Transactional
    public AccountingNoteResponse create(AccountingNoteRequest request) {
        validateDoubleEntry(request);
        validationFacade.validateDetails(request.details(), request.enterpriseId());

        if (noteRepository.existsByOriginModuleAndOriginId(request.originModule(), request.originId())) {
            throw new DuplicateAccountingNoteException(request.originModule(), request.originId());
        }

        DocumentTypePort.DocumentType configuredType = documentTypePort
                .findActiveByPrefix(request.documentTypeCode(), request.enterpriseId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El tipo de documento " + request.documentTypeCode()
                                + " no está configurado o está inactivo para la empresa."));

        DocumentTypeEntity documentType = documentTypeRepository
                .findByCodeWithLock(request.documentTypeCode(), request.enterpriseId())
                .orElseGet(() -> {
                    DocumentTypeEntity created = new DocumentTypeEntity();
                    created.setCode(request.documentTypeCode());
                    created.setEnterpriseId(request.enterpriseId());
                    created.setCurrentSequence(0L);
                    return created;
                });
        documentType.setPrefix(configuredType.prefix());
        documentType = documentTypeRepository.save(documentType);
        long sequence = documentType.getCurrentSequence() + 1;
        documentType.setCurrentSequence(sequence);

        AccountingNoteEntity note = new AccountingNoteEntity(
                documentType.getPrefix() + String.format("%04d", sequence),
                request.documentTypeCode(), request.date(), request.concept(),
                request.originModule(), request.originId(), request.enterpriseId());
        if ("contapp-frontend".equals(request.originModule())) {
            note.setStatus(AccountingNoteEntity.NoteStatus.DRAFT);
        }

        note.replaceDetails(toEntities(request.details()));

        return toResponse(noteRepository.save(note));
    }

    @Transactional
    public AccountingNoteResponse updateDraft(Long id, AccountingNoteRequest request) {
        validateDoubleEntry(request);
        validationFacade.validateDetails(request.details(), request.enterpriseId());
        AccountingNoteEntity note = noteRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AccountingNoteNotFoundException(id));
        if (note.getStatus() != AccountingNoteEntity.NoteStatus.DRAFT) {
            throw new IllegalStateException("La nota ya fue contabilizada o anulada y no se puede editar.");
        }
        if (!"contapp-frontend".equals(note.getOriginModule())) {
            throw new IllegalStateException("Solo se pueden editar notas manuales creadas desde el frontend.");
        }
        if (!Objects.equals(note.getEnterpriseId(), request.enterpriseId())) {
            throw new IllegalArgumentException("La nota no pertenece a la empresa seleccionada.");
        }
        documentTypePort.findActiveByPrefix(request.documentTypeCode(), request.enterpriseId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El tipo de documento no está configurado o está inactivo para la empresa."));
        note.updateDraft(request.date(), request.documentTypeCode(), request.concept(),
                request.enterpriseId(), toEntities(request.details()));
        return toResponse(noteRepository.save(note));
    }

    @Transactional
    public AccountingNoteResponse postDraft(Long id, String enterpriseId) {
        AccountingNoteEntity note = noteRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AccountingNoteNotFoundException(id));
        if (!Objects.equals(note.getEnterpriseId(), enterpriseId)) {
            throw new IllegalArgumentException("La nota no pertenece a la empresa seleccionada.");
        }
        note.post();
        return toResponse(noteRepository.save(note));
    }

    @Transactional
    public AccountingNoteResponse annul(Long id, String enterpriseId, String originModule, String reason) {
        AccountingNoteEntity note = noteRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AccountingNoteNotFoundException(id));
        if (!Objects.equals(note.getEnterpriseId(), enterpriseId)) {
            throw new IllegalArgumentException("La nota no pertenece a la empresa seleccionada.");
        }
        if (!Objects.equals(note.getOriginModule(), originModule)) {
            throw new IllegalArgumentException("El módulo de origen no coincide con el de la nota.");
        }
        boolean manualDraft = "contapp-frontend".equals(note.getOriginModule())
                && note.getStatus() == AccountingNoteEntity.NoteStatus.DRAFT;
        boolean automaticPosted = !"contapp-frontend".equals(note.getOriginModule())
                && note.getStatus() == AccountingNoteEntity.NoteStatus.POSTED;
        if (!manualDraft && !automaticPosted) {
            throw new IllegalStateException(
                    "Solo se pueden anular borradores manuales o notas automáticas notificadas por su módulo de origen. Las notas manuales contabilizadas requieren un asiento reverso.");
        }
        note.annul(reason.trim());
        return toResponse(noteRepository.save(note));
    }

    private List<AccountingNoteDetailEntity> toEntities(List<AccountingNoteDetailRequest> details) {
        List<AccountingNoteDetailEntity> entities = new ArrayList<>();
        for (AccountingNoteDetailRequest detail : details) {
            entities.add(new AccountingNoteDetailEntity(
                    detail.accountCode(), detail.thirdPartyDocument(), detail.costCenterCode(),
                    detail.description(), detail.debit(), detail.credit()));
        }
        return entities;
    }

    @Transactional(readOnly = true)
    public AccountingNoteResponse findById(Long id, String enterpriseId) {
        return noteRepository.findByIdWithDetails(id, enterpriseId)
                .map(this::toResponse)
                .orElseThrow(() -> new AccountingNoteNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<AccountingNoteResponse> findAll(String enterpriseId) {
        return noteRepository.findAllByEnterpriseId(enterpriseId).stream().map(this::toResponse).toList();
    }

    private void validateDoubleEntry(AccountingNoteRequest request) {
        if (request.details().size() < 2) {
            throw new IllegalArgumentException("La nota contable debe contener al menos dos detalles.");
        }
        if (request.details().size() > 999) {
            throw new IllegalArgumentException("La nota contable no puede superar 999 detalles.");
        }
        for (AccountingNoteDetailRequest detail : request.details()) {
            boolean hasDebit = detail.debit().signum() > 0;
            boolean hasCredit = detail.credit().signum() > 0;
            if (hasDebit == hasCredit) {
                throw new IllegalArgumentException("Cada detalle debe tener débito o crédito, pero no ambos ni ninguno.");
            }
        }
        BigDecimal debit = request.details().stream().map(AccountingNoteDetailRequest::debit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credit = request.details().stream().map(AccountingNoteDetailRequest::credit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (debit.signum() == 0 && credit.signum() == 0) {
            throw new IllegalArgumentException("La nota contable no puede tener valores en cero.");
        }
        if (debit.compareTo(credit) != 0) {
            throw new IllegalArgumentException("La nota contable no está cuadrada (partida doble).");
        }
    }

    private AccountingNoteResponse toResponse(AccountingNoteEntity note) {
        BigDecimal total = note.getDetails().stream()
                .map(AccountingNoteDetailEntity::getDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<AccountingNoteDetailResponse> details = note.getDetails().stream()
                .map(detail -> new AccountingNoteDetailResponse(detail.getAccountCode(),
                        detail.getThirdPartyId(), detail.getCostCenterCode(), detail.getDescription(),
                        detail.getDebit(), detail.getCredit()))
                .toList();
        return new AccountingNoteResponse(note.getId(), note.getNumber(), note.getDate(),
                note.getDocumentTypeCode(), note.getConcept(), note.getEnterpriseId(),
                note.getOriginModule(), note.getOriginId(), note.getStatus().name(), total,
                note.getCreatedAt(), details, note.getAnnulledAt(), note.getAnnulmentReason());
    }

    public record AccountingNoteDetailResponse(String accountCode, String thirdPartyDocument,
                                               String costCenterCode, String description,
                                               BigDecimal debit, BigDecimal credit) { }

    public record AccountingNoteResponse(Long id, String number, LocalDate date, String documentTypeCode,
                                         String concept, String enterpriseId, String originModule,
                                         String originId, String status, BigDecimal totalAmount,
                                         LocalDateTime createdAt, List<AccountingNoteDetailResponse> details,
                                         LocalDateTime annulledAt, String annulmentReason) {
    }
}
