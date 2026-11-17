package com.andimm09.discordclone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de integrare pe Postgres-ul real (baza discord_clone_test):
 * Flyway ruleaza migrarile, iar testul trece prin tot fluxul REST, cu login JWT.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiFlowTest {

    @TestConfiguration
    static class CleanDatabase {
        @Bean
        FlywayMigrationStrategy cleanThenMigrate() {
            return flyway -> {
                flyway.clean();
                flyway.migrate();
            };
        }
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Test
    void authAndAccessControl() throws Exception {
        long aliceId = id(send(post("/api/auth/register"), null, """
                {"username": "alice", "email": "alice@test.ro", "password": "parola1234"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passwordHash").doesNotExist()));
        send(post("/api/auth/register"), null, """
                {"username": "bob", "email": "bob@test.ro", "password": "parola1234"}""")
                .andExpect(status().isCreated());
        send(post("/api/auth/register"), null, """
                {"username": "eve", "email": "eve@test.ro", "password": "parola1234"}""")
                .andExpect(status().isCreated());

        // username duplicat -> 409, date invalide -> 400
        send(post("/api/auth/register"), null, """
                {"username": "alice", "email": "alt@test.ro", "password": "parola1234"}""")
                .andExpect(status().isConflict());
        send(post("/api/auth/register"), null, """
                {"username": "x", "email": "nu-e-email", "password": "scurt"}""")
                .andExpect(status().isBadRequest());

        // login: parola gresita sau user inexistent -> 401
        send(post("/api/auth/login"), null, """
                {"username": "alice", "password": "gresita123"}""")
                .andExpect(status().isUnauthorized());
        send(post("/api/auth/login"), null, """
                {"username": "nimeni", "password": "parola1234"}""")
                .andExpect(status().isUnauthorized());

        String alice = login("alice");
        String bob = login("bob");
        String eve = login("eve");

        // fara token sau cu token fals -> 401
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        send(get("/api/auth/me"), "token.fals.123", null).andExpect(status().isUnauthorized());
        send(get("/api/auth/me"), alice, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));

        // serverul nou vine cu #general (text) si General (voice), iar alice e OWNER
        long server = id(send(post("/api/servers"), alice, """
                {"name": "Grupa 3A"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(aliceId)));
        JsonNode channels = body(send(get("/api/servers/" + server + "/channels"), alice, null)
                .andExpect(status().isOk()));
        assertThat(channels).hasSize(2);
        long general = channels.get(0).get("id").asLong();
        long voice = channels.get(1).get("id").asLong();
        assertThat(channels.get(1).get("type").asText()).isEqualTo("VOICE");

        // bob nu e membru inca: nu vede serverul, canalele sau mesajele si nu poate scrie
        send(get("/api/servers/" + server), bob, null).andExpect(status().isForbidden());
        send(get("/api/servers/" + server + "/channels"), bob, null).andExpect(status().isForbidden());
        send(get("/api/channels/" + general + "/messages"), bob, null).andExpect(status().isForbidden());
        send(post("/api/channels/" + general + "/messages"), bob, """
                {"content": "salut"}""")
                .andExpect(status().isForbidden());

        // invitatie cu o singura utilizare
        String code = body(send(post("/api/servers/" + server + "/invites"), alice, """
                {"maxUses": 1}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE")))
                .get("code").asText();
        send(post("/api/invites/" + code + "/join"), bob, null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("MEMBER"));
        send(post("/api/invites/" + code + "/join"), bob, null)
                .andExpect(status().isConflict());
        // invitatia s-a consumat, eve nu mai poate intra cu ea
        send(post("/api/invites/" + code + "/join"), eve, null)
                .andExpect(status().isBadRequest());

        send(get("/api/servers/" + server), bob, null).andExpect(status().isOk());
        send(get("/api/servers/" + server + "/members"), bob, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", contains("alice", "bob")));
        // fiecare isi vede doar serverele lui
        send(get("/api/users/me/servers"), bob, null)
                .andExpect(jsonPath("$[0].name").value("Grupa 3A"));
        send(get("/api/users/me/servers"), eve, null)
                .andExpect(jsonPath("$", hasSize(0)));

        // MEMBER nu poate crea canale
        send(post("/api/servers/" + server + "/channels"), bob, """
                {"name": "secret", "type": "TEXT"}""")
                .andExpect(status().isForbidden());

        // mesaje
        send(post("/api/channels/" + general + "/messages"), alice, """
                {"content": "primul"}""").andExpect(status().isCreated());
        long bobMsg = id(send(post("/api/channels/" + general + "/messages"), bob, """
                {"content": "al doilea"}""").andExpect(status().isCreated()));
        send(post("/api/channels/" + voice + "/messages"), bob, """
                {"content": "nu merge pe voice"}""")
                .andExpect(status().isBadRequest());

        // alice nu poate edita mesajul lui bob, dar il poate sterge (OWNER)
        send(patch("/api/messages/" + bobMsg), alice, """
                {"content": "hack"}""")
                .andExpect(status().isForbidden());
        send(patch("/api/messages/" + bobMsg), bob, """
                {"content": "al doilea (editat)"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.editedAt").isNotEmpty());
        send(delete("/api/messages/" + bobMsg), alice, null)
                .andExpect(status().isNoContent());

        // istoricul: cele mai noi primele, cel sters apare fara continut
        send(get("/api/channels/" + general + "/messages?size=10"), bob, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.items[0].deleted").value(true))
                .andExpect(jsonPath("$.items[0].content").value(nullValue()))
                .andExpect(jsonPath("$.items[1].content").value("primul"))
                .andExpect(jsonPath("$.items[1].authorUsername").value("alice"));

        // doar OWNER-ul poate sterge serverul
        send(delete("/api/servers/" + server), bob, null).andExpect(status().isForbidden());
        send(delete("/api/servers/" + server), alice, null).andExpect(status().isNoContent());
        send(get("/api/servers/" + server), alice, null).andExpect(status().isNotFound());
    }

    private String login(String username) throws Exception {
        return body(send(post("/api/auth/login"), null, """
                {"username": "%s", "password": "parola1234"}""".formatted(username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer")))
                .get("accessToken").asText();
    }

    /** Trimite request-ul, optional cu token (Authorization: Bearer ...) si body JSON. */
    private ResultActions send(MockHttpServletRequestBuilder request, String token, String jsonBody) throws Exception {
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        if (jsonBody != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(jsonBody);
        }
        return mvc.perform(request);
    }

    private JsonNode body(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private long id(ResultActions result) throws Exception {
        return body(result).get("id").asLong();
    }
}
