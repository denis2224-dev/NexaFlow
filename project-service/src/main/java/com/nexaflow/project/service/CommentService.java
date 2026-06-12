package com.nexaflow.project.service;

import com.nexaflow.project.domain.Comment;
import com.nexaflow.project.domain.Task;
import com.nexaflow.project.domain.enumeration.ActivityAction;
import com.nexaflow.project.domain.enumeration.ActivityEntityType;
import com.nexaflow.project.repository.CommentRepository;
import com.nexaflow.project.repository.TaskRepository;
import com.nexaflow.project.security.OrganizationAccessService;
import com.nexaflow.project.security.SecurityUtils;
import com.nexaflow.project.service.dto.CommentDTO;
import com.nexaflow.project.service.dto.CreateCommentRequest;
import com.nexaflow.project.service.dto.UpdateCommentRequest;
import com.nexaflow.project.service.mapper.CommentMapper;
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
 * Service Implementation for managing {@link com.nexaflow.project.domain.Comment}.
 */
@Service
@Transactional
public class CommentService {

    private static final Logger LOG = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final CommentMapper commentMapper;
    private final OrganizationAccessService organizationAccessService;
    private final ActivityLogService activityLogService;

    public CommentService(
        CommentRepository commentRepository,
        TaskRepository taskRepository,
        CommentMapper commentMapper,
        OrganizationAccessService organizationAccessService,
        ActivityLogService activityLogService
    ) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.commentMapper = commentMapper;
        this.organizationAccessService = organizationAccessService;
        this.activityLogService = activityLogService;
    }

    public CommentDTO addToTask(Long taskId, CreateCommentRequest request) {
        LOG.debug("Request to add Comment to Task {} : {}", taskId, request);
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new AccessDeniedException("Task not found"));
        organizationAccessService.assertMember(task.getOrganizationId());

        Comment comment = new Comment();
        comment.setOrganizationId(task.getOrganizationId());
        comment.setTask(task);
        comment.setContent(request.content());
        comment.setAuthorLogin(SecurityUtils.getCurrentUserLogin().orElse("system"));
        comment.setCreatedAt(Instant.now());

        comment = commentRepository.save(comment);
        activityLogService.record(comment.getOrganizationId(), ActivityEntityType.TASK, taskId, ActivityAction.COMMENT_ADDED);
        return commentMapper.toDto(comment);
    }

    public CommentDTO update(Long id, UpdateCommentRequest request) {
        LOG.debug("Request to update Comment : {}, {}", id, request);
        Comment comment = getExistingComment(id);
        organizationAccessService.assertMember(comment.getOrganizationId());
        comment.setContent(request.content());
        comment = commentRepository.save(comment);
        activityLogService.record(comment.getOrganizationId(), ActivityEntityType.COMMENT, comment.getId(), ActivityAction.COMMENT_UPDATED);
        return commentMapper.toDto(comment);
    }

    public CommentDTO save(CommentDTO commentDTO) {
        LOG.debug("Request to save Comment : {}", commentDTO);
        organizationAccessService.assertMember(commentDTO.getOrganizationId());

        Comment comment = commentMapper.toEntity(commentDTO);
        comment = commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    public CommentDTO update(CommentDTO commentDTO) {
        LOG.debug("Request to update Comment : {}", commentDTO);

        Comment existingComment = getExistingComment(commentDTO.getId());
        Long organizationId = existingComment.getOrganizationId();
        organizationAccessService.assertMember(organizationId);
        commentDTO.setOrganizationId(organizationId);

        Comment comment = commentMapper.toEntity(commentDTO);
        comment = commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    public Optional<CommentDTO> partialUpdate(CommentDTO commentDTO) {
        LOG.debug("Request to partially update Comment : {}", commentDTO);

        return commentRepository
            .findById(commentDTO.getId())
            .map(existingComment -> {
                Long organizationId = existingComment.getOrganizationId();
                organizationAccessService.assertMember(organizationId);

                commentDTO.setOrganizationId(organizationId);
                commentMapper.partialUpdate(existingComment, commentDTO);
                existingComment.setOrganizationId(organizationId);

                return existingComment;
            })
            .map(commentRepository::save)
            .map(commentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CommentDTO> findAllByOrganization(Long organizationId, Pageable pageable) {
        LOG.debug("Request to get Comments for organization : {}", organizationId);

        organizationAccessService.assertMember(organizationId);

        return commentRepository.findByOrganizationId(organizationId, pageable).map(commentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CommentDTO> findByTask(Long taskId, Pageable pageable) {
        LOG.debug("Request to get Comments for task : {}", taskId);
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new AccessDeniedException("Task not found"));
        organizationAccessService.assertMember(task.getOrganizationId());
        return commentRepository.findByTaskId(taskId, pageable).map(commentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CommentDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Comments");
        return commentRepository.findAll(pageable).map(commentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<CommentDTO> findOne(Long id) {
        LOG.debug("Request to get Comment : {}", id);
        return commentRepository
            .findById(id)
            .map(comment -> {
                organizationAccessService.assertMember(comment.getOrganizationId());
                return commentMapper.toDto(comment);
            });
    }

    public void delete(Long id) {
        LOG.debug("Request to delete Comment : {}", id);

        Comment existingComment = getExistingComment(id);
        organizationAccessService.assertMember(existingComment.getOrganizationId());

        commentRepository.deleteById(id);
    }

    private Comment getExistingComment(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new AccessDeniedException("Comment not found"));
    }
}
