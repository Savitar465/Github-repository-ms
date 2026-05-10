package com.githubx.githubrepositoryms.mapper;

import com.githubx.githubrepositoryms.model.RepositoryDocument;
import com.smithy.g.repo.server.repository.model.RepositoryDTO;
import com.smithy.g.repo.server.repository.model.RepoVisibility;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface RepositoryMapper {

    @Mapping(target = "starsCount", source = "starsCount", qualifiedByName = "intToBigDecimal")
    @Mapping(target = "forksCount", source = "forksCount", qualifiedByName = "intToBigDecimal")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "dateTimeToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "dateTimeToString")
    @Mapping(target = "visibility", source = "visibility", qualifiedByName = "stringToRepoVisibility")
    RepositoryDTO toDto(RepositoryDocument doc);

    @Named("intToBigDecimal")
    static BigDecimal intToBigDecimal(int value) {
        return BigDecimal.valueOf(value);
    }

    @Named("dateTimeToString")
    static String dateTimeToString(LocalDateTime ldt) {
        return ldt != null ? ldt.toString() : null;
    }

    @Named("stringToRepoVisibility")
    static RepoVisibility stringToRepoVisibility(String value) {
        if (value == null) return RepoVisibility.PUBLIC;
        return RepoVisibility.fromValue(value.toLowerCase());
    }
}
