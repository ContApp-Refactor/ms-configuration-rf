package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.controller;

import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPayableWriteOffCommandUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPayableWriteOffQueryUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.*;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.assembler.WriteOffResponseAssembler;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.dto.TreasuryDtos.WriteOffRequest;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.dto.TreasuryDtos.WriteOffResponse;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.mapper.IPayableWriteOffRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/treasury/payable-write-offs") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('user_client','admin_client','super_client','Estudiante','Profesor','Administrador')")
public class PayableWriteOffController {
    private final IPayableWriteOffCommandUseCase commands;
    private final IPayableWriteOffQueryUseCase queries;
    private final IPayableWriteOffRestMapper mapper;
    private final WriteOffResponseAssembler assembler;
    @PostMapping public WriteOffResponse create(@Valid@RequestBody WriteOffRequest r){return assembler.toResponse(commands.create(mapper.toCommand(r)));}
    @GetMapping public List<WriteOffResponse> list(@RequestParam String enterpriseId){return assembler.toResponseList(queries.list(enterpriseId));}
    @GetMapping("/{id}")public WriteOffResponse find(@PathVariable Long id){return assembler.toResponse(queries.find(id));}
    @PostMapping("/{id}/confirm")public WriteOffResponse confirm(@PathVariable Long id){return assembler.toResponse(commands.confirm(id));}
    @PostMapping("/{id}/discard")public WriteOffResponse discardDraft(@PathVariable Long id){return assembler.toResponse(commands.discardDraft(id));}
    @PostMapping("/{id}/void")public WriteOffResponse voidWriteOff(@PathVariable Long id){return assembler.toResponse(commands.voidWriteOff(id));}
}
