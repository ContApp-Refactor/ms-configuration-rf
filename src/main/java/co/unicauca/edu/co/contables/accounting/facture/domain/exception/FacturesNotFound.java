package co.unicauca.edu.co.contables.accounting.facture.domain.exception;

public class FacturesNotFound extends RuntimeException{
    public FacturesNotFound(String message){
        super(message);
    }
}
