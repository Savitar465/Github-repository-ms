package com.githubx.githubrepositoryms.mapper;

import com.githubx.githubrepositoryms.model.FileEntryDocument;
import com.smithy.g.repo.server.content.model.FileEntryDTO;
import com.smithy.g.repo.server.content.model.FileType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface FileEntryMapper {

    @Mapping(target = "type", source = "type", qualifiedByName = "stringToFileType")
    @Mapping(target = "size", source = "size", qualifiedByName = "longToBigDecimal")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "dateTimeToString")
    FileEntryDTO toDto(FileEntryDocument doc);

    @Named("stringToFileType")
    static FileType stringToFileType(String value) {
        return value != null ? FileType.fromValue(value) : FileType.FILE;
    }

    @Named("longToBigDecimal")
    static BigDecimal longToBigDecimal(long value) {
        return BigDecimal.valueOf(value);
    }

    @Named("dateTimeToString")
    static String dateTimeToString(LocalDateTime ldt) {
        return ldt != null ? ldt.toString() : null;
    }
}
