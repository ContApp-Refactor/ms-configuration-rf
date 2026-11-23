package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.AccountCatalogueAccountSnapshot;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountCatalogueAccountCodeAdapterTest {

    @Mock
    private IAccountCatalogueSearchInputPort searchPort;

    private AccountCatalogueAccountCodeAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AccountCatalogueAccountCodeAdapter(searchPort);
    }

    private AccountCatalogue account(Long id, String code, ClassificationEnum classification, Boolean status) {
        return AccountCatalogue.builder()
                .id(id)
                .code(code)
                .description("Cuenta " + code)
                .classification(classification)
                .status(status)
                .build();
    }

    @Test
    @DisplayName("resolveAccount devuelve el snapshot con la classification como ordinal del enum")
    void resolveAccountMapeaLaClassificationComoOrdinal() {
        when(searchPort.getAllAccountsByEnterprise("55433c17")).thenReturn(List.of(
                account(7L, "211101", ClassificationEnum.CURRENTLIABILITIES, true)));

        Optional<AccountCatalogueAccountSnapshot> snapshot =
                adapter.resolveAccount(7L, "55433c17");

        assertTrue(snapshot.isPresent());
        assertEquals("211101", snapshot.get().code());
        assertEquals("Cuenta 211101", snapshot.get().description());
        assertEquals(2, snapshot.get().classification());
        assertTrue(snapshot.get().active());
        assertTrue(snapshot.get().isPayableLiability());
    }

    @Test
    @DisplayName("Pasivo no corriente (ordinal 3) tambien es obligacion valida")
    void classificationTresTambienEsObligacion() {
        when(searchPort.getAllAccountsByEnterprise("ent")).thenReturn(List.of(
                account(8L, "211102", ClassificationEnum.NONCURRENTLIABILITIES, true)));

        Optional<AccountCatalogueAccountSnapshot> snapshot = adapter.resolveAccount(8L, "ent");

        assertTrue(snapshot.isPresent());
        assertEquals(3, snapshot.get().classification());
        assertTrue(snapshot.get().isPayableLiability());
    }

    @Test
    @DisplayName("Cuenta 11xx (activo corriente) nunca es obligacion valida")
    void cuentaDeActivoCorrienteNoEsObligacion() {
        when(searchPort.getAllAccountsByEnterprise("ent")).thenReturn(List.of(
                account(9L, "110101", ClassificationEnum.CURRENTASSETS, true)));

        Optional<AccountCatalogueAccountSnapshot> snapshot = adapter.resolveAccount(9L, "ent");

        assertTrue(snapshot.isPresent());
        assertEquals(0, snapshot.get().classification());
        assertFalse(snapshot.get().isPayableLiability());
    }

    @Test
    @DisplayName("Cuenta inactiva se refleja como active=false")
    void cuentaInactiva() {
        when(searchPort.getAllAccountsByEnterprise("ent")).thenReturn(List.of(
                account(10L, "510101", ClassificationEnum.OPERATINGEXPENSES, false)));

        Optional<AccountCatalogueAccountSnapshot> snapshot = adapter.resolveAccount(10L, "ent");

        assertTrue(snapshot.isPresent());
        assertFalse(snapshot.get().active());
        assertFalse(snapshot.get().isPayableLiability());
    }

    @Test
    @DisplayName("Id inexistente en la empresa devuelve vacio")
    void idInexistenteDevuelveVacio() {
        when(searchPort.getAllAccountsByEnterprise("ent")).thenReturn(List.of(
                account(7L, "211101", ClassificationEnum.CURRENTLIABILITIES, true)));

        assertTrue(adapter.resolveAccount(999L, "ent").isEmpty());
    }

    @Test
    @DisplayName("Guards: id nulo, empresa nula o en blanco no tocan el puerto")
    void guardsNoInvocanElPuerto() {
        assertTrue(adapter.resolveAccount(null, "ent").isEmpty());
        assertTrue(adapter.resolveAccount(1L, null).isEmpty());
        assertTrue(adapter.resolveAccount(1L, "   ").isEmpty());
        verifyNoInteractions(searchPort);
    }

    @Test
    @DisplayName("Una excepcion del modulo catalogue no se propaga: devuelve vacio")
    void excepcionDelPuertoNoSePropaga() {
        when(searchPort.getAllAccountsByEnterprise("ent")).thenThrow(new IllegalStateException("boom"));

        assertTrue(adapter.resolveAccount(7L, "ent").isEmpty());
    }

    @Test
    @DisplayName("resolveCode delega en resolveAccount y devuelve el codigo")
    void resolveCodeDevuelveElCodigo() {
        when(searchPort.getAllAccountsByEnterprise("ent")).thenReturn(List.of(
                account(7L, "211101", ClassificationEnum.CURRENTLIABILITIES, true)));

        assertEquals(Optional.of("211101"), adapter.resolveCode(7L, "ent"));
    }
}
