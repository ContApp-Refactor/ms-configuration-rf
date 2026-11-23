package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.messageProcessingError.IMessageProcessingErrorCommandPort;
import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.messageProcessingError.IMessageProcessingErrorQueryPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.MessageProcessingErrorDtoResponse;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper.IMessageProcessingErrorResponseMapper;

import lombok.RequiredArgsConstructor;

/**
 * @brief REST controller for MessageProcessingError operations
 * 
 * Provides HTTP endpoints for managing message processing error records
 * including query operations and deletion capabilities.
 */
@RestController("kardexMessageProcessingErrorController")@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average/message-processing-errors")
@Validated
public class MessageProcessingErrorController {

    private final IMessageProcessingErrorQueryPort messageProcessingErrorQueryPort;
    private final IMessageProcessingErrorCommandPort messageProcessingErrorCommandPort;
    private final IMessageProcessingErrorResponseMapper messageProcessingErrorResponseMapper;

    /**
     * @brief Retrieves a message processing error by ID
     * @param id Error record identifier
     * @return Response with message processing error information or not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<MessageProcessingErrorDtoResponse>> findById(@PathVariable Long id) {
        Optional<MessageProcessingError> messageProcessingError = messageProcessingErrorQueryPort.findById(id);
        
        MessageProcessingErrorDtoResponse response = messageProcessingErrorResponseMapper.toDtoResponse(messageProcessingError.get());
        ResponseDto<MessageProcessingErrorDtoResponse> responseDto = ResponseDto.<MessageProcessingErrorDtoResponse>builder()
                .data(response)
                .status(200)
                .message("Message processing error found successfully")
                .build();
        return responseDto.of();
    }

    /**
     * @brief Retrieves the most recent message processing error
     * @return Response with the latest message processing error or not found
     */
    @GetMapping("/last")
    public ResponseEntity<ResponseDto<MessageProcessingErrorDtoResponse>> findLastRecord() {
        Optional<MessageProcessingError> messageProcessingError = messageProcessingErrorQueryPort.findLastRecord();
        
        MessageProcessingErrorDtoResponse response = messageProcessingErrorResponseMapper.toDtoResponse(messageProcessingError.get());
        ResponseDto<MessageProcessingErrorDtoResponse> responseDto = ResponseDto.<MessageProcessingErrorDtoResponse>builder()
                .data(response)
                .status(200)
                .message("Latest message processing error found successfully")
                .build();
        return responseDto.of();
    }

    /**
     * @brief Retrieves all message processing errors with pagination
     * @param pageable Pagination parameters
     * @return Response with paginated message processing errors
     */
    @GetMapping("/paginated")
    public ResponseEntity<ResponseDto<Page<MessageProcessingErrorDtoResponse>>> findAllPaginated(Pageable pageable) {
        Page<MessageProcessingError> messageProcessingErrors = messageProcessingErrorQueryPort.findAll(pageable);
        Page<MessageProcessingErrorDtoResponse> response = messageProcessingErrors.map(messageProcessingErrorResponseMapper::toDtoResponse);
        
        ResponseDto<Page<MessageProcessingErrorDtoResponse>> responseDto = ResponseDto.<Page<MessageProcessingErrorDtoResponse>>builder()
                .data(response)
                .status(200)
                .message("Message processing errors retrieved successfully")
                .build();
        return responseDto.of();
    }

    /**
     * @brief Deletes a specific message processing error by ID
     * @param id Error record identifier
     * @return Response confirming deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> deleteById(@PathVariable Long id) {
        messageProcessingErrorCommandPort.deleteById(id);
        
        ResponseDto<Void> responseDto = ResponseDto.<Void>builder()
                .data(null)
                .status(200)
                .message("Message processing error deleted successfully")
                .build();
        return responseDto.of();
    }

    /**
     * @brief Deletes all message processing error records
     * @return Response confirming deletion
     */
    @DeleteMapping
    public ResponseEntity<ResponseDto<Void>> deleteAll() {
        messageProcessingErrorCommandPort.deleteAll();
        
        ResponseDto<Void> responseDto = ResponseDto.<Void>builder()
                .data(null)
                .status(200)
                .message("All message processing errors deleted successfully")
                .build();
        return responseDto.of();
    }
}