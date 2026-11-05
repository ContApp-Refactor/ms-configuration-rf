package co.unicauca.edu.co.contables.accounting.facture.domain.exception;

public class FactureNotFound extends RuntimeException{
    public FactureNotFound(String message){
        super(message);
    }
}
