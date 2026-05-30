package com.nexaflow.userservice.service;

import com.nexaflow.userservice.domain.Invitation;
import com.nexaflow.userservice.repository.InvitationRepository;
import com.nexaflow.userservice.service.dto.InvitationDTO;
import com.nexaflow.userservice.service.mapper.InvitationMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.nexaflow.userservice.domain.Invitation}.
 */
@Service
@Transactional
public class InvitationService {

    private static final Logger LOG = LoggerFactory.getLogger(InvitationService.class);

    private final InvitationRepository invitationRepository;

    private final InvitationMapper invitationMapper;

    public InvitationService(InvitationRepository invitationRepository, InvitationMapper invitationMapper) {
        this.invitationRepository = invitationRepository;
        this.invitationMapper = invitationMapper;
    }

    /**
     * Save a invitation.
     *
     * @param invitationDTO the entity to save.
     * @return the persisted entity.
     */
    public InvitationDTO save(InvitationDTO invitationDTO) {
        LOG.debug("Request to save Invitation : {}", invitationDTO);
        Invitation invitation = invitationMapper.toEntity(invitationDTO);
        invitation = invitationRepository.save(invitation);
        return invitationMapper.toDto(invitation);
    }

    /**
     * Update a invitation.
     *
     * @param invitationDTO the entity to save.
     * @return the persisted entity.
     */
    public InvitationDTO update(InvitationDTO invitationDTO) {
        LOG.debug("Request to update Invitation : {}", invitationDTO);
        Invitation invitation = invitationMapper.toEntity(invitationDTO);
        invitation = invitationRepository.save(invitation);
        return invitationMapper.toDto(invitation);
    }

    /**
     * Partially update a invitation.
     *
     * @param invitationDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<InvitationDTO> partialUpdate(InvitationDTO invitationDTO) {
        LOG.debug("Request to partially update Invitation : {}", invitationDTO);

        return invitationRepository
            .findById(invitationDTO.getId())
            .map(existingInvitation -> {
                invitationMapper.partialUpdate(existingInvitation, invitationDTO);

                return existingInvitation;
            })
            .map(invitationRepository::save)
            .map(invitationMapper::toDto);
    }

    /**
     * Get all the invitations with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<InvitationDTO> findAllWithEagerRelationships(Pageable pageable) {
        return invitationRepository.findAllWithEagerRelationships(pageable).map(invitationMapper::toDto);
    }

    /**
     * Get one invitation by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<InvitationDTO> findOne(Long id) {
        LOG.debug("Request to get Invitation : {}", id);
        return invitationRepository.findOneWithEagerRelationships(id).map(invitationMapper::toDto);
    }

    /**
     * Delete the invitation by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Invitation : {}", id);
        invitationRepository.deleteById(id);
    }
}
