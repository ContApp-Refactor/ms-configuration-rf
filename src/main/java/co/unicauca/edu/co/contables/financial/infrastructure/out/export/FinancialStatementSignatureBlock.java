package co.unicauca.edu.co.contables.financial.infrastructure.out.export;

public record FinancialStatementSignatureBlock(
        byte[] image,
        String signerName,
        String signerRole
) {
}