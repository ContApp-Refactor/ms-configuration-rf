package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IPortfolioSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.ClientPortfolioSummaryResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.InvoiceDetailResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.PortfolioAgingAccountResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.ReceiptSummaryResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.ReceiptEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IReceiptDetailRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PortfolioSearchService implements IPortfolioSearchInputPort {
    private final IAccountingSearchOutputPort accountingSearchOutputPort;
    private final IReceiptDetailRepository receiptDetailRepository;
    private final IInvoiceProviderPort invoiceProviderPort;

    @Override
    @Transactional
    public List<ClientPortfolioSummaryResponse> getClientPortfolioSummary(List<Long> clientIds) {
        if (clientIds == null || clientIds.isEmpty())
            return List.of();

        // 1. Obtener TODAS las facturas pendientes para los clientes solicitados
        List<InvoiceReplica> allInvoices = accountingSearchOutputPort.findPendingInvoicesByClientIds(clientIds);

        // 2. Agrupar las facturas por el ID del cliente (thirdId)
        Map<Long, List<InvoiceReplica>> invoicesByClient = allInvoices.stream()
                .collect(Collectors.groupingBy(InvoiceReplica::getThirdId));

        // 3. Procesar el mapa para calcular los resúmenes de cada cliente
        return invoicesByClient.entrySet().stream()
                .map(entry -> {
                    Long clientId = entry.getKey();
                    List<InvoiceReplica> clientInvoices = entry.getValue();
                    LocalDate today = LocalDate.now();

                    // Calculamos el valor total de la deuda
                    BigDecimal totalDebt = clientInvoices.stream()
                            .map(InvoiceReplica::getPendingValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Calculamos la deuda vencida
                    BigDecimal overdueAmount = clientInvoices.stream()
                            .filter(inv -> inv.getExpirationDate() != null && inv.getExpirationDate().isBefore(today))
                            .map(InvoiceReplica::getPendingValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Calculamos la deuda por vencer
                    BigDecimal dueToAmount = totalDebt.subtract(overdueAmount);

                    // 4. Construir y devolver el DTO de respuesta
                    return ClientPortfolioSummaryResponse.builder()
                            .clientId(clientId)
                            .totalDebt(totalDebt)
                            .overdueAmount(overdueAmount)
                            .dueToAmount(dueToAmount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceReplica> findPendingInvoicesByClientId(Long thirdIds) {
        return accountingSearchOutputPort.findPendingInvoicesByClientId(thirdIds);
    }

    @Override
    @Transactional
    public List<ReceiptSummaryResponse> findReceiptsByInvoiceId(Long invoiceId) {
        return receiptDetailRepository.findByOriginalInvoiceId(invoiceId)
                .stream()
                .map(detailEntity -> {
                    ReceiptEntity receipt = detailEntity.getReceipt();
                    return ReceiptSummaryResponse.builder()
                            .id(receipt.getId())
                            .receiptCode(receipt.getReceiptCode())
                            .issueDate(receipt.getIssueDate())
                            .amountPaid(detailEntity.getAmountPaid())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<InvoiceDetailResponse> getInvoiceDetailsByClientId(Long clientId) {
        // 1. Reutilizamos el método existente para obtener los modelos de dominio.
        List<InvoiceReplica> invoices = this.findPendingInvoicesByClientId(clientId);

        // 2. Obtenemos la fecha actual una sola vez para eficiencia.
        LocalDate today = LocalDate.now();

        // 3. Mapeamos la lista de modelos de dominio (InvoiceReplica) a la lista de
        // DTOs (InvoiceDetailResponse)
        return invoices.stream()
                .map((InvoiceReplica invoice) -> {
                    int daysInArrears = 0;

                    // 4. Calculamos los días en mora (la misma lógica que estaba en el controlador)
                    if (invoice.getExpirationDate() != null && invoice.getExpirationDate().isBefore(today)) {
                        daysInArrears = (int) ChronoUnit.DAYS.between(invoice.getExpirationDate(), today);
                    }

                    // 5. Construimos el DTO de respuesta
                    return InvoiceDetailResponse.builder()
                            .id(invoice.getId())
                            .factCode(invoice.getFactCode())
                            .clientId(invoice.getThirdId())
                            .creationDate(invoice.getCreationDate())
                            .expirationDate(invoice.getExpirationDate())
                            .totalValue(invoice.getTotalValue())
                            .totalPay(invoice.getTotalPay())
                            .pendingValue(invoice.getPendingValue())
                            .status(invoice.getStatus().toString())
                            .daysInArrears(daysInArrears) // Asignamos el valor calculado
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PortfolioAgingAccountResponse> getPortfolioAgingReport(Long clientId, LocalDate cutoffDate,
            String enterpriseId, boolean includeDocuments) {

        // --- PASO 1: OBTENER LOS DATOS BASE ---
        //HCELE DEJA DE SER PERESOZA OME, QUE ASI NO SE PUEDE NO
        List<AccountCatalogue> allAccounts = accountingSearchOutputPort.findAllAccountsByEnterprise(enterpriseId);

        List<InvoiceReplica> pendingInvoices;
        if (clientId != null) {
            // Reporte para un cliente específico
            pendingInvoices = invoiceProviderPort.findPendingInvoicesByClientId(clientId);
        } else {
            // Reporte Consolidado (Todos los clientes)
            pendingInvoices = invoiceProviderPort.findPendingInvoicesByEnterpriseId(enterpriseId);
        }

        Map<String, AccountCatalogue> accountsByCode = allAccounts.stream()
                .collect(Collectors.toMap(AccountCatalogue::getCode, Function.identity()));

        // Mapa que contendrá el reporte completo. Lo inicializamos con CADA cuenta.
        Map<Long, PortfolioAgingAccountResponse> reportMap = new LinkedHashMap<>();
        for (AccountCatalogue account : allAccounts) {
            reportMap.put(account.getId(), PortfolioAgingAccountResponse.builder()
                    .accountCode(account.getCode())
                    .accountName(account.getDescription())
                    .build());
        }

        // --- PASO 2 Y 3 COMBINADOS: CONSTRUIR EL REPORTE DESDE LAS FACTURAS PENDIENTES
        // ---
        // Esta es la única fuente de verdad para un reporte de cartera.
        for (InvoiceReplica invoice : pendingInvoices) {
            AccountCatalogue account = accountsByCode.get(String.valueOf(invoice.getAccountingAccount()));
            if (account == null)
                continue;

            PortfolioAgingAccountResponse accountResponse = reportMap.get(account.getId());
            if (accountResponse == null)
                continue;

            BigDecimal pendingValue = invoice.getPendingValue();

            // El "Total Adeudado" ES la suma de los saldos pendientes de las facturas.
            accountResponse.setTotalAdeudado(accountResponse.getTotalAdeudado().add(pendingValue));

            // Ahora clasificamos ese mismo saldo pendiente en las cubetas de edades.
            long daysOverdue = 0L;
            if (invoice.getExpirationDate() != null && !invoice.getExpirationDate().isAfter(cutoffDate)) {
                daysOverdue = ChronoUnit.DAYS.between(invoice.getExpirationDate(), cutoffDate);
                if (daysOverdue > 0) { // Si es vencido
                    if (daysOverdue <= 30)
                        accountResponse.setDias1a30(accountResponse.getDias1a30().add(pendingValue));
                    else if (daysOverdue <= 60)
                        accountResponse.setDias31a60(accountResponse.getDias31a60().add(pendingValue));
                    else if (daysOverdue <= 90)
                        accountResponse.setDias61a90(accountResponse.getDias61a90().add(pendingValue));
                    else
                        accountResponse.setMasDe90dias(accountResponse.getMasDe90dias().add(pendingValue));
                }
            }
            // Si el flag 'includeDocuments' es verdadero, creamos el DTO del detalle
            // de la factura y lo añadimos a la lista de documentos de la cuenta
            // correspondiente.

            if (includeDocuments) {
                InvoiceDetailResponse invoiceDetail = InvoiceDetailResponse.builder()
                        .id(invoice.getId())
                        .factCode(invoice.getFactCode())
                        .clientId(invoice.getThirdId())
                        .creationDate(invoice.getCreationDate())
                        .expirationDate(invoice.getExpirationDate())
                        .totalValue(invoice.getTotalValue())
                        .totalPay(invoice.getTotalPay())
                        .pendingValue(invoice.getPendingValue())
                        .status(invoice.getStatus().toString())
                        .daysInArrears(daysOverdue > 0 ? (int) daysOverdue : 0)
                        .build();

                // Agregamos el detalle de la factura a la respuesta de la cuenta
                accountResponse.getDocuments().add(invoiceDetail);
            }
        }

        // --- PASO 4: AGREGAR TODOS LOS VALORES HACIA ARRIBA ---
        List<AccountCatalogue> reversedAccounts = new ArrayList<>(allAccounts);
        Collections.reverse(reversedAccounts);

        for (AccountCatalogue account : reversedAccounts) {
            if (account.getParent() != null) {
                PortfolioAgingAccountResponse childResponse = reportMap.get(account.getId());
                PortfolioAgingAccountResponse parentResponse = reportMap.get(account.getParent().getId());

                if (childResponse != null && parentResponse != null) {
                    // Se suman los valores "brutos"
                    parentResponse
                            .setTotalAdeudado(parentResponse.getTotalAdeudado().add(childResponse.getTotalAdeudado()));
                    parentResponse.setDias1a30(parentResponse.getDias1a30().add(childResponse.getDias1a30()));
                    parentResponse.setDias31a60(parentResponse.getDias31a60().add(childResponse.getDias31a60()));
                    parentResponse.setDias61a90(parentResponse.getDias61a90().add(childResponse.getDias61a90()));
                    parentResponse.setMasDe90dias(parentResponse.getMasDe90dias().add(childResponse.getMasDe90dias()));
                }
            }
        }

        // --- PASO 5: CALCULAR 'CORRIENTE' PARA TODAS LAS CUENTAS ---
        for (PortfolioAgingAccountResponse response : reportMap.values()) {
            BigDecimal totalAged = response.getDias1a30()
                    .add(response.getDias31a60())
                    .add(response.getDias61a90())
                    .add(response.getMasDe90dias());
            response.setCorriente(response.getTotalAdeudado().subtract(totalAged));
        }

        // --- PASO 6: CONSTRUIR EL ÁRBOL FINAL ---
        for (PortfolioAgingAccountResponse responseNode : reportMap.values()) {
            AccountCatalogue account = accountsByCode.get(responseNode.getAccountCode());
            if (account != null && account.getParent() != null) {
                PortfolioAgingAccountResponse parentResponse = reportMap.get(account.getParent().getId());
                // Añadimos solo hijos que tengan saldo para no poblar el árbol con ramas
                // vacías.
                if (parentResponse != null && responseNode.getTotalAdeudado().compareTo(BigDecimal.ZERO) != 0) {
                    parentResponse.getChildren().add(responseNode);
                }
            }
        }

        // Filtramos para quedarnos únicamente con los nodos raíz que tengan saldo.
        List<PortfolioAgingAccountResponse> rootNodes = reportMap.values().stream()
                .filter(responseNode -> {
                    // Un nodo raíz debe tener saldo Y no tener padre.
                    if (responseNode.getTotalAdeudado().compareTo(BigDecimal.ZERO) == 0)
                        return false;
                    AccountCatalogue account = accountsByCode.get(responseNode.getAccountCode());
                    return account != null && account.getParent() == null;
                })
                .collect(Collectors.toList());

        return rootNodes;
    }

}
