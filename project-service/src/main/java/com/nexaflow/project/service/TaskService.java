package com.nexaflow.project.service;

import com.nexaflow.project.client.dto.BillingFeature;
import com.nexaflow.project.domain.Project;
import com.nexaflow.project.domain.Task;
import com.nexaflow.project.domain.enumeration.ActivityAction;
import com.nexaflow.project.domain.enumeration.ActivityEntityType;
import com.nexaflow.project.domain.enumeration.ProjectStatus;
import com.nexaflow.project.domain.enumeration.TaskStatus;
import com.nexaflow.project.repository.CommentRepository;
import com.nexaflow.project.repository.ProjectRepository;
import com.nexaflow.project.repository.TaskRepository;
import com.nexaflow.project.security.OrganizationAccessService;
import com.nexaflow.project.security.SecurityUtils;
import com.nexaflow.project.service.dto.AssignTaskRequest;
import com.nexaflow.project.service.dto.ChangeTaskStatusRequest;
import com.nexaflow.project.service.dto.CreateTaskRequest;
import com.nexaflow.project.service.dto.TaskDTO;
import com.nexaflow.project.service.dto.UpdateTaskRequest;
import com.nexaflow.project.service.mapper.TaskMapper;
import com.nexaflow.project.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
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
    private static final String ENTITY_NAME = "task";

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final CommentRepository commentRepository;
    private final TaskMapper taskMapper;
    private final OrganizationAccessService organizationAccessService;
    private final BillingAccessService billingAccessService;
    private final ActivityLogService activityLogService;

    public TaskService(
        TaskRepository taskRepository,
        ProjectRepository projectRepository,
        CommentRepository commentRepository,
        TaskMapper taskMapper,
        OrganizationAccessService organizationAccessService,
        BillingAccessService billingAccessService,
        ActivityLogService activityLogService
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.commentRepository = commentRepository;
        this.taskMapper = taskMapper;
        this.organizationAccessService = organizationAccessService;
        this.billingAccessService = billingAccessService;
        this.activityLogService = activityLogService;
    }

    public TaskDTO create(CreateTaskRequest request) {
        LOG.debug("Request to create Task : {}", request);
        Project project = projectRepository.findById(request.projectId()).orElseThrow(() -> new AccessDeniedException("Project not found"));
        organizationAccessService.assertMember(project.getOrganizationId());
        billingAccessService.assertAllowed(project.getOrganizationId(), BillingFeature.TASKS, 1);
        assertProjectNotArchived(project);
        organizationAccessService.assertUserIsMember(project.getOrganizationId(), request.assignedUserLogin());

        Task task = new Task();
        task.setOrganizationId(project.getOrganizationId());
        task.setProject(project);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setStatus(request.status());
        task.setAssignedUserLogin(request.assignedUserLogin());
        task.setDueDate(request.dueDate());
        task.setCreatedBy(SecurityUtils.getCurrentUserLogin().orElse("system"));
        task.setCreatedAt(Instant.now());

        task = taskRepository.save(task);
        activityLogService.record(task.getOrganizationId(), ActivityEntityType.TASK, task.getId(), ActivityAction.TASK_CREATED);
        return taskMapper.toDto(task);
    }

    public TaskDTO save(TaskDTO taskDTO) {
        LOG.debug("Request to save Task : {}", taskDTO);

        organizationAccessService.assertMember(taskDTO.getOrganizationId());

        Task task = taskMapper.toEntity(taskDTO);
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    public TaskDTO update(Long id, UpdateTaskRequest request) {
        LOG.debug("Request to update Task : {}, {}", id, request);
        Task task = getExistingTask(id);
        organizationAccessService.assertMember(task.getOrganizationId());
        assertTaskProjectNotArchived(task);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setUpdatedAt(Instant.now());

        task = taskRepository.save(task);
        activityLogService.record(task.getOrganizationId(), ActivityEntityType.TASK, task.getId(), ActivityAction.TASK_UPDATED);
        return taskMapper.toDto(task);
    }

    public TaskDTO update(TaskDTO taskDTO) {
        LOG.debug("Request to update Task : {}", taskDTO);

        Task existingTask = getExistingTask(taskDTO.getId());
        Long organizationId = existingTask.getOrganizationId();
        organizationAccessService.assertMember(organizationId);
        assertTaskProjectNotArchived(existingTask);

        taskDTO.setOrganizationId(organizationId);

        Task task = taskMapper.toEntity(taskDTO);
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    public Optional<TaskDTO> partialUpdate(TaskDTO taskDTO) {
        LOG.debug("Request to partially update Task : {}", taskDTO);

        return taskRepository
            .findById(taskDTO.getId())
            .map(existingTask -> {
                Long organizationId = existingTask.getOrganizationId();
                organizationAccessService.assertMember(organizationId);
                assertTaskProjectNotArchived(existingTask);

                taskDTO.setOrganizationId(organizationId);
                taskMapper.partialUpdate(existingTask, taskDTO);
                existingTask.setOrganizationId(organizationId);

                return existingTask;
            })
            .map(taskRepository::save)
            .map(taskMapper::toDto);
    }

    public TaskDTO changeStatus(Long id, ChangeTaskStatusRequest request) {
        Task task = getExistingTask(id);
        organizationAccessService.assertMember(task.getOrganizationId());
        assertTaskProjectNotArchived(task);
        task.setStatus(request.status());
        task.setUpdatedAt(Instant.now());
        task = taskRepository.save(task);
        activityLogService.record(task.getOrganizationId(), ActivityEntityType.TASK, task.getId(), ActivityAction.TASK_STATUS_CHANGED);
        return taskMapper.toDto(task);
    }

    public TaskDTO assign(Long id, AssignTaskRequest request) {
        Task task = getExistingTask(id);
        organizationAccessService.assertMember(task.getOrganizationId());
        assertTaskProjectNotArchived(task);
        organizationAccessService.assertUserIsMember(task.getOrganizationId(), request.assignedUserLogin());
        task.setAssignedUserLogin(request.assignedUserLogin());
        task.setUpdatedAt(Instant.now());
        task = taskRepository.save(task);
        activityLogService.record(task.getOrganizationId(), ActivityEntityType.TASK, task.getId(), ActivityAction.TASK_ASSIGNED);
        return taskMapper.toDto(task);
    }

    public TaskDTO unassign(Long id) {
        Task task = getExistingTask(id);
        organizationAccessService.assertMember(task.getOrganizationId());
        assertTaskProjectNotArchived(task);
        task.setAssignedUserLogin(null);
        task.setUpdatedAt(Instant.now());
        task = taskRepository.save(task);
        activityLogService.record(task.getOrganizationId(), ActivityEntityType.TASK, task.getId(), ActivityAction.TASK_UNASSIGNED);
        return taskMapper.toDto(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> findAllByOrganization(Long organizationId, Pageable pageable) {
        LOG.debug("Request to get Tasks for organization : {}", organizationId);

        organizationAccessService.assertMember(organizationId);

        return taskRepository.findByOrganizationId(organizationId, pageable).map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> findByProject(Long projectId, Pageable pageable) {
        LOG.debug("Request to get Tasks for project : {}", projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new AccessDeniedException("Project not found"));
        organizationAccessService.assertMember(project.getOrganizationId());
        return taskRepository.findByProjectId(projectId, pageable).map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> findMyTasks(Long organizationId, Pageable pageable) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccessDeniedException("User is not authenticated"));
        organizationAccessService.assertMember(organizationId);
        return taskRepository.findByOrganizationIdAndAssignedUserLogin(organizationId, login, pageable).map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> findDueToday(Long organizationId, Pageable pageable) {
        organizationAccessService.assertMember(organizationId);
        return taskRepository.findByOrganizationIdAndDueDate(organizationId, java.time.LocalDate.now(), pageable).map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> findOverdue(Long organizationId, Pageable pageable) {
        organizationAccessService.assertMember(organizationId);
        return taskRepository
            .findByOrganizationIdAndDueDateBeforeAndStatusNot(organizationId, java.time.LocalDate.now(), TaskStatus.DONE, pageable)
            .map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Tasks");
        return taskRepository.findAll(pageable).map(taskMapper::toDto);
    }

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

    public void delete(Long id) {
        LOG.debug("Request to delete Task : {}", id);

        Task existingTask = getExistingTask(id);
        organizationAccessService.assertMember(existingTask.getOrganizationId());
        assertTaskProjectNotArchived(existingTask);

        commentRepository.deleteByTaskId(id);
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Task getExistingTask(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new AccessDeniedException("Task not found"));
    }

    private void assertTaskProjectNotArchived(Task task) {
        assertProjectNotArchived(task.getProject());
    }

    private void assertProjectNotArchived(Project project) {
        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new BadRequestAlertException("Archived projects cannot be modified", ENTITY_NAME, "projectarchived");
        }
    }
}
