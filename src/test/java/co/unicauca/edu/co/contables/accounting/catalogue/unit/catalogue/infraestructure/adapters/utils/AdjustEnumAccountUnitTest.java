package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.infraestructure.adapters.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.utils.AdjustEnumAccount;

class AdjustEnumAccountUnitTest {

    private AdjustEnumAccount adjustEnumAccount;

    @BeforeEach
    void setUp() {
        adjustEnumAccount = new AdjustEnumAccount();
    }

    @Nested
    @DisplayName("Tests para adjustClassificationEnum")
    class AdjustClassificationEnumTests {

        @Test
        @DisplayName("Debe retornar CURRENTASSETS cuando el estado es 'Activo Corriente'")
        void testAdjustClassificationEnum_CurrentAssets() {
            String state = "Activo Corriente";

            ClassificationEnum result = adjustEnumAccount.adjustClassificationEnum(state);

            assertEquals(ClassificationEnum.CURRENTASSETS, result);
        }

        @Test
        @DisplayName("Debe retornar NONCURRENTASSETS cuando el estado es 'Activo No Corriente'")
        void testAdjustClassificationEnum_NonCurrentAssets() {
            String state = "Activo No Corriente";

            ClassificationEnum result = adjustEnumAccount.adjustClassificationEnum(state);

            assertEquals(ClassificationEnum.NONCURRENTASSETS, result);
        }

        @Test
        @DisplayName("Debe retornar CURRENTLIABILITIES cuando el estado es 'Pasivo Corriente'")
        void testAdjustClassificationEnum_CurrentLiabilities() {
            String state = "Pasivo Corriente";

            ClassificationEnum result = adjustEnumAccount.adjustClassificationEnum(state);

            assertEquals(ClassificationEnum.CURRENTLIABILITIES, result);
        }

        @Test
        @DisplayName("Debe retornar NONCURRENTLIABILITIES cuando el estado es 'Pasivo No Corriente'")
        void testAdjustClassificationEnum_NonCurrentLiabilities() {
            String state = "Pasivo No Corriente";

            ClassificationEnum result = adjustEnumAccount.adjustClassificationEnum(state);

            assertEquals(ClassificationEnum.NONCURRENTLIABILITIES, result);
        }

        @Test
        @DisplayName("Debe retornar EQUITY cuando el estado es 'Patrimonio'")
        void testAdjustClassificationEnum_Equity() {
            String state = "Patrimonio";

            ClassificationEnum result = adjustEnumAccount.adjustClassificationEnum(state);

            assertEquals(ClassificationEnum.EQUITY, result);
        }

        @Test
        @DisplayName("Debe retornar NONOPERATINGINCOME cuando el estado es 'Ingresos No Operacionales'")
        void testAdjustClassificationEnum_NonOperatingIncome() {
            String state = "Ingresos No Operacionales";

            ClassificationEnum result = adjustEnumAccount.adjustClassificationEnum(state);

            assertEquals(ClassificationEnum.NONOPERATINGINCOME, result);
        }

        @Test
        @DisplayName("Debe retornar OPERATINGEXPENSES cuando el estado es 'Gastos Operacionales'")
        void testAdjustClassificationEnum_OperatingExpenses() {
            String state = "Gastos Operacionales";

            ClassificationEnum result = adjustEnumAccount.adjustClassificationEnum(state);

            assertEquals(ClassificationEnum.OPERATINGEXPENSES, result);
        }

        @Test
        @DisplayName("Debe retornar OPERATINGREVENUES cuando el estado es 'Ingresos Operacionales'")
        void testAdjustClassificationEnum_OperatingRevenues() {
            String state = "Ingresos Operacionales";

            ClassificationEnum result = adjustEnumAccount.adjustClassificationEnum(state);

            assertEquals(ClassificationEnum.OPERATINGREVENUES, result);
        }

        @Test
        @DisplayName("Debe aplicar trim al estado antes de procesarlo")
        void testAdjustClassificationEnum_WithWhitespace() {
            String state = "  Activo Corriente  ";

            ClassificationEnum result = adjustEnumAccount.adjustClassificationEnum(state);

            assertEquals(ClassificationEnum.CURRENTASSETS, result);
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el estado es null")
        void testAdjustClassificationEnum_NullState() {
            String state = null;

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustClassificationEnum(state)
            );

            assertEquals("La clasificación no puede ser nula o vacía", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el estado es vacío")
        void testAdjustClassificationEnum_EmptyState() {
            String state = "";

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustClassificationEnum(state)
            );

            assertEquals("La clasificación no puede ser nula o vacía", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el estado contiene solo espacios")
        void testAdjustClassificationEnum_WhitespaceOnlyState() {
            String state = "   ";

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustClassificationEnum(state)
            );

            assertEquals("La clasificación no puede ser nula o vacía", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el estado no es válido")
        void testAdjustClassificationEnum_InvalidState() {
            String state = "Clasificación Inválida";

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustClassificationEnum(state)
            );

            assertEquals("Clasificación no válida: Clasificación Inválida", exception.getMessage());
        }

        @Test
        @DisplayName("Debe ser case-sensitive y lanzar excepción para variantes en minúsculas")
        void testAdjustClassificationEnum_CaseSensitive() {
            String state = "activo corriente";

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustClassificationEnum(state)
            );

            assertEquals("Clasificación no válida: activo corriente", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests para adjustFinancialStatusEnum")
    class AdjustFinancialStatusEnumTests {

        @Test
        @DisplayName("Debe retornar INCOMESTATEMENT cuando el estado es 'Estado de Resultados'")
        void testAdjustFinancialStatusEnum_IncomeStatement() {
            String state = "Estado de Resultados";

            FinancialStatusEnum result = adjustEnumAccount.adjustFinancialStatusEnum(state);

            assertEquals(FinancialStatusEnum.INCOMESTATEMENT, result);
        }

        @Test
        @DisplayName("Debe retornar STATEMENTFINANCIALPOSITION cuando el estado es 'Estado de Situacion Financiero'")
        void testAdjustFinancialStatusEnum_StatementFinancialPosition() {
            String state = "Estado de Situacion Financiero";

            FinancialStatusEnum result = adjustEnumAccount.adjustFinancialStatusEnum(state);

            assertEquals(FinancialStatusEnum.STATEMENTFINANCIALPOSITION, result);
        }

        @Test
        @DisplayName("Debe aplicar trim al estado antes de procesarlo")
        void testAdjustFinancialStatusEnum_WithWhitespace() {
            String state = "  Estado de Resultados  ";

            FinancialStatusEnum result = adjustEnumAccount.adjustFinancialStatusEnum(state);

            assertEquals(FinancialStatusEnum.INCOMESTATEMENT, result);
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el estado es null")
        void testAdjustFinancialStatusEnum_NullState() {
            String state = null;

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustFinancialStatusEnum(state)
            );

            assertEquals("El estado financiero no puede ser nulo o vacío", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el estado es vacío")
        void testAdjustFinancialStatusEnum_EmptyState() {
            String state = "";

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustFinancialStatusEnum(state)
            );

            assertEquals("El estado financiero no puede ser nulo o vacío", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el estado contiene solo espacios")
        void testAdjustFinancialStatusEnum_WhitespaceOnlyState() {
            String state = "   ";

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustFinancialStatusEnum(state)
            );

            assertEquals("El estado financiero no puede ser nulo o vacío", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el estado no es válido")
        void testAdjustFinancialStatusEnum_InvalidState() {
            String state = "Estado Inválido";

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustFinancialStatusEnum(state)
            );

            assertEquals("Estado financiero no válido: Estado Inválido", exception.getMessage());
        }

        @Test
        @DisplayName("Debe ser case-sensitive y lanzar excepción para variantes en minúsculas")
        void testAdjustFinancialStatusEnum_CaseSensitive() {
            String state = "estado de resultados";

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustFinancialStatusEnum(state)
            );

            assertEquals("Estado financiero no válido: estado de resultados", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests para adjustNatureEnum")
    class AdjustNatureEnumTests {

        @Test
        @DisplayName("Debe retornar CREDIT cuando el estado es 'Credito'")
        void testAdjustNatureEnum_Credit() {
            String state = "Credito";

            NatureEnum result = adjustEnumAccount.adjustNatureEnum(state);

            assertEquals(NatureEnum.CREDIT, result);
        }

        @Test
        @DisplayName("Debe retornar DEBIT cuando el estado es 'Debito'")
        void testAdjustNatureEnum_Debit() {
            String state = "Debito";

            NatureEnum result = adjustEnumAccount.adjustNatureEnum(state);

            assertEquals(NatureEnum.DEBIT, result);
        }

        @Test
        @DisplayName("Debe aplicar trim al estado antes de procesarlo")
        void testAdjustNatureEnum_WithWhitespace() {
            String state = "  Credito  ";

            NatureEnum result = adjustEnumAccount.adjustNatureEnum(state);

            assertEquals(NatureEnum.CREDIT, result);
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el estado es null")
        void testAdjustNatureEnum_NullState() {
            String state = null;

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustNatureEnum(state)
            );

            assertEquals("La naturaleza no puede ser nula o vacía", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el estado es vacío")
        void testAdjustNatureEnum_EmptyState() {
            String state = "";

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustNatureEnum(state)
            );

            assertEquals("La naturaleza no puede ser nula o vacía", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el estado contiene solo espacios")
        void testAdjustNatureEnum_WhitespaceOnlyState() {
            String state = "   ";

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustNatureEnum(state)
            );

            assertEquals("La naturaleza no puede ser nula o vacía", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el estado no es válido")
        void testAdjustNatureEnum_InvalidState() {
            String state = "Naturaleza Inválida";

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustNatureEnum(state)
            );

            assertEquals("Naturaleza no válida: Naturaleza Inválida", exception.getMessage());
        }

        @Test
        @DisplayName("Debe ser case-sensitive y lanzar excepción para variantes en minúsculas")
        void testAdjustNatureEnum_CaseSensitive() {
            String state = "credito";

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustNatureEnum(state)
            );

            assertEquals("Naturaleza no válida: credito", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando se usa tilde en Crédito")
        void testAdjustNatureEnum_WithAccent() {
            String state = "Crédito";

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adjustEnumAccount.adjustNatureEnum(state)
            );

            assertEquals("Naturaleza no válida: Crédito", exception.getMessage());
        }
    }
}
