package com.tatsinktech.keycloak.service;

import com.tatsinktech.keycloak.util.KeyCloakUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class KeycloakService {

    private static Logger logger = LoggerFactory.getLogger(KeycloakService.class);

    @Value("${keycloak.serverUrl}")
    private String SERVER_URL;

    @Value("${keycloak.realm}")
    private String REALM;

    @Value("${keycloak.master.realm}")
    private String masterREALM;

    @Value("${keycloak.username}")
    private String USERNAME;

    @Value("${keycloak.password}")
    private String PASSWORD;

    @Value("${keycloak.clientId}")
    private String CLIENT_ID;

    private Keycloak getInstance() {
        return KeycloakBuilder
                .builder()
                .serverUrl(SERVER_URL)
                .realm(masterREALM)
                .username(USERNAME)
                .password(PASSWORD)
                .clientId(CLIENT_ID)
                .build();

    }

    public int createAccount(String username, String password) {

        Keycloak keycloak = getInstance();

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(Boolean.TRUE);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setFirstName("First Name");
        user.setLastName("Last Name");
        user.singleAttribute("customAttribute", "customAttribute");
        user.setEmail(username + "@yahoo.fr");
        user.setEnabled(Boolean.TRUE);
        user.setCredentials(Arrays.asList(credential));
        // Get realm
        RealmResource realmResource = keycloak.realm(REALM);
        UsersResource userRessource = realmResource.users();

        // Create user (requires manage-users role)
        Response response = userRessource.create(user);

        //javax.ws.rs.core.Response response = getInstance().realm(REALM).users().create(user);
        final int status = response.getStatus();
        if (status != HttpStatus.CREATED.value()) {
            return status;
        }

        final String createdId = KeyCloakUtil.getCreatedId(response);
        // Reset password
        CredentialRepresentation newCredential = new CredentialRepresentation();
        UserResource userResource = keycloak.realm(REALM).users().get(createdId);
        newCredential.setType(CredentialRepresentation.PASSWORD);
        newCredential.setValue(password);
        newCredential.setTemporary(false);
        userResource.resetPassword(newCredential);
        return HttpStatus.CREATED.value();
    }

    public int addRoleToListOf(String role, String compositeRole) {

        Keycloak keycloak = getInstance();

        List<String> listRoles = getAllRoles();

        final String clientUuid = keycloak.realm(REALM)
                .clients()
                .findByClientId(CLIENT_ID)
                .get(0)
                .getId();

        RolesResource rolesResource = keycloak.realm(REALM)
                .clients()
                .get(clientUuid)
                .roles();

        final List<RoleRepresentation> existingRoles = rolesResource.list();

        final boolean roleExists = existingRoles.stream().anyMatch(r -> r.getName().equals(role));

        if (!roleExists) {
            RoleRepresentation roleRepresentation = new RoleRepresentation();
            roleRepresentation.setName(role);
            roleRepresentation.setClientRole(true);
            roleRepresentation.setComposite(false);

            rolesResource.create(roleRepresentation);
        }

        if (compositeRole != null) {
            final boolean compositeExists = existingRoles.stream().anyMatch(r -> r.getName().equals(compositeRole));

            if (!compositeExists) {
                RoleRepresentation compositeRoleRepresentation = new RoleRepresentation();
                compositeRoleRepresentation.setName(compositeRole);
                compositeRoleRepresentation.setClientRole(true);
                compositeRoleRepresentation.setComposite(true);

                rolesResource.create(compositeRoleRepresentation);
            }

            final RoleResource compositeRoleResource = rolesResource.get(compositeRole);

            final boolean alreadyAdded = compositeRoleResource.getRoleComposites().stream().anyMatch(r -> r.getName().equals(role));

            if (!alreadyAdded) {
                final RoleRepresentation roleToAdd = rolesResource.get(role).toRepresentation();
                compositeRoleResource.addComposites(Collections.singletonList(roleToAdd));
            }

        }
        return HttpStatus.CREATED.value();
    }

    public List<String> getAllRoles() {

        Keycloak keycloak = getInstance();

        List<String> availableRoles = keycloak
                .realm(REALM)
                .roles()
                .list()
                .stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());
        return availableRoles;
    }

    public int addRealmRole(String new_role_name) {
        Keycloak keycloak = getInstance();

        if (!getAllRoles().contains(new_role_name)) {
            RoleRepresentation roleRep = new RoleRepresentation();
            roleRep.setName(new_role_name);
            roleRep.setDescription("role_" + new_role_name);
            keycloak.realm(REALM).roles().create(roleRep);
            return HttpStatus.CREATED.value();
        } else {
            return HttpStatus.NOT_FOUND.value();
        }

    }

    public int makeComposite(String role_name) {
        Keycloak keycloak = getInstance();

        RoleRepresentation role = keycloak
                .realm(REALM)
                .roles()
                .get(role_name)
                .toRepresentation();

        List<RoleRepresentation> composites = new LinkedList<>();

        composites.add(keycloak
                .realm(REALM)
                .roles()
                .get("offline_access")
                .toRepresentation()
        );

        keycloak.realm(REALM)
                .rolesById()
                .addComposites(role.getId(), composites);
         return HttpStatus.CREATED.value();
    }

    public int addRealmRoleToUser(String userName, String role_name) {
        Keycloak keycloak = getInstance();

        String userId = keycloak
                .realm(REALM)
                .users()
                .search(userName)
                .get(0)
                .getId();
        
        UserResource user = keycloak
                .realm(REALM)
                .users()
                .get(userId);
        
        
        List<RoleRepresentation> roleToAdd = new LinkedList<>();

        roleToAdd.add(keycloak.realm(REALM)
                .roles()
                .get(role_name)
                .toRepresentation()
        );
        
        user.roles().realmLevel().add(roleToAdd);
        return HttpStatus.CREATED.value();
    }
}
