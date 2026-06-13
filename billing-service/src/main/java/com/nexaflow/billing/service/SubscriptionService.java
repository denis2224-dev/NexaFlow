package com.nexaflow.billing.service;

import com.nexaflow.billing.domain.Subscription;
import com.nexaflow.billing.repository.SubscriptionRepository;
import com.nexaflow.billing.service.dto.SubscriptionDTO;
import com.nexaflow.billing.service.mapper.SubscriptionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.nexaflow.billing.domain.Subscription}.
 */
@Service
@Transactional
public class SubscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;

    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, SubscriptionMapper subscriptionMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionMapper = subscriptionMapper;
    }

    /**
     * Save a subscription.
     *
     * @param subscriptionDTO the entity to save.
     * @return the persisted entity.
     */
    public SubscriptionDTO save(SubscriptionDTO subscriptionDTO) {
        LOG.debug("Request to save Subscription : {}", subscriptionDTO);
        Subscription subscription = subscriptionMapper.toEntity(subscriptionDTO);
        subscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDto(subscription);
    }

    /**
     * Update a subscription.
     *
     * @param subscriptionDTO the entity to save.
     * @return the persisted entity.
     */
    public SubscriptionDTO update(SubscriptionDTO subscriptionDTO) {
        LOG.debug("Request to update Subscription : {}", subscriptionDTO);
        Subscription subscription = subscriptionMapper.toEntity(subscriptionDTO);
        subscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDto(subscription);
    }

    /**
     * Partially update a subscription.
     *
     * @param subscriptionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SubscriptionDTO> partialUpdate(SubscriptionDTO subscriptionDTO) {
        LOG.debug("Request to partially update Subscription : {}", subscriptionDTO);

        return subscriptionRepository
            .findById(subscriptionDTO.getId())
            .map(existingSubscription -> {
                subscriptionMapper.partialUpdate(existingSubscription, subscriptionDTO);

                return existingSubscription;
            })
            .map(subscriptionRepository::save)
            .map(subscriptionMapper::toDto);
    }

    /**
     * Get one subscription by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SubscriptionDTO> findOne(Long id) {
        LOG.debug("Request to get Subscription : {}", id);
        return subscriptionRepository.findById(id).map(subscriptionMapper::toDto);
    }

    /**
     * Delete the subscription by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Subscription : {}", id);
        subscriptionRepository.deleteById(id);
    }
}
