package co.unicauca.edu.co.contables.auxiliary.unit.domain;

import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountingMovementBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.DiaryBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.InventoryAndBalancesBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.MajorAndBalancesBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.ThirdPartyBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IAccountingInfoClient;
import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IThirdPartyInfoClient;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.AccountingInfoProcessor;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.AuxiliaryBookCriteriaProcessor;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy.AccountStrategy;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy.AccountingMovementStrategy;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy.DiaryStrategy;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy.InventoryAndBalancesStrategy;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy.MajorAndBalancesStrategy;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy.ThirdPartyStrategy;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.CriteriaRange;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.Account;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.AccountingInfo;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.AccountingMovement;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.CostCenter;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.Voucher;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.thirdParty.ThirdParty;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @brief Pruebas unitarias para el modelo de dominio {@link AuxiliaryBook} y
 * las estrategias principales de procesamiento del módulo de libros auxiliares.
 */
@ExtendWith(MockitoExtension.class)
public class AuxiliaryBookUnitTest {

    @Mock
    private AuxiliaryBookTemplate mockTemplate;
    @Mock
    private AuxiliaryBookCriteria mockCriteria;
    @Mock
    private IAccountingInfoClient accountingInfoClient;
    @Mock
    private IThirdPartyInfoClient thirdPartyInfoClient;

    private AuxiliaryBook auxiliaryBook;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.of(2025, 1, 15, 10, 30);

        auxiliaryBook = AuxiliaryBook.builder()
                .id(1L)
                .publicId("uuid-test-123")
                .type(EAuxiliaryBookType.INVENTORY_AND_BALANCES)
                .entId("ent-001")
                .userId(100L)
                .format(EAuxiliaryBookFormat.PDF)
                .template(mockTemplate)
                .criteria(mockCriteria)
                .createdAt(testTime)
                .build();
    }

    @Test
    @DisplayName("Debería crear instancia y obtener valores con Builder y Getters")
    void testBuilderAndGetters() {
        Assertions.assertThat(auxiliaryBook.getId()).isEqualTo(1L);
        Assertions.assertThat(auxiliaryBook.getPublicId()).isEqualTo("uuid-test-123");
        Assertions.assertThat(auxiliaryBook.getType()).isEqualTo(EAuxiliaryBookType.INVENTORY_AND_BALANCES);
        Assertions.assertThat(auxiliaryBook.getEntId()).isEqualTo("ent-001");
        Assertions.assertThat(auxiliaryBook.getUserId()).isEqualTo(100L);
        Assertions.assertThat(auxiliaryBook.getFormat()).isEqualTo(EAuxiliaryBookFormat.PDF);
        Assertions.assertThat(auxiliaryBook.getTemplate()).isSameAs(mockTemplate);
        Assertions.assertThat(auxiliaryBook.getCriteria()).isSameAs(mockCriteria);
        Assertions.assertThat(auxiliaryBook.getCreatedAt()).isEqualTo(testTime);
    }

    @Test
    @DisplayName("Debería funcionar el constructor sin argumentos")
    void testNoArgsConstructor() {
        AuxiliaryBook book = new AuxiliaryBook();

        Assertions.assertThat(book).isNotNull();
        Assertions.assertThat(book.getId()).isNull();
        Assertions.assertThat(book.getPublicId()).isNull();
    }

    @Test
    @DisplayName("Debería funcionar el constructor con todos los argumentos")
    void testAllArgsConstructor() {
        AuxiliaryBook allArgsBook = new AuxiliaryBook(
                2L,
                "uuid-456",
                EAuxiliaryBookType.ACCOUNT,
                "ent-002",
                200L,
                EAuxiliaryBookFormat.EXCEL,
                mockTemplate,
                mockCriteria,
                testTime
        );

        Assertions.assertThat(allArgsBook.getId()).isEqualTo(2L);
        Assertions.assertThat(allArgsBook.getPublicId()).isEqualTo("uuid-456");
        Assertions.assertThat(allArgsBook.getType()).isEqualTo(EAuxiliaryBookType.ACCOUNT);
        Assertions.assertThat(allArgsBook.getFormat()).isEqualTo(EAuxiliaryBookFormat.EXCEL);
        Assertions.assertThat(allArgsBook.getTemplate()).isSameAs(mockTemplate);
    }

    @Test
    @DisplayName("Debería funcionar los Setters (provistos por @Data)")
    void testSetters() {
        AuxiliaryBook book = new AuxiliaryBook();
        LocalDateTime newTime = LocalDateTime.now();

        book.setId(3L);
        book.setPublicId("new-uuid");
        book.setType(EAuxiliaryBookType.INVENTORY_AND_BALANCES);
        book.setCreatedAt(newTime);

        Assertions.assertThat(book.getId()).isEqualTo(3L);
        Assertions.assertThat(book.getPublicId()).isEqualTo("new-uuid");
        Assertions.assertThat(book.getType()).isEqualTo(EAuxiliaryBookType.INVENTORY_AND_BALANCES);
        Assertions.assertThat(book.getCreatedAt()).isEqualTo(newTime);
    }

    @Test
    @DisplayName("Debería cambiar el formato con el método changeFormat")
    void testChangeFormat() {
        Assertions.assertThat(auxiliaryBook.getFormat()).isEqualTo(EAuxiliaryBookFormat.PDF);

        auxiliaryBook.changeFormat(EAuxiliaryBookFormat.EXCEL);

        Assertions.assertThat(auxiliaryBook.getFormat()).isEqualTo(EAuxiliaryBookFormat.EXCEL);
    }

    @Test
    @DisplayName("Debería manejar correctamente Equals y HashCode (provistos por @Data)")
    void testEqualsAndHashCode() {
        AuxiliaryBook bookCopy = AuxiliaryBook.builder()
                .id(1L)
                .publicId("uuid-test-123")
                .type(EAuxiliaryBookType.INVENTORY_AND_BALANCES)
                .entId("ent-001")
                .userId(100L)
                .format(EAuxiliaryBookFormat.PDF)
                .template(mockTemplate)
                .criteria(mockCriteria)
                .createdAt(testTime)
                .build();

        AuxiliaryBook differentBook = AuxiliaryBook.builder()
                .id(2L)
                .publicId("uuid-test-999")
                .build();

        Assertions.assertThat(auxiliaryBook).isEqualTo(bookCopy)
                .isNotEqualTo(differentBook)
                .isNotEqualTo(null)
                .isNotEqualTo(new Object());

        Assertions.assertThat(auxiliaryBook.hashCode()).isEqualTo(bookCopy.hashCode())
                .isNotEqualTo(differentBook.hashCode());
    }

    @Test
    @DisplayName("Debería generar un String no nulo (provisto por @Data)")
    void testToString() {
        String bookString = auxiliaryBook.toString();

        Assertions.assertThat(bookString).isNotNull()
                .contains("AuxiliaryBook")
                .contains("id=1")
                .contains("publicId=uuid-test-123")
                .contains("entId=ent-001");
    }

    @Test
    @DisplayName("AuxiliaryBookCriteriaProcessor debe filtrar por entidad, fecha fin y criterios opcionales")
    void processAccountingInfoFiltersByEntityAndOptionalCriteria() {
        AuxiliaryBookCriteriaProcessor processor = new AuxiliaryBookCriteriaProcessor();
        AuxiliaryBookCriteria criteria = criteria(
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 31),
                ECriteriaType.ACCOUNT,
                new CriteriaRange(1105L, 1105L),
                "CC1",
                "TP1",
                "VT1"
        );
        AuxiliaryBook book = AuxiliaryBook.builder()
                .entId("ENT1")
                .criteria(criteria)
                .build();

        AccountingInfo matching = accountingInfo(
                "ENT1",
                LocalDate.of(2025, 1, 31),
                11050001L,
                "debito",
                "Caja",
                50d,
                0d,
                " TP1 ",
                " CC1 ",
                " VT1 ",
                "001",
                "movimiento válido"
        );

        Mockito.when(accountingInfoClient.getAllAccountInfo()).thenReturn(List.of(
                matching,
                accountingInfo("ENT2", LocalDate.of(2025, 1, 31), 11050001L, "debito", "Caja", 50d, 0d, "TP1", "CC1", "VT1", "002", "otra entidad"),
                accountingInfo("ENT1", LocalDate.of(2025, 2, 1), 11050001L, "debito", "Caja", 50d, 0d, "TP1", "CC1", "VT1", "003", "fuera de fecha"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 31), 22050001L, "debito", "Clientes", 50d, 0d, "TP1", "CC1", "VT1", "004", "fuera de rango"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 31), 11050001L, "debito", "Caja", 50d, 0d, "TP2", "CC1", "VT1", "005", "tercero distinto"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 31), 11050001L, "debito", "Caja", 50d, 0d, "TP1", "CC2", "VT1", "006", "cc distinto"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 31), 11050001L, "debito", "Caja", 50d, 0d, "TP1", "CC1", "VT2", "007", "voucher distinto"),
                new AccountingInfo("ENT1", null, null, null, null, null, null)
        ));

        List<AccountingInfo> result = processor.processAccountingInfo(accountingInfoClient, book);

        Assertions.assertThat(result).containsExactly(matching);
    }

    @Test
    @DisplayName("AuxiliaryBookCriteriaProcessor no debe aplicar filtro por fecha inicial")
    void processAccountingInfoDoesNotApplyStartDateFilter() {
        AuxiliaryBookCriteriaProcessor processor = new AuxiliaryBookCriteriaProcessor();
        AuxiliaryBookCriteria criteria = criteria(
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 31),
                null,
                null,
                null,
                null,
                null
        );
        AuxiliaryBook book = AuxiliaryBook.builder()
                .entId("ENT1")
                .criteria(criteria)
                .build();

        AccountingInfo beforeStart = accountingInfo("ENT1", LocalDate.of(2025, 1, 5), 11050001L, "debito", "Caja", 10d, 0d, "TP1", "CC1", "VT1", "001", "anterior al inicio");
        AccountingInfo withinRange = accountingInfo("ENT1", LocalDate.of(2025, 1, 15), 11050001L, "debito", "Caja", 15d, 0d, "TP1", "CC1", "VT1", "002", "dentro del rango");

        Mockito.when(accountingInfoClient.getAllAccountInfo()).thenReturn(List.of(beforeStart, withinRange));

        List<AccountingInfo> result = processor.processAccountingInfo(accountingInfoClient, book);

        Assertions.assertThat(result).containsExactly(beforeStart, withinRange);
    }

    @Test
    @DisplayName("MajorAndBalancesStrategy debe calcular saldos agrupados por naturaleza y excluir grupos en cero")
    void majorAndBalancesStrategyCalculatesGroupedBalances() {
        MajorAndBalancesStrategy strategy = new MajorAndBalancesStrategy();
        AuxiliaryBookCriteria criteria = criteria(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                ECriteriaType.ACCOUNT,
                new CriteriaRange(1105L, 3305L),
                null,
                null,
                null
        );

        List<AccountingInfo> data = List.of(
                accountingInfo("ENT1", LocalDate.of(2024, 12, 31), 1105L, "debito", "Caja", 100d, 20d, "TP1", "CC1", "RC", "001", "saldo inicial caja"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 10), 11050001L, "debito", "Caja menor", 30d, 10d, "TP1", "CC1", "RC", "002", "movimiento caja"),
                accountingInfo("ENT1", LocalDate.of(2024, 12, 31), 2205L, "credito", "Proveedores", 10d, 50d, "TP1", "CC1", "RC", "003", "saldo inicial proveedores"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 8), 22050001L, "credito", "Proveedor nacional", 5d, 25d, "TP1", "CC1", "RC", "004", "movimiento proveedores"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 15), 3305L, "debito", "Patrimonio", 0d, 0d, "TP1", "CC1", "RC", "005", "grupo cero")
        );

        @SuppressWarnings("unchecked")
        List<MajorAndBalancesBookDTO> result = (List<MajorAndBalancesBookDTO>) strategy.process(
                criteria,
                data,
                new AccountingInfoProcessor()
        );

        Map<Long, MajorAndBalancesBookDTO> resultByCode = result.stream()
                .collect(Collectors.toMap(MajorAndBalancesBookDTO::getAccountCode, dto -> dto));

        Assertions.assertThat(result).hasSize(2);
        Assertions.assertThat(resultByCode.get(1105L).getInitialBalance()).isEqualByComparingTo("80.00");
        Assertions.assertThat(resultByCode.get(1105L).getDebitMovement()).isEqualByComparingTo("30.00");
        Assertions.assertThat(resultByCode.get(1105L).getCreditMovement()).isEqualByComparingTo("10.00");
        Assertions.assertThat(resultByCode.get(1105L).getFinalBalance()).isEqualByComparingTo("100.00");
        Assertions.assertThat(resultByCode.get(2205L).getInitialBalance()).isEqualByComparingTo("40.00");
        Assertions.assertThat(resultByCode.get(2205L).getDebitMovement()).isEqualByComparingTo("5.00");
        Assertions.assertThat(resultByCode.get(2205L).getCreditMovement()).isEqualByComparingTo("25.00");
        Assertions.assertThat(resultByCode.get(2205L).getFinalBalance()).isEqualByComparingTo("60.00");
    }

    @Test
    @DisplayName("MajorAndBalancesStrategy debe rechazar rangos incompatibles")
    void majorAndBalancesStrategyRejectsInvalidRange() {
        MajorAndBalancesStrategy strategy = new MajorAndBalancesStrategy();
        AuxiliaryBookCriteria criteria = criteria(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                ECriteriaType.ACCOUNT,
                new CriteriaRange(11L, 999L),
                null,
                null,
                null
        );

        Assertions.assertThatThrownBy(() -> strategy.process(criteria, List.of(), new AccountingInfoProcessor()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no es compatible");
    }

    @Test
    @DisplayName("AccountStrategy debe calcular saldo corrido por cuenta respetando costo y rango")
    void accountStrategyCalculatesRunningBalance() {
        AccountStrategy strategy = new AccountStrategy();
        AuxiliaryBookCriteria criteria = criteria(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                ECriteriaType.ACCOUNT,
                new CriteriaRange(1105L, 1105L),
                "CC1",
                null,
                null
        );

        List<AccountingInfo> data = List.of(
                accountingInfo("ENT1", LocalDate.of(2024, 12, 31), 1105L, "debito", "Caja", 100d, 0d, "TP0", "CC1", "RC", "000", "saldo inicial"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 5), 11050002L, "debito", "Caja menor", 0d, 30d, "TP1", "CC1", "RC", "001", "salida"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 10), 11050001L, "debito", "Caja general", 20d, 0d, "TP2", "CC1", "RC", "002", "entrada"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 8), 11050003L, "debito", "Caja sucursal", 50d, 0d, "TP3", "CC2", "RC", "003", "otro centro de costo")
        );

        @SuppressWarnings("unchecked")
        List<AccountBookDTO> result = (List<AccountBookDTO>) strategy.process(criteria, data, new AccountingInfoProcessor());

        Assertions.assertThat(result).hasSize(2);
        Assertions.assertThat(result.get(0).getDate()).isEqualTo("05/01/2025");
        Assertions.assertThat(result.get(0).getAccountCode()).isEqualTo(1105L);
        Assertions.assertThat(result.get(0).getBalanceMovement()).isEqualByComparingTo("70.00");
        Assertions.assertThat(result.get(0).getVoucherCostCenter()).isEqualTo("CC1");
        Assertions.assertThat(result.get(1).getDate()).isEqualTo("10/01/2025");
        Assertions.assertThat(result.get(1).getBalanceMovement()).isEqualByComparingTo("90.00");
        Assertions.assertThat(result.get(1).getThirdPartyId()).isEqualTo("TP2");
    }

    @Test
    @DisplayName("ThirdPartyStrategy debe resolver nombre del tercero y mantener saldo corrido")
    void thirdPartyStrategyResolvesThirdPartyNameAndBalances() {
        ThirdPartyStrategy strategy = new ThirdPartyStrategy(thirdPartyInfoClient);
        AuxiliaryBookCriteria criteria = criteria(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                ECriteriaType.ACCOUNT,
                null,
                null,
                "900",
                null
        );

        Mockito.when(thirdPartyInfoClient.getThirdPartyById(900L))
                .thenReturn(new ThirdParty(900L, "Proveedor Uno", 123456L, "CC"));

        List<AccountingInfo> data = List.of(
                accountingInfo("ENT1", LocalDate.of(2024, 12, 31), 1105L, "debito", "Caja", 100d, 0d, "900", "CC1", "RC", "000", "saldo inicial"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 5), 11050001L, "debito", "Caja menor", 0d, 40d, "900", "CC1", "RC", "001", "salida"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 10), 11050002L, "debito", "Caja general", 10d, 0d, "900", "CC1", "RC", "002", "entrada"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 12), 11050003L, "debito", "Caja sucursal", 10d, 0d, "901", "CC1", "RC", "003", "otro tercero")
        );

        @SuppressWarnings("unchecked")
        List<ThirdPartyBookDTO> result = (List<ThirdPartyBookDTO>) strategy.process(criteria, data, new AccountingInfoProcessor());

        Assertions.assertThat(result).hasSize(2);
        Assertions.assertThat(result.get(0).getThirdPartyName()).isEqualTo("Proveedor Uno");
        Assertions.assertThat(result.get(0).getBalanceMovement()).isEqualByComparingTo("60.00");
        Assertions.assertThat(result.get(1).getBalanceMovement()).isEqualByComparingTo("70.00");
        Mockito.verify(thirdPartyInfoClient).getThirdPartyById(900L);
    }

    @Test
    @DisplayName("ThirdPartyStrategy debe dejar el nombre vacío cuando el id no es numérico")
    void thirdPartyStrategyReturnsEmptyNameForNonNumericId() {
        ThirdPartyStrategy strategy = new ThirdPartyStrategy(thirdPartyInfoClient);
        AuxiliaryBookCriteria criteria = criteria(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                null,
                null,
                null,
                "ABC",
                null
        );

        List<AccountingInfo> data = List.of(
                accountingInfo("ENT1", LocalDate.of(2025, 1, 5), 11050001L, "debito", "Caja menor", 50d, 0d, "ABC", "CC1", "RC", "001", "movimiento")
        );

        @SuppressWarnings("unchecked")
        List<ThirdPartyBookDTO> result = (List<ThirdPartyBookDTO>) strategy.process(criteria, data, new AccountingInfoProcessor());

        Assertions.assertThat(result).singleElement().satisfies(dto -> {
            Assertions.assertThat(dto.getThirdPartyName()).isEmpty();
            Assertions.assertThat(dto.getAccountCode()).isEqualTo(11050001L);
        });
        Mockito.verifyNoInteractions(thirdPartyInfoClient);
    }

    @Test
    @DisplayName("AccountingMovementStrategy debe ordenar comprobantes por fecha inicial y arrastrar saldo entre comprobantes")
    void accountingMovementStrategyOrdersVouchersAndCarriesBalance() {
        AccountingMovementStrategy strategy = new AccountingMovementStrategy();
        AuxiliaryBookCriteria criteria = criteria(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                ECriteriaType.ACCOUNT,
                null,
                null,
                null,
                null
        );

        List<AccountingInfo> data = List.of(
                accountingInfo("ENT1", LocalDate.of(2024, 12, 31), 1105L, "debito", "Caja", 100d, 0d, "TP0", "CC1", "Z", "000", "saldo inicial"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 10), 11050001L, "debito", "Caja menor", 20d, 0d, "TP2", "CC1", "B", "002", "segundo comprobante"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 5), 11050001L, "debito", "Caja menor", 0d, 10d, "TP1", "CC1", "A", "001", "primer comprobante")
        );

        @SuppressWarnings("unchecked")
        List<AccountingMovementBookDTO> result = (List<AccountingMovementBookDTO>) strategy.process(
                criteria,
                data,
                new AccountingInfoProcessor()
        );

        Assertions.assertThat(result).hasSize(2);
        Assertions.assertThat(result.get(0).getVoucherType()).isEqualTo("A");
        Assertions.assertThat(result.get(0).getInitialBalance()).isEqualByComparingTo("100.00");
        Assertions.assertThat(result.get(0).getNetMovement()).isEqualByComparingTo("90.00");
        Assertions.assertThat(result.get(1).getVoucherType()).isEqualTo("B");
        Assertions.assertThat(result.get(1).getInitialBalance()).isEqualByComparingTo("90.00");
        Assertions.assertThat(result.get(1).getNetMovement()).isEqualByComparingTo("110.00");
    }

    @Test
    @DisplayName("InventoryAndBalancesStrategy debe calcular saldo final y excluir cuentas con saldo cero")
    void inventoryAndBalancesStrategyCalculatesFinalBalances() {
        InventoryAndBalancesStrategy strategy = new InventoryAndBalancesStrategy();
        AuxiliaryBookCriteria criteria = criteria(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                null,
                null,
                null,
                null,
                null
        );

        List<AccountingInfo> data = List.of(
                accountingInfo("ENT1", LocalDate.of(2024, 12, 31), 11050001L, "debito", "Caja menor", 100d, 0d, "TP1", "CC1", "RC", "001", "saldo inicial"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 5), 11050001L, "debito", "Caja menor", 0d, 20d, "TP1", "CC1", "RC", "002", "salida"),
                accountingInfo("ENT1", LocalDate.of(2024, 12, 31), 22050001L, "credito", "Proveedores", 10d, 60d, "TP1", "CC1", "RC", "003", "saldo inicial crédito"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 6), 22050001L, "credito", "Proveedores", 10d, 10d, "TP1", "CC1", "RC", "004", "movimiento crédito"),
                accountingInfo("ENT1", LocalDate.of(2024, 12, 31), 33050001L, "debito", "Patrimonio", 10d, 0d, "TP1", "CC1", "RC", "005", "saldo cero"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 7), 33050001L, "debito", "Patrimonio", 0d, 10d, "TP1", "CC1", "RC", "006", "saldo cero")
        );

        @SuppressWarnings("unchecked")
        List<InventoryAndBalancesBookDTO> result = (List<InventoryAndBalancesBookDTO>) strategy.process(
                criteria,
                data,
                new AccountingInfoProcessor()
        );

        Map<Long, InventoryAndBalancesBookDTO> resultByCode = result.stream()
                .collect(Collectors.toMap(InventoryAndBalancesBookDTO::getAccountCode, dto -> dto));

        Assertions.assertThat(result).hasSize(2);
        Assertions.assertThat(resultByCode.get(11050001L).getValue()).isEqualByComparingTo("80.00");
        Assertions.assertThat(resultByCode.get(22050001L).getValue()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("DiaryStrategy sin agrupación debe ordenar cronológicamente y mantener la cuenta original")
    void diaryStrategyWithoutGroupingSortsAndMapsRawAccount() {
        DiaryStrategy strategy = new DiaryStrategy();
        AuxiliaryBookCriteria criteria = criteria(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                null,
                null,
                null,
                null,
                null
        );

        List<AccountingInfo> data = List.of(
                accountingInfo("ENT1", LocalDate.of(2025, 1, 10), 2205L, "credito", "Proveedores", 0d, 20d, "TP2", "CC1", "RC", "002", "segundo"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 5), 1105L, "debito", "Caja", 10d, 0d, "TP1", "CC1", "RC", "001", "primero")
        );

        @SuppressWarnings("unchecked")
        List<DiaryBookDTO> result = (List<DiaryBookDTO>) strategy.process(criteria, data, new AccountingInfoProcessor());

        Assertions.assertThat(result).hasSize(2);
        Assertions.assertThat(result.get(0).getDate()).isEqualTo("05/01/2025");
        Assertions.assertThat(result.get(0).getAccountCode()).isEqualTo(1105L);
        Assertions.assertThat(result.get(0).getVoucherNumber()).isEqualTo("001");
        Assertions.assertThat(result.get(1).getAccountCode()).isEqualTo(2205L);
        Assertions.assertThat(result.get(1).getCredit()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("DiaryStrategy con agrupación debe reemplazar la cuenta por el nivel agrupado")
    void diaryStrategyWithGroupingUsesGroupedAccount() {
        DiaryStrategy strategy = new DiaryStrategy();
        AuxiliaryBookCriteria criteria = criteria(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                ECriteriaType.ACCOUNT,
                null,
                null,
                null,
                null
        );

        List<AccountingInfo> data = List.of(
                accountingInfo("ENT1", LocalDate.of(2025, 1, 5), 1105L, "debito", "Caja", 0d, 0d, "TP1", "CC1", "RC", "000", "cuenta padre"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 6), 11050001L, "debito", "Caja menor", 15d, 0d, "TP1", "CC1", "RC", "001", "movimiento"),
                accountingInfo("ENT1", LocalDate.of(2025, 1, 7), 11050002L, "debito", "Caja general", 0d, 5d, "TP1", "CC1", "RC", "002", "movimiento")
        );

        @SuppressWarnings("unchecked")
        List<DiaryBookDTO> result = (List<DiaryBookDTO>) strategy.process(criteria, data, new AccountingInfoProcessor());

        Assertions.assertThat(result).hasSize(3);
        Assertions.assertThat(result).allSatisfy(dto -> {
            Assertions.assertThat(dto.getAccountCode()).isEqualTo(1105L);
            Assertions.assertThat(dto.getAccountDescription()).isEqualTo("Caja");
        });
    }

    private AuxiliaryBookCriteria criteria(
            LocalDate startDate,
            LocalDate endDate,
            ECriteriaType criteriaType,
            CriteriaRange range,
            String costCenterId,
            String thirdPartyId,
            String voucherType
    ) {
        return AuxiliaryBookCriteria.builder()
                .criteriaType(criteriaType)
                .criteriaRange(range)
                .costCenterId(costCenterId)
                .thirdPartyId(thirdPartyId)
                .voucherType(voucherType)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    private AccountingInfo accountingInfo(
            String entId,
            LocalDate date,
            Long accountCode,
            String nature,
            String accountName,
            Double debit,
            Double credit,
            String thirdPartyId,
            String costCenterCode,
            String voucherType,
            String voucherNumber,
            String description
    ) {
        return new AccountingInfo(
                entId,
                toDate(date),
                new Voucher(voucherNumber, voucherType),
                new Account(accountCode, nature, accountName),
                thirdPartyId,
                new AccountingMovement(description, debit, credit),
                new CostCenter(costCenterCode, "Cost center " + costCenterCode)
        );
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
