package com.nexaflow.project.web.rest;

import com.nexaflow.project.service.CommentService;
import com.nexaflow.project.service.dto.CommentDTO;
import com.nexaflow.project.service.dto.UpdateCommentRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/comments")
public class CommentResource {

    private static final Logger LOG = LoggerFactory.getLogger(CommentResource.class);
    private static final String ORGANIZATION_HEADER = "X-Organization-Id";

    private final CommentService commentService;

    public CommentResource(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("")
    public ResponseEntity<List<CommentDTO>> getAllComments(
        @RequestHeader(ORGANIZATION_HEADER) Long organizationId,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Comments for organization : {}", organizationId);
        Page<CommentDTO> page = commentService.findAllByOrganization(organizationId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentDTO> getComment(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Comment : {}", id);
        return ResponseUtil.wrapOrNotFound(commentService.findOne(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable("id") Long id, @Valid @RequestBody UpdateCommentRequest request) {
        LOG.debug("REST request to update Comment : {}, {}", id, request);
        return ResponseEntity.ok(commentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Comment : {}", id);
        commentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
