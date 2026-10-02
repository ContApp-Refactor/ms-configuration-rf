package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.PDFgeneration;

import co.unicauca.edu.co.contables.accounting.facture.application.ports.output.FactureGeneratePDFOutputPort;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Enterprise;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Product;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.IEnterpriseSearchManagerPort;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.GetThirdUseCase;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;

import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.WriterException;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.awt.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.hibernate.metamodel.model.domain.DomainType;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

public class FacturePDFAdapter implements FactureGeneratePDFOutputPort {

        @Autowired
        private IEnterpriseSearchManagerPort enterpriseSearchManagerPort;

        @Autowired
        private GetThirdUseCase thirdUseCase;

        private Random random = new Random();

        /**
         * Genera un arreglo de bytes que representa una imagen JPEG
         * que contiene un c digo QR para la factura dada.
         *
         * @param facture la factura para la que se va a generar el
         *                c digo QR.
         * @return un arreglo de bytes que representa la imagen JPEG
         *         generada.
         */
        @Override
        public byte[] generateQR(Facture facture) {
                try {
                        Third third = this.thirdData(facture);
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date date = new Date();
                        Enterprise enterprise = this.enterpriseData(facture);

                        // Tamaño de la imagen
                        int width = 800;
                        int height = 600;
                        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2d = image.createGraphics();

                        // Fondo blanco
                        g2d.setColor(java.awt.Color.white);
                        g2d.fillRect(0, 0, width, height);
                        g2d.setColor(java.awt.Color.black);

                        Image logo = null;
                        try {
                                // Cargar imagen con iText
                                BufferedImage bufferedImage = ImageIO.read(new URL("yourImageUrlHere"));

                                // Convertir BufferedImage (java.awt.Image) a Image de iText
                                logo = new Image(ImageDataFactory.create(bufferedImage, null));

                                // Opcional: Cambiar el tamaño de la imagen
                                logo.setWidth(100);

                                // Dibujar la imagen utilizando Graphics2D (necesitamos convertir la imagen de
                                // iText a java.awt.Image)
                                BufferedImage awtLogo = bufferedImage; // ya que bufferedImage es de tipo java.awt.Image
                                g2d.drawImage(awtLogo, 50, 50, null); // Usamos null porque no necesitamos un
                                                                      // ImageObserver
                        } catch (MalformedURLException e) {
                                e.printStackTrace();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }

                        // Título
                        g2d.setFont(new Font("Arial", Font.BOLD, 24));
                        g2d.drawString("Factura de venta", 250, 50);

                        // Detalles de la factura
                        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
                        g2d.drawString("Número de Factura: " + facture.getFactCode(), 50, 100);
                        g2d.drawString("Fecha: " + dateFormat.format(date), 600, 100);
                        g2d.drawString("Cliente: " + third.getNames() + " " + third.getLastNames(), 50, 130);

                        // Encabezado de la tabla de productos
                        int tableStartY = 160;
                        int rowHeight = 30;
                        g2d.setFont(new Font("Arial", Font.BOLD, 14));
                        g2d.drawString("Código", 50, tableStartY);
                        g2d.drawString("Descripción", 150, tableStartY);
                        g2d.drawString("Cantidad", 300, tableStartY);
                        g2d.drawString("Precio Unitario", 390, tableStartY);
                        g2d.drawString("Descuento", 510, tableStartY);
                        g2d.drawString("IVA", 600, tableStartY);
                        g2d.drawString("Subtotal", 700, tableStartY);

                        // Línea de separación
                        g2d.drawLine(50, tableStartY + 5, 750, tableStartY + 5);

                        // Filas de la tabla de productos
                        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                        int currentY = tableStartY + rowHeight;
                        for (Product p : facture.getFactProducts()) {
                                g2d.drawString(p.getCode(), 50, currentY);
                                g2d.drawString(p.getDescription(), 150, currentY);
                                g2d.drawString(String.valueOf(p.getAmount()), 300, currentY);
                                g2d.drawString(String.format("%.2f", p.getUnitPrice()), 400, currentY);
                                g2d.drawString(String.format("%.2f", p.getDescount()), 500, currentY);
                                g2d.drawString(String.format("%.2f", p.getVat()), 600, currentY);
                                g2d.drawString(String.format("%.2f", p.getSubtotal()), 700, currentY);
                                currentY += rowHeight;
                        }

                        // Total de la factura
                        g2d.setFont(new Font("Arial", Font.BOLD, 14));
                        g2d.drawString("Subtotal: " + facture.getFactSubtotals(), 550, currentY + 20);
                        g2d.drawString("Descuento: " + facture.getDescounts(), 400, currentY + 20);
                        g2d.drawString("IVA: " + facture.getFacSalesTax(), 250, currentY + 20);
                        g2d.drawString("Total: " + (facture.getFacSalesTax() + facture.getFactSubtotals()
                                        - facture.getFacWithholdingSource()), 550, currentY + 50);

                        // Liberar recursos gráficos
                        g2d.dispose();

                        // Convertir la imagen a bytes
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(image, "jpeg", baos);
                        return baos.toByteArray();
                } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                }
        }

        /**
         * Genera un PDF para la factura dada. La responsabilidad de
         * guardar el PDF es del puerto de salida.
         *
         * @param facture La factura para la que se va a generar el PDF.
         * @return Un arreglo de bytes que representa el PDF generado.
         * @throws IOException Si ocurre un error al generar el PDF.
         */
        @Override
        public byte[] generatePDF(Facture facture) {
                try {
                        return generateInvoicePdf(facture);
                } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                }
        }

        /**
         * Genera un PDF para la factura dada. La responsabilidad de
         * guardar el PDF es del puerto de salida.
         *
         * @param facture La factura para la que se va a generar el PDF.
         * @return Un arreglo de bytes que representa el PDF generado.
         * @throws IOException Si ocurre un error al generar el PDF.
         */
        private byte[] generateInvoicePdf(Facture facture) throws IOException {
                if (facture.getFactureType().toString().equals("Venta")) {
                        return generateInvoiceSalePdf(facture);
                }

                /* Si es factura de compra con esto se valida el descuento */
                if (facture.getDescounts() == null) {
                        facture.setDescounts(0.0);
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc);
                document.setFontSize(10);

                DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                Date date = new Date();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, 24);

                Date newDate = calendar.getTime();
                DateFormat expirationDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedExpirationDate = expirationDateFormat.format(newDate);

                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

                Enterprise enterprise = this.enterpriseData(facture);
                Third third = this.thirdData(facture);

                Image logo = null;
                try {
                        BufferedImage bufferedImage = ImageIO.read(new URL(enterprise.getEntLogo()));
                        ByteArrayOutputStream imageBaos = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, "png", imageBaos);
                        ByteArrayInputStream imageBais = new ByteArrayInputStream(imageBaos.toByteArray());
                        ImageData imageData = ImageDataFactory.create(imageBais.readAllBytes());
                        logo = new Image(imageData);
                        logo.setWidth(100);
                        ;
                } catch (MalformedURLException e) {
                        e.printStackTrace();
                }

                // Informacion de empresa
                Table enterpriseData = new Table(1);
                enterpriseData.setWidthPercent(100);
                enterpriseData.addCell(new Cell().add(enterprise.getEntName()).setTextAlignment(TextAlignment.CENTER)
                                .setBorder(Border.NO_BORDER));
                enterpriseData.addCell(new Cell().add(boldText("Direccion: ", enterprise.getEntAddress()))
                                .setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
                enterpriseData.addCell(new Cell().add(boldText("NIT: ", enterprise.getEntNIT()))
                                .setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
                enterpriseData.addCell(new Cell().add(boldText("Contacto: ", enterprise.getEntContact()))
                                .setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
                enterpriseData.addCell(new Cell().add(new Paragraph("Factura Electrónica de compra"))
                                .setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
                enterpriseData.addCell(new Cell()
                                .add(boldText("Resolución de facturación aprobado por la DIAN No.",
                                                "\n" + this.random.nextLong(1000000)))
                                .setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));

                Table enterpriseDetails = new Table(2);
                enterpriseDetails.setWidthPercent(100);
                if (logo != null) {
                        enterpriseDetails.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER));
                } else {
                        enterpriseDetails.addCell(new Cell().add("Imagen no encontrada").setBorder(Border.NO_BORDER));
                }
                enterpriseDetails.addCell(new Cell().add(enterpriseData).setBorder(Border.NO_BORDER));
                document.add(enterpriseDetails);
                document.add(new Paragraph(" "));

                // Tabla detalles de factura
                Table invoiceDetails = new Table(2);
                invoiceDetails.setWidthPercent(100);
                invoiceDetails.addCell(new Cell().add(boldText("FECHA DE EMISIÓN: ", String.valueOf(LocalDate.now()))));
                invoiceDetails.addCell(
                                new Cell().add(boldText("AUT. NUMERACIÓN FAC: ", facture.getFactCode().toString())));
                invoiceDetails.addCell(new Cell().add(boldText("HORA DE EMISIÓN: ", dateFormat.format(date))));
                invoiceDetails.addCell(new Cell().add(boldText("FECHA DE VENCIMIENTO: ", formattedExpirationDate)));
                document.add(invoiceDetails);

                // Tabla detalles del Emisor
                document.add(new Paragraph("DATOS DEL EMISOR").setTextAlignment(TextAlignment.CENTER).setBold());
                Table transmitterDetails = new Table(2);
                transmitterDetails.setWidthPercent(100);
                transmitterDetails.addCell(new Cell().add(boldText("NIT: ", enterprise.getEntNIT())));
                transmitterDetails.addCell(new Cell().add(boldText("CONTACTO: ", enterprise.getEntContact())));
                transmitterDetails.addCell(new Cell().add(boldText("NOMBRE: ", enterprise.getEntName())));
                transmitterDetails.addCell(new Cell().add(boldText("DIRECCION: ", enterprise.getEntAddress())));
                document.add(transmitterDetails);

                // Tabla detalles del tercero
                document.add(new Paragraph("DATOS DEL CLIENTE").setTextAlignment(TextAlignment.CENTER).setBold());
                Table clientDetails = new Table(2);
                clientDetails.setWidthPercent(100);
                clientDetails.addCell(
                                new Cell().add(boldText("CÓDIGO DEL CLIENTE: ", third.getVerificationNumber() + "")));
                clientDetails.addCell(new Cell().add(boldText("DEPARTAMENTO: ", third.getProvince())));
                clientDetails.addCell(new Cell().add(boldText("TIPO DE DOCUMENTO: ", third.getTypeId())));
                clientDetails.addCell(new Cell().add(boldText("CIUDAD: ", third.getCity())));
                clientDetails.addCell(new Cell().add(boldText("NÚMERO DE DOCUMENTO: ", third.getIdNumber() + "")));
                clientDetails.addCell(new Cell().add(boldText("TELEFONO: ", third.getPhoneNumber())));
                clientDetails.addCell(new Cell()
                                .add(boldText("NOMBRE DE CLIENTE: ", third.getNames() + " " + third.getLastNames())));
                clientDetails.addCell(new Cell().add(boldText("CORREO: ", third.getEmail())));
                clientDetails.addCell(new Cell().add(boldText("DIRECCIÓN DEL CLIENTE: ", third.getAddress())));
                clientDetails.addCell(new Cell().add(boldText("TIPO DE PERSONA: ", third.getPersonType())));
                document.add(clientDetails);

                // tabla de productos
                document.add(new Paragraph("PRODUCTOS").setTextAlignment(TextAlignment.CENTER).setBold());
                Table itemTable = new Table(new float[] { 1, 3, 1, 2, 2 });
                itemTable.setWidthPercent(100);
                itemTable.addHeaderCell(new Cell().add(new Paragraph("CANTIDAD").setBold())
                                .setBackgroundColor(Color.LIGHT_GRAY));
                itemTable.addHeaderCell(new Cell().add(new Paragraph("DESCRIPCIÓN").setBold())
                                .setBackgroundColor(Color.LIGHT_GRAY));
                itemTable.addHeaderCell(
                                new Cell().add(new Paragraph("IVA").setBold()).setBackgroundColor(Color.LIGHT_GRAY));
                itemTable.addHeaderCell(new Cell().add(new Paragraph("PRECIO UNITARIO").setBold())
                                .setBackgroundColor(Color.LIGHT_GRAY));
                itemTable.addHeaderCell(new Cell().add(new Paragraph("VALOR TOTAL").setBold())
                                .setBackgroundColor(Color.LIGHT_GRAY));

                // Productos
                for (Product p : facture.getFactProducts()) {
                        itemTable.addCell(new Cell().add(new Paragraph(p.getAmount() + "")));
                        itemTable.addCell(new Cell().add(new Paragraph(p.getDescription())));
                        itemTable.addCell(new Cell().add(new Paragraph((p.getVat() * 100) + "%")));
                        itemTable.addCell(new Cell().add(new Paragraph(currencyFormat.format(p.getUnitPrice()))));
                        itemTable.addCell(new Cell()
                                        .add(new Paragraph(currencyFormat.format((p.getAmount() * p.getUnitPrice())))));
                }

                itemTable.addCell(new Cell().add(boldText("Cantidad Total: ", facture.getFactProducts().size() + "")));
                document.add(itemTable);
                document.add(new Paragraph(" "));
                document.add(new Paragraph(" "));

                // Cuenta total
                Table summaryTable = new Table(2);
                summaryTable.setWidthPercent(100);
                summaryTable.addCell(new Cell().add("SUBTOTALES").setBold().setBackgroundColor(Color.LIGHT_GRAY));
                summaryTable.addCell(new Cell().add(currencyFormat.format(facture.getFactSubtotals())));
                summaryTable.addCell(new Cell().add("IMPUESTO SOBRE LAS VENTAS").setBold()
                                .setBackgroundColor(Color.LIGHT_GRAY));
                summaryTable.addCell(new Cell().add(currencyFormat.format(facture.getFacSalesTax())));
                summaryTable.addCell(new Cell().add("RETFUENTE 2.5%").setBold().setBackgroundColor(Color.LIGHT_GRAY));
                summaryTable.addCell(new Cell().add(currencyFormat.format(facture.getFacWithholdingSource())));
                summaryTable.addCell(new Cell().add("TOTAL A PAGAR").setBold().setBackgroundColor(Color.LIGHT_GRAY));
                summaryTable.addCell(new Cell().add((currencyFormat.format(facture.getFactSubtotals()
                                + facture.getFacSalesTax() + facture.getFacWithholdingSource()))));
                document.add(summaryTable);

                document.add(new Paragraph(" "));

                // Tabla para el campo de Observaciones
                Table observationsTable = new Table(1); // Tabla de una sola columna
                observationsTable.setWidthPercent(100); // Configura la tabla al 100% del ancho

                // Celda de Observaciones
                observationsTable.addCell(new Cell()
                                .add(new Paragraph("Observaciones:").setBold())
                                .add(new Paragraph(
                                                // Agregar las observaciones de la factura, tiene que ser guardarce en
                                                // la bd
                                                facture.getFactObservations()))
                                .setPadding(10) // Espaciado interno en la celda
                                .setBorder(new SolidBorder(1)) // Borde sólido de grosor 1
                );

                // Añadir la tabla de observaciones al documento
                document.add(observationsTable);

                document.add(new Paragraph(" "));
                try {
                        // Crear una tabla con dos columnas y establecer los anchos (80% para texto, 20%
                        // para QR)
                        Table table = new Table(new float[] { 4, 1 });
                        table.setWidth(UnitValue.createPercentValue(100)); // Ancho total de la tabla al 100%

                        // Agregar el texto en la primera columna con tamaño de letra más pequeño
                        Paragraph textoFactura = new Paragraph(this.textForInvoiceFacture(facture)).setBold()
                                        .setTextAlignment(TextAlignment.LEFT)
                                        .setFontSize(8); // Tamaño de fuente más pequeño
                        table.addCell(new Cell().add(textoFactura).setBackgroundColor(Color.LIGHT_GRAY));

                        // Generar la imagen del código QR y establecer su tamaño aproximado
                        Image qrCodeImage = generateQRCodeImage(
                                        "http://contables.unicauca.edu.co/#/general/facturaQR/" + facture.getFactId());

                        // Crear una celda para el QR y aplicar escalado manual para que ocupe todo el
                        // espacio
                        Cell qrCell = new Cell();

                        // Añadir la imagen QR a la celda
                        qrCell.add(qrCodeImage).setPadding(0).setMargins(0, 0, 0, 0);
                        table.addCell(qrCell);

                        // Agregar la tabla al documento
                        document.add(table);

                } catch (Exception e) {
                        e.printStackTrace();
                }

                document.add(new Paragraph("**FACTURA GENERADA CON FINES EDUCATIVOS, NO TIENE NINGUN VALOR LEGAL**")
                                .setFontColor(Color.RED).setTextAlignment(TextAlignment.CENTER));

                document.close();

                return baos.toByteArray();
        }

        /**
         * Genera un PDF para una factura de venta.
         *
         * @param facture Factura de la que se va a generar el PDF.
         * @return El contenido del PDF en un arreglo de bytes.
         * @throws IOException Si ocurre un error al generar el PDF.
         */
        private byte[] generateInvoiceSalePdf(Facture facture) throws IOException {
                float tamañoEncabezadosTablas = 8;
                if (facture.getDescounts() == null) {
                        facture.setDescounts(0.0);
                }
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc);
                document.setFontSize(10);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date();

                // Configuración de tablas y encabezados
                Enterprise enterprise = this.enterpriseData(facture);
                Third third = this.thirdData(facture);

                Image logo = null;
                try {
                        BufferedImage bufferedImage = ImageIO.read(new URL(enterprise.getEntLogo()));
                        ByteArrayOutputStream imageBaos = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, "png", imageBaos);
                        ByteArrayInputStream imageBais = new ByteArrayInputStream(imageBaos.toByteArray());
                        ImageData imageData = ImageDataFactory.create(imageBais.readAllBytes());
                        logo = new Image(imageData).setWidth(100);
                } catch (MalformedURLException e) {
                        e.printStackTrace();
                }

                // Tabla de encabezado unificado con logo y detalles de la empresa
                Table headerTable = new Table(new float[] { 2, 8 });
                headerTable.setWidth(UnitValue.createPercentValue(100));

                if (logo != null) {
                        headerTable.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER));
                } else {
                        headerTable.addCell(new Cell().add("Logo no disponible").setBorder(Border.NO_BORDER));
                }

                Paragraph enterpriseInfo = new Paragraph()
                                .add(new Text(enterprise.getEntName()).setBold().setFontSize(24))
                                .add(new Text("\nDirección: ").setBold().setFontSize(13))
                                .add(new Text(enterprise.getEntAddress() + ", Colombia").setFontSize(13))
                                .add(new Text("\nNIT: ").setBold().setFontSize(13))
                                .add(new Text(enterprise.getEntNIT() + "-"
                                                + calcularDigitoVerificacion(enterprise.getEntNIT())).setFontSize(13))
                                .add(new Text("\nContacto: ").setBold().setFontSize(13))
                                .add(new Text(enterprise.getEntContact()).setFontSize(13))
                                .setTextAlignment(TextAlignment.LEFT);

                headerTable.addCell(new Cell().add(enterpriseInfo).setBorder(Border.NO_BORDER));
                document.add(headerTable);

                document.add(new Paragraph(" ")); // Espacio entre secciones

                document.add(new Paragraph(" ")); // Espacio entre secciones

                Table dataCliente = new Table(1);
                dataCliente.setWidthPercent(20);
                Paragraph DatosCliente = new Paragraph("Datos del cliente")
                                .setTextAlignment(TextAlignment.LEFT)
                                .setBold()
                                .setBackgroundColor(Color.LIGHT_GRAY)
                                .setBorder(Border.NO_BORDER);
                dataCliente.addCell(new Cell().add(DatosCliente).setBorder(Border.NO_BORDER));
                document.add(dataCliente);
                // Tabla principal que contiene las dos tablas internas
                Table mainTable = new Table(new float[] { 8, 2 }); // Dos columnas de diferente tamaño
                mainTable.setWidthPercent(100);

                // Primera tabla: detalles del cliente
                Table clientDetails = new Table(new float[] { 1, 2 }); // Una columna para títulos y otra para datos
                clientDetails.setWidthPercent(100);
                clientDetails.setHeight(100);

                // Agrega las celdas de información del cliente en dos columnas (título
                // sombreado y datos)
                clientDetails.addCell(
                                new Cell().add(new Paragraph("Cliente:").setBold().setFontSize(tamañoEncabezadosTablas))
                                                .setBackgroundColor(Color.LIGHT_GRAY));
                clientDetails.addCell(new Cell().add(new Paragraph(third.getNames() + " " + third.getLastNames())
                                .setFontSize(tamañoEncabezadosTablas)));

                clientDetails.addCell(
                                new Cell().add(new Paragraph("NIT:").setBold().setFontSize(tamañoEncabezadosTablas))
                                                .setBackgroundColor(Color.LIGHT_GRAY));
                clientDetails.addCell(new Cell()
                                .add(new Paragraph(third.getPhoneNumber()).setFontSize(tamañoEncabezadosTablas)));

                clientDetails.addCell(new Cell()
                                .add(new Paragraph("Dirección:").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY));
                clientDetails.addCell(
                                new Cell().add(new Paragraph(third.getAddress()).setFontSize(tamañoEncabezadosTablas)));

                clientDetails.addCell(
                                new Cell().add(new Paragraph("Ciudad:").setBold().setFontSize(tamañoEncabezadosTablas))
                                                .setBackgroundColor(Color.LIGHT_GRAY));
                clientDetails.addCell(
                                new Cell().add(new Paragraph(third.getCity())).setFontSize(tamañoEncabezadosTablas));

                clientDetails.addCell(new Cell()
                                .add(new Paragraph("Teléfono:").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY));
                clientDetails.addCell(new Cell()
                                .add(new Paragraph(third.getPhoneNumber()).setFontSize(tamañoEncabezadosTablas)));

                clientDetails.addCell(
                                new Cell().add(new Paragraph("Correo:").setBold().setFontSize(tamañoEncabezadosTablas))
                                                .setBackgroundColor(Color.LIGHT_GRAY));
                clientDetails.addCell(
                                new Cell().add(new Paragraph(third.getEmail()).setFontSize(tamañoEncabezadosTablas)));

                // Segunda tabla: información de la factura
                Table facturaDetails = new Table(1);
                facturaDetails.setHeight(100);
                facturaDetails.setWidthPercent(100);

                facturaDetails.addCell(new Cell()
                                .add(new Paragraph("FACTURA VENTA No IND " + facture.getFactCode().toString()).setBold()
                                                .setFontSize(12)
                                                .setTextAlignment(TextAlignment.CENTER))
                                .add(new Paragraph("Expedición: " + dateFormat.format(date))
                                                .setTextAlignment(TextAlignment.CENTER))
                                .add(new Paragraph("Vencimiento: " + dateFormat.format(date))
                                                .setTextAlignment(TextAlignment.CENTER))
                                .setVerticalAlignment(VerticalAlignment.MIDDLE));

                // Alinea ambas tablas en la tabla principal
                mainTable.addCell(new Cell().add(clientDetails).setBorder(Border.NO_BORDER));
                mainTable.addCell(new Cell().add(facturaDetails).setBorder(Border.NO_BORDER));

                // Añade la tabla contenedora al documento
                document.add(mainTable);
                // Productos
                Table nameTable = new Table(1);
                nameTable.setWidthPercent(20);
                Paragraph detalleFactura = new Paragraph("Detalle de Factura")
                                .setTextAlignment(TextAlignment.LEFT)
                                .setBold()
                                .setBackgroundColor(Color.LIGHT_GRAY)
                                .setBorder(Border.NO_BORDER);
                nameTable.addCell(new Cell().add(detalleFactura).setBorder(Border.NO_BORDER));
                document.add(nameTable);

                Table itemTable = new Table(new float[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 });
                itemTable.setWidthPercent(100);

                // Encabezados principales

                itemTable.addHeaderCell(new Cell(2, 1)
                                .add(new Paragraph("CÓDIGO").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY))
                                .setTextAlignment(TextAlignment.CENTER);
                ;
                itemTable.addHeaderCell(new Cell(2, 1)
                                .add(new Paragraph("DESCRIPCIÓN").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY))
                                .setTextAlignment(TextAlignment.CENTER);
                ;

                itemTable.addHeaderCell(new Cell(2, 1)
                                .add(new Paragraph("CANTIDAD").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY));
                itemTable.addHeaderCell(new Cell(2, 1)
                                .add(new Paragraph("PRECIO UNITARIO").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY))
                                .setTextAlignment(TextAlignment.CENTER);
                ;
                // Celda de encabezado IVA con subcolumnas
                Cell descontHeader = new Cell(1, 2)
                                .add(new Paragraph("DESCUENTOS").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY)
                                .setTextAlignment(TextAlignment.CENTER);
                itemTable.addHeaderCell(descontHeader);

                // Celda de encabezado IVA con subcolumnas
                Cell ivaHeader = new Cell(1, 2).add(new Paragraph("IVA").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY)
                                .setTextAlignment(TextAlignment.CENTER);
                itemTable.addHeaderCell(ivaHeader);

                itemTable.addHeaderCell(new Cell(2, 1)
                                .add(new Paragraph("VALOR TOTAL").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY))
                                .setTextAlignment(TextAlignment.CENTER);
                ;
                // Subencabezados para IVA
                itemTable.addHeaderCell(
                                new Cell().add(new Paragraph("$").setBold().setFontSize(tamañoEncabezadosTablas))
                                                .setBackgroundColor(Color.LIGHT_GRAY));
                itemTable.addHeaderCell(
                                new Cell().add(new Paragraph("%").setBold().setFontSize(tamañoEncabezadosTablas))
                                                .setBackgroundColor(Color.LIGHT_GRAY));
                itemTable.addHeaderCell(
                                new Cell().add(new Paragraph("$").setBold().setFontSize(tamañoEncabezadosTablas))
                                                .setBackgroundColor(Color.LIGHT_GRAY));
                itemTable.addHeaderCell(
                                new Cell().add(new Paragraph("%").setBold().setFontSize(tamañoEncabezadosTablas))
                                                .setBackgroundColor(Color.LIGHT_GRAY));

                // Filas de productos
                for (Product p : facture.getFactProducts()) {
                        itemTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getCode()))));
                        itemTable.addCell(new Cell().add(new Paragraph(p.getDescription())));
                        itemTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getAmount()))));
                        itemTable.addCell(new Cell().add(new Paragraph(currencyFormat.format(p.getUnitPrice()))));

                        Double descountValue = (p.getUnitPrice() * (p.getDescount() / 100)) * p.getAmount();
                        Double vatValue = ((p.getAmount() * p.getUnitPrice()) - descountValue) * p.getVat();
                        itemTable.addCell(new Cell().add(new Paragraph(currencyFormat.format(descountValue))));

                        itemTable.addCell(new Cell().add(new Paragraph((p.getDescount()) + "%")));

                        // Valores para IVA
                        itemTable.addCell(new Cell().add(new Paragraph(
                                        currencyFormat.format(vatValue))));
                        itemTable.addCell(new Cell().add(new Paragraph((p.getVat() * 100) + "%")));

                        itemTable.addCell(new Cell().add(new Paragraph(currencyFormat.format(
                                        (p.getAmount() * p.getUnitPrice()) - descountValue + vatValue))));
                }

                // Celda de cantidad total
                itemTable.addCell(
                                new Cell(1, 1).add(new Paragraph("Cantidad Total: " + facture.getFactProducts().size()))
                                                .setTextAlignment(TextAlignment.LEFT)
                                                .setBold().setFontSize(tamañoEncabezadosTablas));

                document.add(itemTable);

                Table resFactTable = new Table(1);
                resFactTable.setWidthPercent(20);
                Paragraph ResumenFactura = new Paragraph("Resumen de Factura")
                                .setTextAlignment(TextAlignment.LEFT)
                                .setBold()
                                .setBackgroundColor(Color.LIGHT_GRAY)
                                .setBorder(Border.NO_BORDER);
                resFactTable.addCell(new Cell().add(ResumenFactura).setBorder(Border.NO_BORDER));
                document.add(resFactTable);

                // Crear una tabla con tres columnas para mostrar los totales en una sola fila
                Table summaryTable = new Table(6); // Cuatro columnas
                summaryTable.setWidthPercent(100);

                // Agregar las celdas de subtotales, impuestos y total en una sola fila
                summaryTable.addCell(new Cell()
                                .add(new Paragraph("VALOR BRUTO").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));
                summaryTable.addCell(new Cell()
                                .add(new Paragraph("VALOR DESCUENTOS").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));
                summaryTable.addCell(new Cell()
                                .add(new Paragraph("VALOR IMPUESTOS").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));
                summaryTable.addCell(new Cell()
                                .add(new Paragraph("VALOR NETO").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));
                summaryTable.addCell(new Cell()
                                .add(new Paragraph("VALOR RETENCIÓN").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));
                summaryTable.addCell(new Cell()
                                .add(new Paragraph("VALOR DOCUMENTO").setBold().setFontSize(tamañoEncabezadosTablas))
                                .setBackgroundColor(Color.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));

                summaryTable.addCell(new Cell().add(new Paragraph(currencyFormat.format(facture.getFactSubtotals())))
                                .setTextAlignment(TextAlignment.CENTER));

                summaryTable.addCell(new Cell().add(new Paragraph(currencyFormat.format(facture.getDescounts())))
                                .setTextAlignment(TextAlignment.CENTER));
                summaryTable.addCell(
                                new Cell().add(new Paragraph(currencyFormat.format(facture.getFacSalesTax())))
                                                .setTextAlignment(TextAlignment.CENTER));
                summaryTable.addCell(new Cell()
                                .add(new Paragraph(currencyFormat
                                                .format(facture.getFactSubtotals() + facture.getFacSalesTax())))
                                .setTextAlignment(TextAlignment.CENTER));
                summaryTable.addCell(
                                new Cell().add(new Paragraph(currencyFormat.format(facture.getFacWithholdingSource())))
                                                .setTextAlignment(TextAlignment.CENTER));

                summaryTable.addCell(new Cell()
                                .add(new Paragraph(currencyFormat
                                                .format(facture.getFactSubtotals() + facture.getFacSalesTax()
                                                                - facture.getFacWithholdingSource())))
                                .setTextAlignment(TextAlignment.CENTER));

                // Agregar la tabla de resumen al documento
                document.add(summaryTable);

                document.add(new Paragraph(" "));

                // Tabla para el campo de Observaciones
                Table observationsTable = new Table(1); // Tabla de una sola columna
                observationsTable.setWidthPercent(100); // Configura la tabla al 100% del ancho

                // Celda de Observaciones
                observationsTable.addCell(new Cell()
                                .add(new Paragraph("Observaciones:").setBold())
                                .add(new Paragraph(
                                                // Agregar las observaciones de la factura, tiene que ser guardarce en
                                                // la bd
                                                facture.getFactObservations()))
                                .setPadding(10) // Espaciado interno en la celda
                                .setBorder(new SolidBorder(1)) // Borde sólido de grosor 1
                );

                // Añadir la tabla de observaciones al documento
                document.add(observationsTable);

                document.add(new Paragraph(" "));

                try {
                        // Crear una tabla con dos columnas y establecer los anchos (80% para texto, 20%
                        // para QR)
                        Table table = new Table(new float[] { 4, 1 });
                        table.setWidth(UnitValue.createPercentValue(100)); // Ancho total de la tabla al 100%

                        // Agregar el texto en la primera columna con tamaño de letra más pequeño
                        Paragraph textoFactura = new Paragraph(this.textForFacture(facture)).setBold()
                                        .setTextAlignment(TextAlignment.LEFT)
                                        .setFontSize(8); // Tamaño de fuente más pequeño
                        table.addCell(new Cell().add(textoFactura).setBackgroundColor(Color.LIGHT_GRAY));

                        // Generar la imagen del código QR y establecer su tamaño aproximado
                        Image qrCodeImage = generateQRCodeImage(
                                        "http://contables.unicauca.edu.co/#/general/facturaQR/" + facture.getFactId());

                        // Crear una celda para el QR y aplicar escalado manual para que ocupe todo el
                        // espacio
                        Cell qrCell = new Cell();

                        // Añadir la imagen QR a la celda
                        qrCell.add(qrCodeImage).setPadding(0).setMargins(0, 0, 0, 0);
                        table.addCell(qrCell);

                        // Agregar la tabla al documento
                        document.add(table);

                } catch (Exception e) {
                        e.printStackTrace();
                }

                document.close();
                return baos.toByteArray();
        }

        /**
         * Genera una imagen de un código QR a partir de una cadena de texto,
         * utilizando la librería ZXing.
         *
         * @param text La cadena de texto que se va a codificar en el código QR.
         * @return La imagen generada.
         * @throws WriterException Si ocurre un error al escribir el código QR.
         * @throws IOException     Si ocurre un error al escribir la imagen.
         */
        private Image generateQRCodeImage(String text) throws WriterException, IOException {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 100, 100);

                BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < 100; x++) {
                        for (int y = 0; y < 100; y++) {
                                bufferedImage.setRGB(x, y,
                                                bitMatrix.get(x, y) ? java.awt.Color.BLACK.getRGB()
                                                                : java.awt.Color.WHITE.getRGB());
                        }
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                return new Image(ImageDataFactory.create(bais.readAllBytes()));
        }

        /**
         * Obtiene la informacion de la empresa asociada a una factura
         * consultando el puerto de entrada del módulo de empresas dentro del
         * monolito (sin llamadas HTTP internas).
         *
         * @param facture La factura para la que se va a obtener la informaci n
         *                de la empresa.
         * @return Un objeto Enterprise con la informaci n de la empresa.
         */
        private Enterprise enterpriseData(Facture facture) {
                co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise empresa =
                                enterpriseSearchManagerPort.getEnterpriseById(UUID.fromString(facture.getEntId()));
                if (empresa == null) {
                        throw new IllegalStateException(
                                        "No se encontró la empresa " + facture.getEntId() + " para la factura");
                }
                String contact = empresa.getEmail() + " - " + empresa.getPhone();
                return new Enterprise(
                                empresa.getName(),
                                "Calle 12 # 12-12",
                                empresa.getNit(),
                                contact,
                                empresa.getLogo());
        }

        /**
         * Calcula el dígito de verificación para un NIT dado. La fórmula
         * utilizada es la definida en el Decreto 2785 de 2005 del Ministerio
         * de Hacienda y Crédito Público de Colombia.
         *
         * @param nit El NIT para el que se va a calcular el dígito de
         *            verificación.
         * @return El dígito de verificación calculado.
         */
        private int calcularDigitoVerificacion(String nit) {
                // Factores según la posición (de derecha a izquierda)
                int[] factores = { 71, 67, 59, 53, 47, 43, 41, 37, 29, 23, 19, 17, 13, 7, 3 };
                int suma = 0;

                // Iteramos sobre el NIT desde el último dígito hasta el primero
                for (int i = 0; i < nit.length(); i++) {
                        // Obtenemos el dígito actual de derecha a izquierda
                        int digito = Character.getNumericValue(nit.charAt(nit.length() - 1 - i));
                        // Multiplicamos el dígito por el factor correspondiente y sumamos
                        suma += digito * factores[i];
                }

                // Calculamos el residuo de la división de la suma por 11
                int residuo = suma % 11;

                // Regla para determinar el dígito de verificación
                if (residuo == 0 || residuo == 1) {
                        return residuo;
                } else {
                        return 11 - residuo;
                }
        }

        /**
         * Genera un número aleatorio de n dígitos.
         * 
         * @param n El número de dígitos que se desean generar.
         * @return Un String que representa el número aleatorio generado.
         */
        private String generateNumberAleatory(int n) {
                // Prefijo fijo
                StringBuilder number = new StringBuilder("");

                // Generador de números aleatorios
                Random random = new Random();

                // Generar el número aleatorio de n dígitos y agregarlo al prefijo
                for (int i = 0; i < n; i++) {
                        number.append(random.nextInt(10)); // Agrega un dígito aleatorio entre 0 y 9
                }

                return number.toString();
        }

        /**
         * Devuelve la fecha actual en formato "yyyyMMdd".
         * 
         * @return La fecha actual en formato "yyyyMMdd".
         */
        private String getFormattedDate() {
                // Obtener la fecha actual
                LocalDate fechaActual = LocalDate.now();

                // Formatear la fecha en "yyyyMMdd"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                return fechaActual.format(formatter);
        }

        /**
         * Genera el prefijo para la factura, tomando los 3 primeros caracteres del
         * nombre
         * de la empresa asociada a la factura y convirtiendo a mayúsculas.
         * 
         * @param facture La factura para la que se va a generar el prefijo.
         * @return El prefijo en mayúsculas.
         */
        private String prefijoFacture(Facture facture) {
                String prefijo = enterpriseData(facture).getEntName().substring(0, 3);
                return prefijo.toUpperCase();
        }

        /**
         * Genera un texto descriptivo para una factura de venta, incorporando
         * referencias legales
         * y otra información relevante.
         *
         * Este texto incluye una declaración que la factura está sujeta a las
         * regulaciones
         * concernientes a las letras de cambio (Artículo 5 Ley 1231 de 2008).
         * Confirma que el comprador ha recibido los bienes o servicios descritos.
         *
         * El texto también especifica un número de autorización, fecha de aprobación,
         * prefijo de empresa y un período de validez. Incluye información sobre la
         * facturación de la DIAN, responsabilidad del IVA, actividad económica,
         * y un código CUFE único.
         *
         * @param facture La entidad de factura para la cual se genera el texto.
         * @return La cadena de texto formateada que contiene los detalles de la
         *         factura.
         */
        private String textForFacture(Facture facture) {
                String textoFacture = "A esta factura de venta aplican las normas relativas a la letra de cambio (artículo 5 Ley 1231 de 2008).\n"
                                +
                                "Con esta el Comprador declara haber recibido real y materialmente las mercancías o prestación de servicios descritos en este título - Valor.\n"
                                +
                                "Número Autorización 1876" + generateNumberAleatory(8) + " aprobado en "
                                + getFormattedDate() + " Prefijo " + prefijoFacture(facture)
                                + " desde el número 00001 al 40000 Vigencia: 12 Meses\n" +
                                "Facturación DIAN \n" +
                                "Responsable de IVA - Actividad Económica " + generateNumberAleatory(4) + "\n" +
                                "CUFE: " + generateCUFE() + "\n";
                return textoFacture;
        }

        /**
         * Genera un texto descriptivo para una factura de compra, incorporando
         * referencias legales
         * y otra información relevante.
         *
         * Este texto incluye una declaración que la factura está sujeta a las
         * regulaciones
         * concernientes a las letras de cambio (Artículo 5 Ley 1231 de 2008).
         * Confirma que el comprador ha recibido los bienes o servicios descritos.
         *
         * El texto también especifica un número de autorización, fecha de aprobación,
         * prefijo de empresa y un período de validez. Incluye información sobre la
         * facturación de la DIAN, responsabilidad del IVA, actividad económica,
         * y un código CUFE único.
         * 
         * @param facture La entidad de factura para la cual se genera el texto.
         * @return La cadena de texto formateada que contiene los detalles de la
         *         factura.
         */
        private String textForInvoiceFacture(Facture facture) {
                String textoFacture = "A esta factura de compra aplican las normas relativas a la letra de cambio (artículo 5 Ley 1231 de 2008).\n"
                                +
                                "Con esta el Comprador declara haber recibido real y materialmente las mercancías o prestación de servicios descritos en este título - Valor.\n"
                                +
                                "Número Autorización 1876" + generateNumberAleatory(8) + " aprobado en "
                                + getFormattedDate() + " Prefijo " + prefijoFacture(facture)
                                + " desde el número 00001 al 40000 Vigencia: 12 Meses\n" +
                                "Facturación DIAN \n" +
                                "Responsable de IVA - Actividad Económica " + generateNumberAleatory(4) + "\n" +
                                "CUFE: " + generateCUFE() + "\n";
                return textoFacture;
        }

        /**
         * Recupera y construye un objeto Tercero a partir del puerto de entrada
         * del módulo de terceros dentro del monolito (sin llamadas HTTP
         * internas), usando el tercero y la empresa de la factura.
         *
         * @param factura El objeto Factura cuyo ID de tercero se utilizará para obtener
         *                los detalles del tercero.
         * @return Un objeto Tercero poblado con los datos del módulo de terceros.
         */
        private Third thirdData(Facture facture) {
                co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third tercero =
                                thirdUseCase.getThirdById(facture.getThId(), facture.getEntId());
                if (tercero == null) {
                        throw new IllegalStateException(
                                        "No se encontró el tercero " + facture.getThId() + " de la factura");
                }
                return new Third(
                                tercero.getVerificationNumber(),
                                tercero.getTypeId() != null ? tercero.getTypeId().getTypeId() : null,
                                tercero.getIdNumber(),
                                tercero.getNames(),
                                tercero.getLastNames(),
                                tercero.getAddress(),
                                tercero.getCountry() != null ? tercero.getCountry().getCountryName() : null,
                                tercero.getProvince() != null ? tercero.getProvince().getStateName() : null,
                                tercero.getCity() != null ? tercero.getCity().getCityName() : null,
                                tercero.getPhoneNumber(),
                                tercero.getEmail(),
                                tercero.getPersonType() != null ? tercero.getPersonType().getCode() : null);
        }

        private String generateCUFE() {
                int length = 82; // Longitud deseada del código hexadecimal
                SecureRandom random = new SecureRandom();
                StringBuilder hexCode = new StringBuilder();

                while (hexCode.length() < length) {
                        int randomValue = random.nextInt(256); // Genera un número aleatorio de 0 a 255
                        hexCode.append(String.format("%02x", randomValue)); // Lo convierte a hexadecimal de 2 dígitos
                }

                return hexCode.substring(0, length);
        }

        // Funcion que recibe 2 String para hacer un parrafo con uno de ellos en negrita
        private Paragraph boldText(String textToBold, String textToNormal) {
                Paragraph paragraph = new Paragraph();
                Text textbold = new Text(textToBold).setBold();
                Text textNormal = new Text(textToNormal);
                paragraph.add(textbold);
                paragraph.add(textNormal);
                return paragraph;
        }
}
