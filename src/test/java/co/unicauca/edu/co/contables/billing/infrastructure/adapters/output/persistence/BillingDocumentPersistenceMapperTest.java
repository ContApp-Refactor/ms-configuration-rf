package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.customer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSaleReturnState;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;

import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.Quantity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentEntity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentLineEntity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.mapper.BillingDocumentPersistenceMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Mapeo dominio <-> entidad sin base de datos: columnas, enums como String y fusion de lineas por lineNo (D9, D10). */
class BillingDocumentPersistenceMapperTest {

    private final BillingDocumentPersistenceMapper mapper = new BillingDocumentPersistenceMapper();

    private static BillingDocument draftWithLines(int count) {
        BillingDocument doc = draftSale();
        List<DocumentLine> lines = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            lines.add(lineOf(i, product(i), String.valueOf(i)));
        }
        doc.replaceInvoiceDraft(customer(), TODAY, null, lines, NOW, USER);
        return doc;
    }

    @Test
    void D10_enums_are_stored_as_plain_strings_and_scalars_are_copied() {
        BillingDocument doc = readySaleDraft();

        BillingDocumentEntity entity = mapper.newEntity(doc);

        assertThat(entity.getId()).isEqualTo(doc.id());
        assertThat(entity.getEnterpriseId()).isEqualTo(ENTERPRISE_ID);
        assertThat(entity.getDocumentType()).isEqualTo("SALE_INVOICE");
        assertThat(entity.getStatus()).isEqualTo("BORRADOR");
        assertThat(entity.getNumberPrefix()).isNull();
        assertThat(entity.getNumberValue()).isNull();
        assertThat(entity.getNumberDisplay()).isNull();
        assertThat(entity.getCounterpartyId()).isEqualTo(10L);
        assertThat(entity.getCounterpartyRole()).isEqualTo("CUSTOMER");
        assertThat(entity.getCurrency()).isEqualTo("COP");
        assertThat(entity.getTenantId()).as("el tenant lo llena Hibernate, nunca el mapper").isNull();
        assertThat(entity.getVersion()).as("version nula = entidad nueva").isNull();
    }

    @Test
    void D6_total_columns_come_from_the_derived_totals() {
        BillingDocument doc = readySaleDraft(); // 2 x 100.00 + 19% = 200.00 / 38.00 / 238.00

        BillingDocumentEntity entity = mapper.newEntity(doc);

        assertThat(entity.getSubtotal()).isEqualByComparingTo("200.00");
        assertThat(entity.getTaxTotal()).isEqualByComparingTo("38.00");
        assertThat(entity.getTotal()).isEqualByComparingTo("238.00");
    }

    @Test
    void emitted_document_fills_number_and_issuer_columns() {
        BillingDocumentEntity entity = mapper.newEntity(emittedSale());

        assertThat(entity.getStatus()).isEqualTo("EMITIDA");
        assertThat(entity.getNumberPrefix()).isEqualTo("FV");
        assertThat(entity.getNumberValue()).isEqualTo(7L);
        assertThat(entity.getNumberDisplay()).isEqualTo("FV-000007");
        assertThat(entity.getIssuerName()).isEqualTo("Empresa Emisora SAS");
        assertThat(entity.getIssuerNit()).isEqualTo("900123456");
        assertThat(entity.getIssuerDv()).isEqualTo("1");
        assertThat(entity.getEmittedAt()).isNotNull();
        assertThat(entity.getEmittedBy()).isEqualTo("approver");
    }

    @Test
    void line_columns_store_catalog_overrides_and_effective_values() {
        BillingDocument doc = draftSale();
        DocumentLine line = DocumentLine.forInvoice(1, product(1L, "100.00", "19"),
                Quantity.of(new BigDecimal("2")), money("150.00"), rate("5"));
        doc.replaceInvoiceDraft(customer(), TODAY, null, List.of(line), NOW, USER);

        BillingDocumentEntity entity = mapper.newEntity(doc);
        BillingDocumentLineEntity row = entity.getLines().get(0);

        assertThat(row.getLineNo()).isEqualTo(1);
        assertThat(row.getDocument()).isSameAs(entity);
        assertThat(row.getCatalogUnitPrice()).isEqualByComparingTo("100.00");
        assertThat(row.getCatalogTaxRate()).isEqualByComparingTo("19.00");
        assertThat(row.getUnitPriceOverride()).isEqualByComparingTo("150.00");
        assertThat(row.getTaxRateOverride()).isEqualByComparingTo("5.00");
        assertThat(row.getUnitPrice()).isEqualByComparingTo("150.00");
        assertThat(row.getTaxRate()).isEqualByComparingTo("5.00");
        assertThat(row.getSubtotal()).isEqualByComparingTo("300.00");
        assertThat(row.getTaxAmount()).isEqualByComparingTo("15.00");
        assertThat(row.getTotal()).isEqualByComparingTo("315.00");
        assertThat(row.getQuantity()).isEqualByComparingTo("2.000");
    }

    @Test
    void round_trip_of_invoice_states_is_lossless() {
        for (BillingDocument doc : List.of(draftSale(), readySaleDraft(), emittedSale(), emittedPurchase())) {
            BillingDocument back = mapper.toDomain(mapper.newEntity(doc));

            assertThat(back.state()).isEqualTo(doc.state());
        }
    }

    @Test
    void round_trip_of_a_return_keeps_original_reference_and_source_line() {
        BillingDocument doc = BillingDocument.restore(draftSaleReturnState("producto danado"));

        BillingDocumentEntity entity = mapper.newEntity(doc);
        BillingDocument back = mapper.toDomain(entity);

        assertThat(entity.getOriginalDocumentType()).isEqualTo("SALE_INVOICE");
        assertThat(entity.getOriginalNumberDisplay()).isEqualTo("FV-000007");
        assertThat(entity.getReturnReason()).isEqualTo("producto danado");
        assertThat(back.state()).isEqualTo(doc.state());
        assertThat(back.original().number().display()).isEqualTo("FV-000007");
        assertThat(back.lines().get(0).sourceLineNo()).isEqualTo(1);
    }

    @Test
    void entity_version_is_carried_into_the_restored_document() {
        BillingDocumentEntity entity = mapper.newEntity(readySaleDraft());
        entity.setVersion(4L);

        assertThat(mapper.toDomain(entity).version()).isEqualTo(4L);
    }

    @Test
    void D9_apply_updates_existing_lines_in_place_and_drops_the_tail_when_shrinking() {
        BillingDocumentEntity entity = mapper.newEntity(draftWithLines(3));
        BillingDocumentLineEntity first = entity.getLines().get(0);
        BillingDocumentLineEntity second = entity.getLines().get(1);

        mapper.apply(draftWithLines(2), entity);

        assertThat(entity.getLines()).hasSize(2);
        assertThat(entity.getLines().get(0)).isSameAs(first);
        assertThat(entity.getLines().get(1)).isSameAs(second);
        assertThat(entity.getLines()).extracting(BillingDocumentLineEntity::getLineNo).containsExactly(1, 2);
    }

    @Test
    void D9_apply_adds_the_extra_lines_at_the_tail_when_growing() {
        BillingDocumentEntity entity = mapper.newEntity(draftWithLines(2));
        BillingDocumentLineEntity first = entity.getLines().get(0);

        mapper.apply(draftWithLines(4), entity);

        assertThat(entity.getLines()).hasSize(4);
        assertThat(entity.getLines().get(0)).isSameAs(first);
        assertThat(entity.getLines()).extracting(BillingDocumentLineEntity::getLineNo).containsExactly(1, 2, 3, 4);
        assertThat(entity.getLines()).allSatisfy(row -> assertThat(row.getDocument()).isSameAs(entity));
    }

    @Test
    void D9_apply_rewrites_the_values_of_a_line_that_keeps_its_number() {
        BillingDocumentEntity entity = mapper.newEntity(draftWithLines(2));
        BillingDocument changed = draftSale();
        changed.replaceInvoiceDraft(customer(), TODAY, null,
                List.of(lineOf(1, product(9L, "50.00", "0"), "3")), NOW.plusSeconds(60), USER);

        mapper.apply(changed, entity);

        assertThat(entity.getLines()).hasSize(1);
        BillingDocumentLineEntity row = entity.getLines().get(0);
        assertThat(row.getProductId()).isEqualTo(9L);
        assertThat(row.getQuantity()).isEqualByComparingTo("3.000");
        assertThat(row.getTotal()).isEqualByComparingTo("150.00");
    }

    @Test
    void apply_clears_a_removed_counterparty_and_all_lines() {
        BillingDocumentEntity entity = mapper.newEntity(readySaleDraft());
        BillingDocument cleared = draftSale();

        mapper.apply(cleared, entity);

        assertThat(entity.getCounterpartyId()).isNull();
        assertThat(entity.getCounterpartyRole()).isNull();
        assertThat(entity.getCounterpartyName()).isNull();
        assertThat(entity.getLines()).isEmpty();
        assertThat(entity.getTotal()).isEqualByComparingTo("0.00");
    }

    @Test
    void summary_is_built_from_columns_without_touching_lines() {
        BillingDocumentEntity entity = mapper.newEntity(emittedSale());
        entity.setVersion(2L);

        BillingDocumentSummary summary = mapper.toSummary(entity);

        assertThat(summary.id()).isEqualTo(entity.getId());
        assertThat(summary.status().name()).isEqualTo("EMITIDA");
        assertThat(summary.number().display()).isEqualTo("FV-000007");
        assertThat(summary.counterpartyId()).isEqualTo(10L);
        assertThat(summary.counterpartyName()).isEqualTo("Cliente 10");
        assertThat(summary.totals().total().value()).isEqualByComparingTo("238.00");
        assertThat(summary.version()).isEqualTo(2L);
        assertThat(summary.originalDocumentId()).isNull();
    }

    @Test
    void with_version_helper_matches_mapper_version_handling() {
        BillingDocument doc = withVersion(readySaleDraft(), 3L);

        BillingDocumentEntity entity = mapper.newEntity(doc);

        assertThat(entity.getVersion()).as("newEntity nunca copia la version: la asigna JPA").isNull();
    }
}
