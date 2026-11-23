package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.controller;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IMessageProcessingErrorCommandPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IMessageProcessingErrorQueryPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.ApiResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.MessageProcessingErrorResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper.IMessageProcessingErrorRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments/message-processing-errors")
public class MessageProcessingErrorController {
    private final IMessageProcessingErrorQueryPort queryUseCase;
    private final IMessageProcessingErrorCommandPort commandUseCase;
    private final IMessageProcessingErrorRestMapper restMapper;

    /**
     * @brief Retrieves the most recent message processing error
     * @return Response with the latest message processing error or not found
     */
    @GetMapping("/last")
    public ResponseEntity<ApiResponse<MessageProcessingErrorResponse>> findLastRecord() {
        MessageProcessingError error = queryUseCase.findLastRecord();

        if (error == null) {
            return ResponseEntity.ok(
                    ApiResponse.successEmpty("No message processing errors found.",
                            "NO_CONTENT"));
        }

        MessageProcessingErrorResponse responseDto = restMapper.toResponse(error);
        return ResponseEntity.ok(
                ApiResponse.success(responseDto, "Latest message processing error found successfully."));
    }

    /**
     * @brief Deletes all message processing error records
     * @return Response confirming deletion
     */
    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponse<Void>> deleteAll() {
        commandUseCase.deleteAll();
        return ResponseEntity.ok(ApiResponse.success(null, "All message processing errors deleted successfully."));
    }
}
