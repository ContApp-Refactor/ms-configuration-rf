package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.domain.model.CounterpartyRole;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartySnapshot;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.GetThirdUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdNotFound;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** TEN-1/DRF-2: el adaptador de terceros llama en proceso al puerto de entrada del modulo thirds. */
@ExtendWith(MockitoExtension.class)
class BillingThirdPartyAdapterTest {

    private static final String ENTERPRISE = "11111111-1111-1111-1111-111111111111";
    private static final long THIRD_ID = 10L;

    @Mock
    private GetThirdUseCase getThirdUseCase;

    private BillingThirdPartyAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BillingThirdPartyAdapter(getThirdUseCase);
    }

    private static Third.ThirdBuilder baseThird() {
        return Third.builder()
                .thId(THIRD_ID)
                .entId(ENTERPRISE)
                .typeId(TypeId.builder().typeId("NIT").typeIdname("Numero de identificacion tributaria").build())
                .idNumber(900123456L)
                .verificationNumber(7L)
                .address("Calle 1 # 2-3")
                .email("tercero@example.com")
                .phoneNumber("3001234567")
                .state(true);
    }

    @Test
    void THIRD_maps_all_fields_with_social_reason_as_name() {
        when(getThirdUseCase.getThirdById(THIRD_ID, ENTERPRISE))
                .thenReturn(baseThird().socialReason("Distribuidora Andina SAS").names("Ignorado").build());

        Optional<CounterpartySnapshot> result = adapter.findCounterparty(ENTERPRISE, THIRD_ID,
                CounterpartyRole.CUSTOMER);

        assertThat(result).isPresent();
        CounterpartySnapshot snapshot = result.get();
        assertThat(snapshot.thirdId()).isEqualTo(THIRD_ID);
        assertThat(snapshot.role()).isEqualTo(CounterpartyRole.CUSTOMER);
        assertThat(snapshot.name()).isEqualTo("Distribuidora Andina SAS");
        assertThat(snapshot.idType()).isEqualTo("NIT");
        assertThat(snapshot.idNumber()).isEqualTo("900123456");
        assertThat(snapshot.verificationNumber()).isEqualTo("7");
        assertThat(snapshot.address()).isEqualTo("Calle 1 # 2-3");
        assertThat(snapshot.email()).isEqualTo("tercero@example.com");
        assertThat(snapshot.phone()).isEqualTo("3001234567");
        assertThat(snapshot.active()).isTrue();
    }

    @Test
    void THIRD_name_uses_names_and_last_names_when_social_reason_is_blank() {
        when(getThirdUseCase.getThirdById(THIRD_ID, ENTERPRISE))
                .thenReturn(baseThird().socialReason("   ").names("Maria").lastNames("Gomez Perez").build());

        CounterpartySnapshot snapshot = adapter.findCounterparty(ENTERPRISE, THIRD_ID, CounterpartyRole.CUSTOMER)
                .orElseThrow();

        assertThat(snapshot.name()).isEqualTo("Maria Gomez Perez");
    }

    @Test
    void THIRD_name_trims_when_last_names_is_missing() {
        when(getThirdUseCase.getThirdById(THIRD_ID, ENTERPRISE))
                .thenReturn(baseThird().names("Maria").lastNames(null).build());

        CounterpartySnapshot snapshot = adapter.findCounterparty(ENTERPRISE, THIRD_ID, CounterpartyRole.SUPPLIER)
                .orElseThrow();

        assertThat(snapshot.name()).isEqualTo("Maria");
        assertThat(snapshot.role()).isEqualTo(CounterpartyRole.SUPPLIER);
    }

    @Test
    void THIRD_not_found_exception_becomes_empty() {
        when(getThirdUseCase.getThirdById(THIRD_ID, ENTERPRISE)).thenThrow(new ThirdNotFound());

        assertThat(adapter.findCounterparty(ENTERPRISE, THIRD_ID, CounterpartyRole.CUSTOMER)).isEmpty();
    }

    @Test
    void THIRD_null_result_becomes_empty() {
        when(getThirdUseCase.getThirdById(THIRD_ID, ENTERPRISE)).thenReturn(null);

        assertThat(adapter.findCounterparty(ENTERPRISE, THIRD_ID, CounterpartyRole.CUSTOMER)).isEmpty();
    }

    @Test
    void THIRD_inactive_is_returned_with_active_false() {
        when(getThirdUseCase.getThirdById(THIRD_ID, ENTERPRISE))
                .thenReturn(baseThird().socialReason("Inactivo SAS").state(false).build());

        CounterpartySnapshot snapshot = adapter.findCounterparty(ENTERPRISE, THIRD_ID, CounterpartyRole.CUSTOMER)
                .orElseThrow();

        assertThat(snapshot.active()).isFalse();
    }

    @Test
    void THIRD_missing_optional_fields_map_to_null() {
        when(getThirdUseCase.getThirdById(THIRD_ID, ENTERPRISE))
                .thenReturn(baseThird().socialReason("Sin extras SAS").verificationNumber(null).address(null)
                        .email(null).phoneNumber(null).build());

        CounterpartySnapshot snapshot = adapter.findCounterparty(ENTERPRISE, THIRD_ID, CounterpartyRole.CUSTOMER)
                .orElseThrow();

        assertThat(snapshot.verificationNumber()).isNull();
        assertThat(snapshot.address()).isNull();
        assertThat(snapshot.email()).isNull();
        assertThat(snapshot.phone()).isNull();
    }

    @Test
    void THIRD_other_errors_propagate() {
        when(getThirdUseCase.getThirdById(THIRD_ID, ENTERPRISE)).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> adapter.findCounterparty(ENTERPRISE, THIRD_ID, CounterpartyRole.CUSTOMER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    @Test
    void THIRD_no_third_type_filter_a_customer_lookup_accepts_any_third() {
        // H0-3: solo se valida existencia y activo, sin filtrar por ThirdType.
        when(getThirdUseCase.getThirdById(THIRD_ID, ENTERPRISE))
                .thenReturn(baseThird().socialReason("Cualquiera SAS").build());

        assertThat(adapter.findCounterparty(ENTERPRISE, THIRD_ID, CounterpartyRole.SUPPLIER)).isPresent();
        assertThat(adapter.findCounterparty(ENTERPRISE, THIRD_ID, CounterpartyRole.CUSTOMER)).isPresent();
    }
}
