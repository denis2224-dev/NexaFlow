package com.nexaflow.project.service;

import com.nexaflow.project.domain.Task;
import com.nexaflow.project.repository.TaskRepository;
import com.nexaflow.project.security.OrganizationAccessService;
import com.nexaflow.project.service.dto.TaskDTO;
import com.nexaflow.project.service.mapper.TaskMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.nexaflow.project.domain.Task}.
 */
@Service
@Transactional
public class TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    private final OrganizationAccessService organizationAccessService;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper, OrganizationAccessService organizationAccessService) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.organizationAccessService = organizationAccessService;
    }

    /**
     * Save a task.
     *
     * @param taskDTO the entity to save.
     * @return the persisted entity.
     */
    public TaskDTO save(TaskDTO taskDTO) {
        LOG.debug("Request to save Task : {}", taskDTO);

        organizationAccessService.assertMember(taskDTO.getOrganizationId());

        Task task = taskMapper.toEntity(taskDTO);
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    /**
     * Update a task.
     *
     * @param taskDTO the entity to save.
     * @return the persisted entity.
     */
    public TaskDTO update(TaskDTO taskDTO) {
        LOG.debug("Request to update Task : {}", taskDTO);

        Task existingTask = taskRepository
            .findById(taskDTO.getId())
            .orElseThrow(() -> new AccessDeniedException("Task not found"));

        Long organizationId = existingTask.getOrganizationId();
        organizationAccessService.assertMember(organizationId);

        taskDTO.setOrganizationId(organizationId);

        Task task = taskMapper.toEntity(taskDTO);
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    /**
     * Partially update a task.
     *
     * @param taskDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<TaskDTO> partialUpdate(TaskDTO taskDTO) {
        LOG.debug("Request to partially update Task : {}", taskDTO);

        return taskRepository
            .findById(taskDTO.getId())
            .map(existingTask -> {
                Long organizationId = existingTask.getOrganizationId();
                organizationAccessService.assertMember(organizationId);

                taskDTO.setOrganizationId(organizationId);
                taskMapper.partialUpdate(existingTask, taskDTO);
                existingTask.setOrganizationId(organizationId);

                return existingTask;
            })
            .map(taskRepository::save)
            .map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> findAllByOrganization(Long organizationId, Pageable pageable) {
        LOG.debug("Request to get Tasks for organization : {}", organizationId);

        organizationAccessService.assertMember(organizationId);

        return taskRepository.findByOrganizationId(organizationId, pageable).map(taskMapper::toDto);
    }

    /**
     * Get all the tasks.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<TaskDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Tasks");
        return taskRepository.findAll(pageable).map(taskMapper::toDto);
    }

    /**
     * Get one task by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<TaskDTO> findOne(Long id) {
        LOG.debug("Request to get Task : {}", id);
        return taskRepository
            .findById(id)
            .map(task -> {
                organizationAccessService.assertMember(task.getOrganizationId());
                return taskMapper.toDto(task);
            });
    }

    /**
     * Delete the task by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Task : {}", id);

        Task existingTask = taskRepository
            .findById(id)
            .orElseThrow(() -> new AccessDeniedException("Task not found"));

        organizationAccessService.assertMember(existingTask.getOrganizationId());

        taskRepository.deleteById(id);
    }
}
