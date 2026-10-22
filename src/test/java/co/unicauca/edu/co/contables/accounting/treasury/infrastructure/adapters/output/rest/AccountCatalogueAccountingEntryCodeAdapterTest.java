package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IAccountingSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountingEntryNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountCatalogueAccountingEntryCodeAdapterTest {

    @Mock
    private IAccountingSearchInputPort accountingSearchInputPort;

    private AccountCatalogueAccountingEntryCodeAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AccountCatalogueAccountingEntryCodeAdapter(accountingSearchInputPort);
    }

    @Test
    @DisplayName("resolveCode devuelve el codigo del asiento")
    void resolveCodeDevuelveElCodigo() {
        when(accountingSearchInputPort.findAccountingEntryById(11L))
                .thenReturn(AccountingEntry.builder().code("AE-2024-001").build());

        assertEquals(Optional.of("AE-2024-001"), adapter.resolveCode(11L));
    }

    @Test
    @DisplayName("Id nulo no toca el puerto")
    void idNuloNoInvocaElPuerto() {
        assertTrue(adapter.resolveCode(null).isEmpty());
        verifyNoInteractions(accountingSearchInputPort);
    }

    @Test
    @DisplayName("Asiento inexistente devuelve vacio (equivale al error anterior)")
    void asientoInexistente() {
        when(accountingSearchInputPort.findAccountingEntryById(11L))
                .thenThrow(new AccountingEntryNotFoundException("Asiento contable con ID 11 no encontrado."));

        assertTrue(adapter.resolveCode(11L).isEmpty());
    }

    @Test
    @DisplayName("Asiento sin codigo devuelve vacio")
    void asientoSinCodigo() {
        when(accountingSearchInputPort.findAccountingEntryById(11L))
                .thenReturn(AccountingEntry.builder().code("  ").build());

        assertTrue(adapter.resolveCode(11L).isEmpty());
    }

    @Test
    @DisplayName("Cualquier otra excepcion no se propaga")
    void excepcionNoSePropaga() {
        when(accountingSearchInputPort.findAccountingEntryById(11L))
                .thenThrow(new IllegalStateException("boom"));

        assertTrue(adapter.resolveCode(11L).isEmpty());
    }
}
