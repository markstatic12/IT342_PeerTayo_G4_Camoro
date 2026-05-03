package edu.cit.camoro.peertayo;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared test utilities — register a user and extract their JWT / ID.
 */
public class TestHelper {

    public record UserInfo(Long id, String token) {}

    /** Register a new user and return both their ID and JWT in one call. */
    public static UserInfo register(MockMvc mockMvc, String email, String password) throws Exception {
        var body = """
                {"firstName":"Test","lastName":"User","email":"%s","password":"%s"}
                """.formatted(email, password);

        var result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Long id    = ((Number) JsonPath.read(json, "$.data.user.id")).longValue();
        String tok = JsonPath.read(json, "$.data.token");
        return new UserInfo(id, tok);
    }

    /** Register and return only the token. */
    public static String registerAndGetToken(MockMvc mockMvc, String email, String password) throws Exception {
        return register(mockMvc, email, password).token();
    }

    /** Register and return only the user ID. */
    public static Long registerAndGetUserId(MockMvc mockMvc, String email, String password) throws Exception {
        return register(mockMvc, email, password).id();
    }

    /** Promote an existing user to FACILITATOR and return the new token. */
    public static String promoteAndGetToken(MockMvc mockMvc, String token) throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/promote-to-facilitator")
                        .header("Authorization", "Bearer " + token))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
    }
}
