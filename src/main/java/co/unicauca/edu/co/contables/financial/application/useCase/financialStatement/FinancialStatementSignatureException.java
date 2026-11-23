package co.unicauca.edu.co.contables.financial.application.useCase.financialStatement;

public class FinancialStatementSignatureException extends RuntimeException {

    public FinancialStatementSignatureException(String message) {
        super(message);
    }

    public FinancialStatementSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
