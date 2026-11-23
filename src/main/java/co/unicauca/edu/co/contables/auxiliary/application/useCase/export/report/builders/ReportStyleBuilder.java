package co.unicauca.edu.co.contables.auxiliary.application.useCase.export.report.builders;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.ExportInfo;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAlignment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.ImageBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief Constructor de estilos visuales para reportes de libros auxiliares.
 *
 * Configura paleta de colores, fuentes, encabezados, pie de página, título
 * con logo y bloque de criterios sobre un {@link JasperReportBuilder}, a
 * partir de la plantilla {@link AuxiliaryBookTemplate} suministrada por
 * la entidad para personalizar la apariencia del reporte exportado.
 */
@Service
@Getter
@NoArgsConstructor
public class ReportStyleBuilder {

    private StyleBuilder textStyle;
    private StyleBuilder headerStyle;
    private StyleBuilder titleStyle;
    private StyleBuilder criteriaTitleStyle;
    private StyleBuilder tableCellStyle;
    private StyleBuilder oddRowStyle;

    private Color primaryColor;
    private Color accentColor;
    private Color softBackground;

    private int size;

    private static final Color DEFAULT_PRIMARY = new Color(11, 60, 97); // Deep blue, professional palette

    private void setTextStyle(AuxiliaryBookTemplate template) {
        String fontName = template.getFont() != null && !template.getFont().isBlank() ? template.getFont() : "Arial";
        Integer fontSize = template.getFontSize();
        this.size = fontSize != null && fontSize > 0 ? fontSize : 11;
        this.textStyle = DynamicReports.stl.style()
                .setFontName(fontName)
                .setFontSize(this.size)
                .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                .setPadding(2);
    }

    private void setHeaderStyle() {
        Color lightHeaderBg = new Color(240, 242, 245);
        this.headerStyle = DynamicReports.stl.style(this.textStyle)
                .setFontSize(this.size + 1)
                .setBold(true)
                .setBackgroundColor(lightHeaderBg)
                .setForegroundColor(primaryColor)
                .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                .setPadding(8)
                .setTopBorder(DynamicReports.stl.pen().setLineWidth(0f))
                .setBottomBorder(DynamicReports.stl.pen().setLineWidth(1.5f).setLineColor(accentColor))
                .setLeftBorder(DynamicReports.stl.pen().setLineWidth(0f))
                .setRightBorder(DynamicReports.stl.pen().setLineWidth(0f));
    }

    private void setTitleAndCriteriaStyle() {
        this.titleStyle = DynamicReports.stl.style(this.textStyle)
                .setFontSize(this.size + 6)
                .setBold(true)
                .setForegroundColor(primaryColor);

        this.criteriaTitleStyle = DynamicReports.stl.style(this.textStyle)
                .setBold(true)
                .setForegroundColor(primaryColor)
                .setTopPadding(10);
    }

    private void setTableCellStyle() {
        Color rowDivider = new Color(228, 231, 235);

        this.tableCellStyle = DynamicReports.stl.style(this.textStyle)
                .setTopBorder(DynamicReports.stl.pen().setLineWidth(0f))
                .setBottomBorder(DynamicReports.stl.pen().setLineWidth(0.4f).setLineColor(rowDivider))
                .setLeftBorder(DynamicReports.stl.pen().setLineWidth(0f))
                .setRightBorder(DynamicReports.stl.pen().setLineWidth(0f))
                .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
                .setPadding(7);

        this.oddRowStyle = DynamicReports.stl.style(this.tableCellStyle)
                .setBackgroundColor(new Color(248, 249, 250));
    }

    private ImageBuilder buildLogoImage(URL logoUrl) {
        System.out.println("Intentando cargar logo desde URL: " + logoUrl);
        if (logoUrl == null) {
            return Components.image(new ByteArrayInputStream(new byte[0]))
                    .setFixedDimension(1, 1);
        }

        try {
            HttpsURLConnection connection = (HttpsURLConnection) logoUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:99.0) Gecko/20100101 Firefox/99.0");
            InputStream imageStream = connection.getInputStream();

            return Components.image(imageStream)
                    .setFixedHeight(50)
                    .setFixedWidth(120);

        } catch (IOException | ClassCastException e) {
            System.err.println("DynamicReports no pudo cargar la imagen desde la URL: " + logoUrl + ". Error: " + e.getMessage());
            return Components.image(new ByteArrayInputStream(new byte[0]))
                    .setFixedDimension(1, 1);
        }
    }

    private VerticalListBuilder buildCriteriaComponent(AuxiliaryBookCriteria criteria, StyleBuilder baseStyle) {
        StyleBuilder labelStyle = DynamicReports.stl.style(baseStyle).setBold(true);
        StyleBuilder valueStyle = DynamicReports.stl.style(baseStyle).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        List<ComponentBuilder<?, ?>> criteriaItems = new ArrayList<>();

        if (criteria.getCriteriaType() != null) {
            criteriaItems.add(createCriteriaRow("Tipo de Nivel:", criteria.getCriteriaType().getDisplayName(), labelStyle, valueStyle));
        }
        if (criteria.hasRange()) {
            String range = criteria.getCriteriaRange().getFromRange() + " a " + criteria.getCriteriaRange().getToRange();
            criteriaItems.add(createCriteriaRow("Rango:", range, labelStyle, valueStyle));
        }
        if (criteria.getCostCenterId() != null) {
            criteriaItems.add(createCriteriaRow("Centro de costo:", criteria.getCostCenterId(), labelStyle, valueStyle));
        }
        if (criteria.getThirdPartyId() != null) {
            criteriaItems.add(createCriteriaRow("Tercero:", criteria.getThirdPartyId(), labelStyle, valueStyle));
        }
        if (criteria.getVoucherType() != null) {
            criteriaItems.add(createCriteriaRow("Tipo de comprobante:", criteria.getVoucherType(), labelStyle, valueStyle));
        }
        if (criteria.getStartDate() != null) {
            criteriaItems.add(createCriteriaRow("Fecha de Inicio:", criteria.getStartDate().toString(), labelStyle, valueStyle));
        }
        if (criteria.getEndDate() != null) {
            criteriaItems.add(createCriteriaRow("Fecha de Corte:", criteria.getEndDate().toString(), labelStyle, valueStyle));
        }

        VerticalListBuilder leftColumn = Components.verticalList();
        VerticalListBuilder rightColumn = Components.verticalList();
        int middle = (int) Math.ceil(criteriaItems.size() / 2.0);

        for (int i = 0; i < criteriaItems.size(); i++) {
            ComponentBuilder<?, ?> item = criteriaItems.get(i);
            if (i < middle) {
                leftColumn.add(item);
                leftColumn.add(Components.verticalGap(8));
            } else {
                rightColumn.add(item);
                rightColumn.add(Components.verticalGap(8));
            }
        }

        HorizontalListBuilder columns = Components.horizontalList().add(leftColumn, rightColumn).setGap(40);
        VerticalListBuilder finalComponent = Components.verticalList();
        finalComponent.add(Components.text("Criterios Utilizados:").setStyle(criteriaTitleStyle));
        if (!criteriaItems.isEmpty()) {
            finalComponent.add(columns);
        }
        return finalComponent;
    }

    private VerticalListBuilder createCriteriaRow(String label, String value, StyleBuilder labelStyle, StyleBuilder valueStyle) {
        return Components.verticalList(
                Components.text(label).setStyle(labelStyle),
                Components.text(value).setStyle(valueStyle)
        );
    }


    private void setTitle(JasperReportBuilder report, AuxiliaryBookTemplate template, ExportInfo exportInfo) {
        EAlignment rawAlignment = template.getAlienation();
        EAlignment alignment = rawAlignment != null ? rawAlignment : EAlignment.RIGHT;

        HorizontalTextAlignment textAlignment = switch (alignment) {
            case LEFT -> HorizontalTextAlignment.LEFT;
            case RIGHT -> HorizontalTextAlignment.RIGHT;
            default -> HorizontalTextAlignment.CENTER;
        };

        ComponentBuilder<?, ?> dateTimeComponent = Components.verticalList(
                Components.text("Generado en:")
                        .setStyle(DynamicReports.stl.style(textStyle).setBold(true))
                        .setHorizontalTextAlignment(textAlignment),

                Components.currentDate()
                        .setPattern("HH:mm dd/MM/yyyy")
                        .setStyle(textStyle)
                        .setHorizontalTextAlignment(textAlignment)
        );

        ImageBuilder logo = buildLogoImage(template.getPathLogotype());
        ComponentBuilder<?, ?> titles = Components.verticalList(
                Components.text(exportInfo.getEntName()).setStyle(titleStyle).setHorizontalTextAlignment(textAlignment),
                Components.text(template.getName()).setStyle(textStyle).setHorizontalTextAlignment(textAlignment)
        );
        HorizontalListBuilder headerList = Components.horizontalList().setGap(10);
        switch (alignment) {
            case LEFT:
                headerList.add(logo, titles).add(Components.filler()).add(dateTimeComponent);
                break;
            case CENTER:
                headerList.add(Components.filler(), logo, titles, Components.filler()).add(dateTimeComponent);
                break;
            case RIGHT:
            default:
                headerList.add(dateTimeComponent).add(Components.filler()).add(titles, logo);
                break;
        }

        VerticalListBuilder criteriaList = buildCriteriaComponent(exportInfo.getAuxiliaryBook().getCriteria(), this.textStyle);
        report.title(
                Components.verticalList(
                        headerList,
                        Components.verticalGap(10),
                        Components.line().setStyle(DynamicReports.stl.style().setForegroundColor(accentColor)),
                        Components.verticalGap(5),
                        criteriaList,
                        Components.verticalGap(20)
                )
        );
    }

    private void setPageFooter(JasperReportBuilder report, AuxiliaryBookTemplate template) {
        Integer rawFontSize = template.getFontSize();
        int baseFontSize = rawFontSize != null ? rawFontSize : 11;
        int footerSize = Math.max(8, baseFontSize - 2);
        StyleBuilder footerStyle = DynamicReports.stl.style(textStyle)
                .setFontSize(footerSize)
                .setForegroundColor(accentColor);

        HorizontalListBuilder pageNumberComponent = Components.horizontalList(
                Components.pageNumber()
                        .setStyle(footerStyle)
                        .setFixedWidth(25)
                        .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT),
                Components.text(" de ")
                        .setStyle(footerStyle)
                        .setFixedWidth(22)
                        .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER),
                Components.totalPages()
                        .setStyle(footerStyle)
                        .setFixedWidth(25)
                        .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
        );

        report.pageFooter(
                Components.horizontalList()
                        .add(Components.filler(), pageNumberComponent)
        );
    }

    public void templateBuilder(JasperReportBuilder report, AuxiliaryBookTemplate template, ExportInfo exportInfo) {
        buildPalette(template);
        this.setTextStyle(template);
        this.setTableCellStyle();
        this.setHeaderStyle();
        this.setTitleAndCriteriaStyle();

        this.setTitle(report, template, exportInfo);
        this.setPageFooter(report, template);

        report.setTemplate(
                DynamicReports.template()
                        .setColumnStyle(this.tableCellStyle)
                        .setColumnTitleStyle(this.headerStyle)
        );
    }

    private void buildPalette(AuxiliaryBookTemplate template) {
        this.primaryColor = safeColor(template.getMainColor(), DEFAULT_PRIMARY);
        this.accentColor = darken(primaryColor, 0.15f);
        this.softBackground = lighten(primaryColor, 0.85f);
    }

    private Color safeColor(String value, Color fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Color.decode(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Color lighten(Color color, float factor) {
        int r = Math.min(255, (int) (color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int) (color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int) (color.getBlue() + (255 - color.getBlue()) * factor));
        return new Color(r, g, b);
    }

    private Color darken(Color color, float factor) {
        int r = Math.max(0, (int) (color.getRed() * (1 - factor)));
        int g = Math.max(0, (int) (color.getGreen() * (1 - factor)));
        int b = Math.max(0, (int) (color.getBlue() * (1 - factor)));
        return new Color(r, g, b);
    }
}
