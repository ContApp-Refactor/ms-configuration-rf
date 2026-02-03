package co.unicauca.edu.co.contables.configuration.noCommercialTags.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.mapper.ITagDomainMapper;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.models.Tag;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.services.ITagService;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.presentation.DTO.request.TagDTORequest;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.presentation.DTO.response.TagDTOResponse;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;




@RestController
@RequiredArgsConstructor
@RequestMapping("/api/config/tag")
public class TagRestController {
    private final ITagDomainMapper tagDomainMapper;
    private final ITagService tagService;

    @PreAuthorize("hasAuthority('NCT#C')")
    @PostMapping("/create")
    public ResponseEntity<TagDTOResponse> createTag(@RequestBody TagDTORequest tagDTORequest) {
        Tag tag=tagDomainMapper.toDomain(tagDTORequest);
        Tag objTag=tagService.create(tag);
        ResponseEntity<TagDTOResponse> objResponse=new ResponseEntity<TagDTOResponse>(tagDomainMapper.toResponse(objTag),HttpStatus.CREATED);
        return objResponse;
    }

    @PreAuthorize("hasAuthority('NCT#U')")
    @PutMapping("/update/{idTag}")
    public ResponseEntity<TagDTOResponse> updateTag(@PathVariable Long idTag, @RequestBody TagDTORequest tagDTORequest) {
        Tag tagUpdate=tagDomainMapper.toDomain(tagDTORequest);
        Tag objTag=tagService.update(idTag, tagUpdate);
        ResponseEntity<TagDTOResponse> objResponse=new ResponseEntity<TagDTOResponse>(tagDomainMapper.toResponse(objTag),HttpStatus.CREATED);
        return objResponse;
    }

    @GetMapping("/enterprise/{enterpriseId}/tag/{idTag}")
    public ResponseEntity<TagDTOResponse> getTag(@PathVariable String enterpriseId,@PathVariable Long idTag) {
        
         return tagService.getTag(idTag,enterpriseId)
        .map(tag -> ResponseEntity.ok(tagDomainMapper.toResponse(tag)))
        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/findAll/{enterpriseId}")
    public ResponseEntity<List<TagDTOResponse>> getAllTags(@PathVariable String enterpriseId) {
        List<Tag> tags = tagService.getAllTag(enterpriseId);
        List<TagDTOResponse> responseList = tags.stream()
        .map(tagDomainMapper::toResponse)
        .toList();

        return ResponseEntity.ok(responseList);
    }

    @PreAuthorize("hasAuthority('NCT#D')")
    @DeleteMapping("/deletetag/{idTag}")
   public ResponseEntity<Void> deleteTag(@PathVariable Long idTag) {
        boolean deleted = tagService.delete(idTag);

        if (deleted) {
            return ResponseEntity.noContent().build(); 
        } else {
            return ResponseEntity.notFound().build(); 
        }
    }
}
    

    
    

