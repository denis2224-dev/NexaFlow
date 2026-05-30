package com.nexaflow.userservice.web.rest;

import com.nexaflow.userservice.repository.OrganizationRepository;
import com.nexaflow.userservice.service.OrganizationQueryService;
import com.nexaflow.userservice.service.OrganizationService;
import com.nexaflow.userservice.service.criteria.OrganizationCriteria;
import com.nexaflow.userservice.service.dto.OrganizationDTO;
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
 * REST controller for managing {@link com.nexaflow.userservice.domain.Organization}.
 */
@RestController
@RequestMapping("/api/organizations")
public class OrganizationResource {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationResource.class);

    private static final String ENTITY_NAME = "userServiceOrganization";

    @Value("${jhipster.clientApp.name:userservice}")
    private String applicationName;

    private final OrganizationService organizationService;

    private final OrganizationRepository organizationRepository;

    private final OrganizationQueryService organizationQueryService;

    public OrganizationResource(
        OrganizationService organizationService,
        OrganizationRepository organizationRepository,
        OrganizationQueryService organizationQueryService
    ) {
        this.organizationService = organizationService;
        this.organizationRepository = organizationRepository;
        this.organizationQueryService = organizationQueryService;
    }

    /**
     * {@code POST  /organizations} : Create a new organization.
     *
     * @param organizationDTO the organizationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new organizationDTO, or with status {@code 400 (Bad Request)} if the organization has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<OrganizationDTO> createOrganization(@Valid @RequestBody OrganizationDTO organizationDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save Organization : {}", organizationDTO);
        if (organizationDTO.getId() != null) {
            throw new BadRequestAlertException("A new organization cannot already have an ID", ENTITY_NAME, "idexists");
        }
        organizationDTO = organizationService.save(organizationDTO);
        return ResponseEntity.created(new URI("/api/organizations/" + organizationDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, organizationDTO.getId().toString()))
            .body(organizationDTO);
    }

    /**
     * {@code PUT  /organizations/:id} : Updates an existing organization.
     *
     * @param id the id of the organizationDTO to save.
     * @param organizationDTO the organizationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated organizationDTO,
     * or with status {@code 400 (Bad Request)} if the organizationDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the organizationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationDTO> updateOrganization(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody OrganizationDTO organizationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Organization : {}, {}", id, organizationDTO);
        if (organizationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, organizationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!organizationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        organizationDTO = organizationService.update(organizationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, organizationDTO.getId().toString()))
            .body(organizationDTO);
    }

    /**
     * {@code PATCH  /organizations/:id} : Partial updates given fields of an existing organization, field will ignore if it is null
     *
     * @param id the id of the organizationDTO to save.
     * @param organizationDTO the organizationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated organizationDTO,
     * or with status {@code 400 (Bad Request)} if the organizationDTO is not valid,
     * or with status {@code 404 (Not Found)} if the organizationDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the organizationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<OrganizationDTO> partialUpdateOrganization(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody OrganizationDTO organizationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Organization partially : {}, {}", id, organizationDTO);
        if (organizationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, organizationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!organizationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<OrganizationDTO> result = organizationService.partialUpdate(organizationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, organizationDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /organizations} : get all the Organizations.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Organizations in body.
     */
    @GetMapping("")
    public ResponseEntity<List<OrganizationDTO>> getAllOrganizations(
        OrganizationCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Organizations by criteria: {}", criteria);

        Page<OrganizationDTO> page = organizationQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /organizations/count} : count all the organizations.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countOrganizations(OrganizationCriteria criteria) {
        LOG.debug("REST request to count Organizations by criteria: {}", criteria);
        return ResponseEntity.ok().body(organizationQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /organizations/:id} : get the "id" organization.
     *
     * @param id the id of the organizationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the organizationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationDTO> getOrganization(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Organization : {}", id);
        Optional<OrganizationDTO> organizationDTO = organizationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(organizationDTO);
    }

    /**
     * {@code DELETE  /organizations/:id} : delete the "id" organization.
     *
     * @param id the id of the organizationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Organization : {}", id);
        organizationService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
