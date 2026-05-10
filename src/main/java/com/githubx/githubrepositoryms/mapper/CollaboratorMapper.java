package com.githubx.githubrepositoryms.mapper;

import com.githubx.githubrepositoryms.model.CollaboratorDocument;
import com.smithy.g.repo.server.collaborator.model.CollaboratorDTO;
import com.smithy.g.repo.server.collaborator.model.CollaboratorRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface CollaboratorMapper {

    @Mapping(target = "role", source = "role", qualifiedByName = "stringToCollaboratorRole")
    @Mapping(target = "addedAt", source = "addedAt", qualifiedByName = "dateTimeToString")
    CollaboratorDTO toDto(CollaboratorDocument doc);

    @Named("stringToCollaboratorRole")
    static CollaboratorRole stringToCollaboratorRole(String value) {
        return value != null ? CollaboratorRole.valueOf(value) : CollaboratorRole.READ;
    }

    @Named("dateTimeToString")
    static String dateTimeToString(LocalDateTime ldt) {
        return ldt != null ? ldt.toString() : null;
    }
}
