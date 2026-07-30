package co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.services;

import java.util.List;
import java.util.Optional;

import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.models.Tag;

public interface ITagService {
    Tag create(Tag tag);

    Tag update(Long idTag, Tag tag);

    Optional<Tag> getTag(Long idTag, String enterpriseId);

    Optional<Tag> getTagById(Long idTag);

    List<Tag> getAllTag(String enterpriseId);

    boolean delete(Long idTag);
}
