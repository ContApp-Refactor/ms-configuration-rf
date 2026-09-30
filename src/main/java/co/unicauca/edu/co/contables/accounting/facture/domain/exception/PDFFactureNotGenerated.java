package co.unicauca.edu.co.contables.accounting.facture.domain.exception;

public class PDFFactureNotGenerated extends RuntimeException{
    public PDFFactureNotGenerated(String message){
        super(message);
    }
}
