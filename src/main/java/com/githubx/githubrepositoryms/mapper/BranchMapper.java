package com.githubx.githubrepositoryms.mapper;

import com.githubx.githubrepositoryms.model.BranchDocument;
import com.smithy.g.repo.server.branch.model.BranchDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BranchMapper {

    BranchDTO toDto(BranchDocument doc);
}
