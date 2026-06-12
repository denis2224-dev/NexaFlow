package com.nexaflow.project.service;

import com.nexaflow.project.domain.Comment;
import com.nexaflow.project.repository.CommentRepository;
import com.nexaflow.project.security.OrganizationAccessService;
import com.nexaflow.project.service.dto.CommentDTO;
import com.nexaflow.project.service.mapper.CommentMapper;
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

    private final CommentMapper commentMapper;

    private final OrganizationAccessService organizationAccessService;

    public CommentService(
        CommentRepository commentRepository,
        CommentMapper commentMapper,
        OrganizationAccessService organizationAccessService
    ) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.organizationAccessService = organizationAccessService;
    }

    /**
     * Save a comment.
     *
     * @param commentDTO the entity to save.
     * @return the persisted entity.
     */
    public CommentDTO save(CommentDTO commentDTO) {
        LOG.debug("Request to save Comment : {}", commentDTO);
        organizationAccessService.assertMember(commentDTO.getOrganizationId());

        Comment comment = commentMapper.toEntity(commentDTO);
        comment = commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    /**
     * Update a comment.
     *
     * @param commentDTO the entity to save.
     * @return the persisted entity.
     */
    public CommentDTO update(CommentDTO commentDTO) {
        LOG.debug("Request to update Comment : {}", commentDTO);

        Comment existingComment = commentRepository
            .findById(commentDTO.getId())
            .orElseThrow(() -> new AccessDeniedException("Comment not found"));

        Long organizationId = existingComment.getOrganizationId();
        organizationAccessService.assertMember(organizationId);
        commentDTO.setOrganizationId(organizationId);

        Comment comment = commentMapper.toEntity(commentDTO);
        comment = commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    /**
     * Partially update a comment.
     *
     * @param commentDTO the entity to update partially.
     * @return the persisted entity.
     */
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

    /**
     * Get all the comments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<CommentDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Comments");
        return commentRepository.findAll(pageable).map(commentMapper::toDto);
    }

    /**
     * Get one comment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
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

    /**
     * Delete the comment by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Comment : {}", id);

        Comment existingComment = commentRepository
            .findById(id)
            .orElseThrow(() -> new AccessDeniedException("Comment not found"));

        organizationAccessService.assertMember(existingComment.getOrganizationId());

        commentRepository.deleteById(id);
    }
}
