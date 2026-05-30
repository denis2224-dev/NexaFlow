package com.nexaflow.userservice.service;

import com.nexaflow.userservice.domain.Membership;
import com.nexaflow.userservice.repository.MembershipRepository;
import com.nexaflow.userservice.service.dto.MembershipDTO;
import com.nexaflow.userservice.service.mapper.MembershipMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.nexaflow.userservice.domain.Membership}.
 */
@Service
@Transactional
public class MembershipService {

    private static final Logger LOG = LoggerFactory.getLogger(MembershipService.class);

    private final MembershipRepository membershipRepository;

    private final MembershipMapper membershipMapper;

    public MembershipService(MembershipRepository membershipRepository, MembershipMapper membershipMapper) {
        this.membershipRepository = membershipRepository;
        this.membershipMapper = membershipMapper;
    }

    /**
     * Save a membership.
     *
     * @param membershipDTO the entity to save.
     * @return the persisted entity.
     */
    public MembershipDTO save(MembershipDTO membershipDTO) {
        LOG.debug("Request to save Membership : {}", membershipDTO);
        Membership membership = membershipMapper.toEntity(membershipDTO);
        membership = membershipRepository.save(membership);
        return membershipMapper.toDto(membership);
    }

    /**
     * Update a membership.
     *
     * @param membershipDTO the entity to save.
     * @return the persisted entity.
     */
    public MembershipDTO update(MembershipDTO membershipDTO) {
        LOG.debug("Request to update Membership : {}", membershipDTO);
        Membership membership = membershipMapper.toEntity(membershipDTO);
        membership = membershipRepository.save(membership);
        return membershipMapper.toDto(membership);
    }

    /**
     * Partially update a membership.
     *
     * @param membershipDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MembershipDTO> partialUpdate(MembershipDTO membershipDTO) {
        LOG.debug("Request to partially update Membership : {}", membershipDTO);

        return membershipRepository
            .findById(membershipDTO.getId())
            .map(existingMembership -> {
                membershipMapper.partialUpdate(existingMembership, membershipDTO);

                return existingMembership;
            })
            .map(membershipRepository::save)
            .map(membershipMapper::toDto);
    }

    /**
     * Get all the memberships with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<MembershipDTO> findAllWithEagerRelationships(Pageable pageable) {
        return membershipRepository.findAllWithEagerRelationships(pageable).map(membershipMapper::toDto);
    }

    /**
     * Get one membership by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MembershipDTO> findOne(Long id) {
        LOG.debug("Request to get Membership : {}", id);
        return membershipRepository.findOneWithEagerRelationships(id).map(membershipMapper::toDto);
    }

    /**
     * Delete the membership by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Membership : {}", id);
        membershipRepository.deleteById(id);
    }
}
