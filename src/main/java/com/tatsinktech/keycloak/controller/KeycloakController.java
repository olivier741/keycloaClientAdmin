package com.tatsinktech.keycloak.controller;

import com.tatsinktech.keycloak.bean.Account;
import com.tatsinktech.keycloak.service.KeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * We will be using API.
 *
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@RestController
@RequestMapping(path = "/keycloak", produces = MediaType.APPLICATION_JSON_VALUE)
public class KeycloakController {

    @Autowired
    private KeycloakService service;

    @GetMapping(path = "/hello")
    public String hello() {
        return "Hello World!";
    }

    @PostMapping(path = "/addRole")
    public void rolesOfCurrentUser(@RequestBody String roleName) {
        service.addRealmRole(roleName);
    }

    @PostMapping(path = "/add_com_role")
    public int rolesOfmakeComposite(@RequestBody String roleName) {
        return service.makeComposite(roleName);
    }

    @PostMapping(path = "/addUser")
    public int createUser(@RequestBody Account account) {
        return service.createAccount(account.getUsername(), account.getPassword());
    }

    @PostMapping(path = "/RoleUser")
    public int addRoleUser(@RequestBody Account account) {
        return service.addRealmRoleToUser(account.getUsername(), account.getRole());
    }
}
