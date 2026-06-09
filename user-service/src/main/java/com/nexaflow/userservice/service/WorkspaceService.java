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
import com.nexaflow.userservice.service.dto.ChangeMemberRoleRequest;
import com.nexaflow.userservice.service.dto.CreateWorkspaceRequest;
import com.nexaflow.userservice.service.dto.CurrentMembershipDTO;
import com.nexaflow.userservice.service.dto.InvitationResponseDTO;
import com.nexaflow.userservice.service.dto.InviteUserRequest;
import com.nexaflow.userservice.service.dto.MemberDTO;
import com.nexaflow.userservice.service.dto.WorkspaceDTO;
import com.nexaflow.userservice.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
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
        CurrentUser currentUser = getCurrentUser();

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
        ownerMembership.setUserId(currentUser.id());
        ownerMembership.setUserLogin(currentUser.login());
        ownerMembership.setUserEmail(currentUser.email());
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

    @Transactional(readOnly = true)
    public WorkspaceDTO getWorkspace(Long organizationId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated")
        );

        Membership currentMembership = membershipRepository
            .findOneByOrganizationIdAndUserLoginAndActiveTrue(organizationId, currentUserLogin)
            .orElseThrow(() -> new BadRequestAlertException("You are not a member of this workspace", ENTITY_NAME, "notmember"));

        Organization organization = organizationRepository
            .findById(organizationId)
            .orElseThrow(() -> new BadRequestAlertException("Workspace not found", ENTITY_NAME, "notfound"));

        if (!organization.getActive()) {
            throw new BadRequestAlertException("Workspace is not active", ENTITY_NAME, "workspacenotactive");
        }

        return new WorkspaceDTO(
            organization.getId(),
            organization.getName(),
            organization.getSlug(),
            organization.getDescription(),
            currentMembership.getRole()
        );
    }

    public InvitationResponseDTO inviteUser(Long organizationId, InviteUserRequest request) {
        CurrentUser currentUser = getCurrentUser();

        Membership currentMembership = membershipRepository
            .findOneByOrganizationIdAndUserLoginAndActiveTrue(organizationId, currentUser.login())
            .orElseThrow(() -> new BadRequestAlertException("You are not a member of this workspace", ENTITY_NAME, "notmember"));

        if (currentMembership.getRole() != MembershipRole.OWNER && currentMembership.getRole() != MembershipRole.ADMIN) {
            throw new BadRequestAlertException("You do not have permission to invite users", ENTITY_NAME, "nopermission");
        }

        if (request.getRole() == MembershipRole.OWNER) {
            throw new BadRequestAlertException("You cannot invite another owner", ENTITY_NAME, "ownernotallowed");
        }

        String normalizedEmail = normalizeEmail(request.getEmail());

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

        invitation.setInvitedByUserId(currentUser.id());
        invitation.setInvitedByLogin(currentUser.login());

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
        CurrentUser currentUser = getCurrentUser();

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

        if (!normalizeEmail(invitation.getEmail()).equals(currentUser.email())) {
            throw new BadRequestAlertException("Invitation email does not match the current user", ENTITY_NAME, "invitationemailmismatch");
        }

        Organization organization = invitation.getOrganization();

        boolean alreadyMember = membershipRepository
            .findOneByOrganizationIdAndUserLoginAndActiveTrue(organization.getId(), currentUser.login())
            .isPresent();

        if (alreadyMember) {
            throw new BadRequestAlertException("You are already a member of this workspace", ENTITY_NAME, "alreadymember");
        }

        Membership membership = new Membership();
        membership.setOrganization(organization);
        membership.setUserId(currentUser.id());
        membership.setUserLogin(currentUser.login());
        membership.setUserEmail(currentUser.email());
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
    public List<InvitationResponseDTO> findMyPendingInvitations() {
        CurrentUser currentUser = getCurrentUser();

        return invitationRepository
            .findByEmailAndStatus(currentUser.email(), InvitationStatus.PENDING)
            .stream()
            .filter(invitation -> invitation.getExpiresAt().isAfter(Instant.now()))
            .map(this::toInvitationResponseDTO)
            .toList();
    }

    public void rejectInvitation(Long invitationId) {
        CurrentUser currentUser = getCurrentUser();

        Invitation invitation = invitationRepository
            .findOneByIdAndEmailAndStatus(invitationId, currentUser.email(), InvitationStatus.PENDING)
            .orElseThrow(() -> new BadRequestAlertException("Pending invitation not found", ENTITY_NAME, "invitationnotfound"));

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);

            throw new BadRequestAlertException("Invitation has expired", ENTITY_NAME, "invitationexpired");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitationRepository.save(invitation);
    }

    @Transactional(readOnly = true)
    public List<MemberDTO> getWorkspaceMembers(Long organizationId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated")
        );

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
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated")
        );

        Membership currentMembership = membershipRepository
            .findOneByOrganizationIdAndUserLoginAndActiveTrue(organizationId, currentUserLogin)
            .orElseThrow(() -> new BadRequestAlertException("You are not a member of this workspace", ENTITY_NAME, "notmember"));

        if (currentMembership.getRole() != MembershipRole.OWNER && currentMembership.getRole() != MembershipRole.ADMIN) {
            throw new BadRequestAlertException("You do not have permission to view invitations", ENTITY_NAME, "nopermission");
        }

        return invitationRepository
            .findByOrganizationIdAndStatus(organizationId, InvitationStatus.PENDING)
            .stream()
            .map(this::toInvitationResponseDTO)
            .toList();
    }

    public void revokeInvitation(Long organizationId, Long invitationId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated")
        );

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

    public MemberDTO changeMemberRole(Long organizationId, Long membershipId, ChangeMemberRoleRequest request) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated")
        );

        Membership currentMembership = membershipRepository
            .findOneByOrganizationIdAndUserLoginAndActiveTrue(organizationId, currentUserLogin)
            .orElseThrow(() -> new BadRequestAlertException("You are not a member of this workspace", ENTITY_NAME, "notmember"));

        if (currentMembership.getRole() != MembershipRole.OWNER) {
            throw new BadRequestAlertException("Only the workspace owner can change member roles", ENTITY_NAME, "nopermission");
        }

        if (request.getRole() == MembershipRole.OWNER) {
            throw new BadRequestAlertException("You cannot assign OWNER role with this endpoint", ENTITY_NAME, "ownernotallowed");
        }

        Membership targetMembership = membershipRepository
            .findOneByIdAndOrganizationIdAndActiveTrue(membershipId, organizationId)
            .orElseThrow(() -> new BadRequestAlertException("Membership not found", ENTITY_NAME, "membershipnotfound"));

        if (targetMembership.getRole() == MembershipRole.OWNER) {
            throw new BadRequestAlertException("Owner role cannot be changed", ENTITY_NAME, "ownercannotbechanged");
        }

        if (targetMembership.getId().equals(currentMembership.getId())) {
            throw new BadRequestAlertException("You cannot change your own role", ENTITY_NAME, "cannotchangeyourself");
        }

        targetMembership.setRole(request.getRole());

        Membership savedMembership = membershipRepository.save(targetMembership);

        return new MemberDTO(
            savedMembership.getId(),
            savedMembership.getUserId(),
            savedMembership.getUserLogin(),
            savedMembership.getUserEmail(),
            savedMembership.getRole(),
            savedMembership.getActive()
        );
    }

    public void removeMember(Long organizationId, Long membershipId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated")
        );

        Membership currentMembership = membershipRepository
            .findOneByOrganizationIdAndUserLoginAndActiveTrue(organizationId, currentUserLogin)
            .orElseThrow(() -> new BadRequestAlertException("You are not a member of this workspace", ENTITY_NAME, "notmember"));

        Membership targetMembership = membershipRepository
            .findOneByIdAndOrganizationIdAndActiveTrue(membershipId, organizationId)
            .orElseThrow(() -> new BadRequestAlertException("Membership not found", ENTITY_NAME, "membershipnotfound"));

        if (targetMembership.getRole() == MembershipRole.OWNER) {
            throw new BadRequestAlertException("Owner cannot be removed from the workspace", ENTITY_NAME, "ownercannotberemoved");
        }

        if (targetMembership.getId().equals(currentMembership.getId())) {
            throw new BadRequestAlertException("You cannot remove yourself from the workspace", ENTITY_NAME, "cannotremoveyourself");
        }

        if (currentMembership.getRole() == MembershipRole.MEMBER) {
            throw new BadRequestAlertException("Members cannot remove users", ENTITY_NAME, "nopermission");
        }

        if (currentMembership.getRole() == MembershipRole.ADMIN && targetMembership.getRole() == MembershipRole.ADMIN) {
            throw new BadRequestAlertException("Admins cannot remove other admins", ENTITY_NAME, "nopermission");
        }

        targetMembership.setActive(false);
        membershipRepository.save(targetMembership);
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

    private InvitationResponseDTO toInvitationResponseDTO(Invitation invitation) {
        Organization organization = invitation.getOrganization();
        return new InvitationResponseDTO(
            invitation.getId(),
            organization.getId(),
            organization.getName(),
            organization.getSlug(),
            invitation.getEmail(),
            invitation.getToken(),
            invitation.getRole(),
            invitation.getStatus(),
            invitation.getExpiresAt(),
            invitation.getInvitedByLogin()
        );
    }

    private CurrentUser getCurrentUser() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated")
        );
        Long id = SecurityUtils.getCurrentUserId().orElseThrow(() ->
            new BadRequestAlertException("Current user id is missing from the token", ENTITY_NAME, "useridmissing")
        );
        String email = SecurityUtils.getCurrentUserEmail()
            .map(WorkspaceService::normalizeEmail)
            .orElseThrow(() ->
                new BadRequestAlertException("Current user email is missing from the token", ENTITY_NAME, "useremailmissing")
            );

        return new CurrentUser(id, login, email);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ENGLISH);
    }

    private record CurrentUser(Long id, String login, String email) {}

    @Transactional(readOnly = true)
    public CurrentMembershipDTO getCurrentMembership(Long organizationId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new AccessDeniedException("User is not authenticated"));

        Membership membership = membershipRepository
            .findOneByOrganizationIdAndUserLoginAndActiveTrue(organizationId, currentUserLogin)
            .orElseThrow(() -> new AccessDeniedException("You are not a member of this workspace"));

        return new CurrentMembershipDTO(organizationId, membership.getRole());
    }
}
