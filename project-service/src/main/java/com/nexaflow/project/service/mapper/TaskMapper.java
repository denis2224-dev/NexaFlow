package com.nexaflow.project.service.mapper;

import com.nexaflow.project.domain.Project;
import com.nexaflow.project.domain.Task;
import com.nexaflow.project.service.dto.ProjectDTO;
import com.nexaflow.project.service.dto.TaskDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Task} and its DTO {@link TaskDTO}.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper extends EntityMapper<TaskDTO, Task> {
    @Mapping(target = "project", source = "project", qualifiedByName = "projectId")
    TaskDTO toDto(Task s);

    @Named("projectId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ProjectDTO toDtoProjectId(Project project);
}
