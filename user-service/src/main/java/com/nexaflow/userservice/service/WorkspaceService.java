package com.nexaflow.userservice.service;

import com.nexaflow.userservice.domain.Invitation;
import com.nexaflow.userservice.domain.Membership;
import com.nexaflow.userservice.domain.Organization;
import com.nexaflow.userservice.domain.enumeration.InvitationStatus;
import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import com.nexaflow.userservice.repository.InvitationRepository;
import com.nexaflow.userservice.repository.MembershipRepository;
import com.nexaflow.userservice.repository.OrganizationRepository;
import com.nexaflow.userservice.security.SecurityUtils;
import com.nexaflow.userservice.service.dto.AcceptInvitationRequest;
import com.nexaflow.userservice.service.dto.CreateWorkspaceRequest;
import com.nexaflow.userservice.service.dto.InvitationResponseDTO;
import com.nexaflow.userservice.service.dto.InviteUserRequest;
import com.nexaflow.userservice.service.dto.MemberDTO;
import com.nexaflow.userservice.service.dto.WorkspaceDTO;
import com.nexaflow.userservice.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkspaceService {

    private static final String ENTITY_NAME = "workspace";

    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final InvitationRepository invitationRepository;

    public WorkspaceService(
        OrganizationRepository organizationRepository,
        MembershipRepository membershipRepository,
        InvitationRepository invitationRepository
    ) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.invitationRepository = invitationRepository;
    }

    public WorkspaceDTO createWorkspace(CreateWorkspaceRequest request) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated")
        );

        String normalizedSlug = request.getSlug().trim().toLowerCase();

        if (organizationRepository.existsBySlug(normalizedSlug)) {
            throw new BadRequestAlertException("A workspace with this slug already exists", ENTITY_NAME, "slugexists");
        }

        Organization organization = new Organization();
        organization.setName(request.getName().trim());
        organization.setSlug(normalizedSlug);
        organization.setDescription(request.getDescription());
        organization.setCreatedAt(Instant.now());
        organization.setActive(true);

        Organization savedOrganization = organizationRepository.save(organization);

        Membership ownerMembership = new Membership();
        ownerMembership.setOrganization(savedOrganization);

        // Temporary until connecting full user details from the gateway.
        ownerMembership.setUserId(0L);
        ownerMembership.setUserLogin(currentUserLogin);
        ownerMembership.setUserEmail(currentUserLogin + "@local");

        ownerMembership.setRole(MembershipRole.OWNER);
        ownerMembership.setJoinedAt(Instant.now());
        ownerMembership.setActive(true);

        membershipRepository.save(ownerMembership);

        return new WorkspaceDTO(
            savedOrganization.getId(),
            savedOrganization.getName(),
            savedOrganization.getSlug(),
            savedOrganization.getDescription(),
            MembershipRole.OWNER
        );
    }

    @Transactional(readOnly = true)
    public List<WorkspaceDTO> findMyWorkspaces() {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated")
        );

        return membershipRepository
            .findByUserLoginAndActiveTrue(currentUserLogin)
            .stream()
            .filter(membership -> Boolean.TRUE.equals(membership.getOrganization().getActive()))
            .map(this::toWorkspaceDTO)
            .toList();
    }

    public InvitationResponseDTO inviteUser(Long organizationId, InviteUserRequest request) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated")
        );

        Membership currentMembership = membershipRepository
            .findOneByOrganizationIdAndUserLoginAndActiveTrue(organizationId, currentUserLogin)
            .orElseThrow(() -> new BadRequestAlertException("You are not a member of this workspace", ENTITY_NAME, "notmember"));

        if (currentMembership.getRole() != MembershipRole.OWNER && currentMembership.getRole() != MembershipRole.ADMIN) {
            throw new BadRequestAlertException("You do not have permission to invite users", ENTITY_NAME, "nopermission");
        }

        if (request.getRole() == MembershipRole.OWNER) {
            throw new BadRequestAlertException("You cannot invite another owner", ENTITY_NAME, "ownernotallowed");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();

        boolean alreadyMember = membershipRepository.existsByOrganizationIdAndUserEmailAndActiveTrue(organizationId, normalizedEmail);

        if (alreadyMember) {
            throw new BadRequestAlertException("This user is already a member", ENTITY_NAME, "alreadymember");
        }

        boolean alreadyInvited = invitationRepository.existsByOrganizationIdAndEmailAndStatus(
            organizationId,
            normalizedEmail,
            InvitationStatus.PENDING
        );

        if (alreadyInvited) {
            throw new BadRequestAlertException("This user already has a pending invitation", ENTITY_NAME, "alreadyinvited");
        }

        Organization organization = organizationRepository
            .findById(organizationId)
            .orElseThrow(() -> new BadRequestAlertException("Workspace not found", ENTITY_NAME, "notfound"));

        Invitation invitation = new Invitation();
        invitation.setOrganization(organization);
        invitation.setEmail(normalizedEmail);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setRole(request.getRole());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setInvitedAt(Instant.now());
        invitation.setExpiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60));

        // Temporary until real user ID is propagated.
        invitation.setInvitedByUserId(0L);
        invitation.setInvitedByLogin(currentUserLogin);

        Invitation savedInvitation = invitationRepository.save(invitation);

        return new InvitationResponseDTO(
            savedInvitation.getId(),
            organization.getId(),
            savedInvitation.getEmail(),
            savedInvitation.getToken(),
            savedInvitation.getRole(),
            savedInvitation.getStatus(),
            savedInvitation.getExpiresAt()
        );
    }

    public WorkspaceDTO acceptInvitation(AcceptInvitationRequest request) {
        String currentUserLogin = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated"));

        Invitation invitation = invitationRepository
            .findOneByToken(request.getToken())
            .orElseThrow(() -> new BadRequestAlertException("Invitation not found", ENTITY_NAME, "invitationnotfound"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestAlertException("Invitation is not pending", ENTITY_NAME, "invitationnotpending");
        }

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);

            throw new BadRequestAlertException("Invitation has expired", ENTITY_NAME, "invitationexpired");
        }

        Organization organization = invitation.getOrganization();

        boolean alreadyMember = membershipRepository
            .findOneByOrganizationIdAndUserLoginAndActiveTrue(organization.getId(), currentUserLogin)
            .isPresent();

        if (alreadyMember) {
            throw new BadRequestAlertException("You are already a member of this workspace", ENTITY_NAME, "alreadymember");
        }

        Membership membership = new Membership();
        membership.setOrganization(organization);

        // Temporary until real user ID/email are propagated from the gateway.
        membership.setUserId(0L);
        membership.setUserLogin(currentUserLogin);
        membership.setUserEmail(invitation.getEmail());

        membership.setRole(invitation.getRole());
        membership.setJoinedAt(Instant.now());
        membership.setActive(true);

        membershipRepository.save(membership);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(Instant.now());
        invitationRepository.save(invitation);

        return new WorkspaceDTO(
            organization.getId(),
            organization.getName(),
            organization.getSlug(),
            organization.getDescription(),
            membership.getRole()
        );
    }

    @Transactional(readOnly = true)
    public List<MemberDTO> getWorkspaceMembers(Long organizationId) {
        String currentUserLogin = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated"));

        membershipRepository
            .findOneByOrganizationIdAndUserLoginAndActiveTrue(organizationId, currentUserLogin)
            .orElseThrow(() -> new BadRequestAlertException("You are not a member of this workspace", ENTITY_NAME, "notmember"));

        return membershipRepository
            .findByOrganizationIdAndActiveTrue(organizationId)
            .stream()
            .map(membership ->
                new MemberDTO(
                    membership.getId(),
                    membership.getUserId(),
                    membership.getUserLogin(),
                    membership.getUserEmail(),
                    membership.getRole(),
                    membership.getActive()
                )
            )
            .toList();
    }

    @Transactional(readOnly = true)
    public List<InvitationResponseDTO> getWorkspaceInvitations(Long organizationId) {
        String currentUserLogin = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated"));

        Membership currentMembership = membershipRepository
            .findOneByOrganizationIdAndUserLoginAndActiveTrue(organizationId, currentUserLogin)
            .orElseThrow(() -> new BadRequestAlertException("You are not a member of this workspace", ENTITY_NAME, "notmember"));

        if (currentMembership.getRole() != MembershipRole.OWNER && currentMembership.getRole() != MembershipRole.ADMIN) {
            throw new BadRequestAlertException("You do not have permission to view invitations", ENTITY_NAME, "nopermission");
        }

        return invitationRepository
            .findByOrganizationIdAndStatus(organizationId, InvitationStatus.PENDING)
            .stream()
            .map(invitation ->
                new InvitationResponseDTO(
                    invitation.getId(),
                    invitation.getOrganization().getId(),
                    invitation.getEmail(),
                    invitation.getToken(),
                    invitation.getRole(),
                    invitation.getStatus(),
                    invitation.getExpiresAt()
                )
            )
            .toList();
    }

    public void revokeInvitation(Long organizationId, Long invitationId) {
        String currentUserLogin = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated"));

        Membership currentMembership = membershipRepository
            .findOneByOrganizationIdAndUserLoginAndActiveTrue(organizationId, currentUserLogin)
            .orElseThrow(() -> new BadRequestAlertException("You are not a member of this workspace", ENTITY_NAME, "notmember"));

        if (currentMembership.getRole() != MembershipRole.OWNER && currentMembership.getRole() != MembershipRole.ADMIN) {
            throw new BadRequestAlertException("You do not have permission to revoke invitations", ENTITY_NAME, "nopermission");
        }

        Invitation invitation = invitationRepository
            .findOneByIdAndOrganizationIdAndStatus(invitationId, organizationId, InvitationStatus.PENDING)
            .orElseThrow(() -> new BadRequestAlertException("Pending invitation not found", ENTITY_NAME, "invitationnotfound"));

        invitation.setStatus(InvitationStatus.REVOKED);
        invitationRepository.save(invitation);
    }

    private WorkspaceDTO toWorkspaceDTO(Membership membership) {
        Organization organization = membership.getOrganization();
        return new WorkspaceDTO(
            organization.getId(),
            organization.getName(),
            organization.getSlug(),
            organization.getDescription(),
            membership.getRole()
        );
    }
}
