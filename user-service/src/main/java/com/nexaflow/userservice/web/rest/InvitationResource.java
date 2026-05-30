package com.nexaflow.userservice.web.rest;

import com.nexaflow.userservice.repository.InvitationRepository;
import com.nexaflow.userservice.service.InvitationQueryService;
import com.nexaflow.userservice.service.InvitationService;
import com.nexaflow.userservice.service.criteria.InvitationCriteria;
import com.nexaflow.userservice.service.dto.InvitationDTO;
import com.nexaflow.userservice.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.nexaflow.userservice.domain.Invitation}.
 */
@RestController
@RequestMapping("/api/invitations")
public class InvitationResource {

    private static final Logger LOG = LoggerFactory.getLogger(InvitationResource.class);

    private static final String ENTITY_NAME = "userServiceInvitation";

    @Value("${jhipster.clientApp.name:userservice}")
    private String applicationName;

    private final InvitationService invitationService;

    private final InvitationRepository invitationRepository;

    private final InvitationQueryService invitationQueryService;

    public InvitationResource(
        InvitationService invitationService,
        InvitationRepository invitationRepository,
        InvitationQueryService invitationQueryService
    ) {
        this.invitationService = invitationService;
        this.invitationRepository = invitationRepository;
        this.invitationQueryService = invitationQueryService;
    }

    /**
     * {@code POST  /invitations} : Create a new invitation.
     *
     * @param invitationDTO the invitationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new invitationDTO, or with status {@code 400 (Bad Request)} if the invitation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<InvitationDTO> createInvitation(@Valid @RequestBody InvitationDTO invitationDTO) throws URISyntaxException {
        LOG.debug("REST request to save Invitation : {}", invitationDTO);
        if (invitationDTO.getId() != null) {
            throw new BadRequestAlertException("A new invitation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        invitationDTO = invitationService.save(invitationDTO);
        return ResponseEntity.created(new URI("/api/invitations/" + invitationDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, invitationDTO.getId().toString()))
            .body(invitationDTO);
    }

    /**
     * {@code PUT  /invitations/:id} : Updates an existing invitation.
     *
     * @param id the id of the invitationDTO to save.
     * @param invitationDTO the invitationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated invitationDTO,
     * or with status {@code 400 (Bad Request)} if the invitationDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the invitationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<InvitationDTO> updateInvitation(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody InvitationDTO invitationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Invitation : {}, {}", id, invitationDTO);
        if (invitationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, invitationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!invitationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        invitationDTO = invitationService.update(invitationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, invitationDTO.getId().toString()))
            .body(invitationDTO);
    }

    /**
     * {@code PATCH  /invitations/:id} : Partial updates given fields of an existing invitation, field will ignore if it is null
     *
     * @param id the id of the invitationDTO to save.
     * @param invitationDTO the invitationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated invitationDTO,
     * or with status {@code 400 (Bad Request)} if the invitationDTO is not valid,
     * or with status {@code 404 (Not Found)} if the invitationDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the invitationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<InvitationDTO> partialUpdateInvitation(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody InvitationDTO invitationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Invitation partially : {}, {}", id, invitationDTO);
        if (invitationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, invitationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!invitationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<InvitationDTO> result = invitationService.partialUpdate(invitationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, invitationDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /invitations} : get all the Invitations.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Invitations in body.
     */
    @GetMapping("")
    public ResponseEntity<List<InvitationDTO>> getAllInvitations(
        InvitationCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Invitations by criteria: {}", criteria);

        Page<InvitationDTO> page = invitationQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /invitations/count} : count all the invitations.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countInvitations(InvitationCriteria criteria) {
        LOG.debug("REST request to count Invitations by criteria: {}", criteria);
        return ResponseEntity.ok().body(invitationQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /invitations/:id} : get the "id" invitation.
     *
     * @param id the id of the invitationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the invitationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InvitationDTO> getInvitation(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Invitation : {}", id);
        Optional<InvitationDTO> invitationDTO = invitationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(invitationDTO);
    }

    /**
     * {@code DELETE  /invitations/:id} : delete the "id" invitation.
     *
     * @param id the id of the invitationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvitation(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Invitation : {}", id);
        invitationService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
