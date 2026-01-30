package gr.grnet.rciam.odrl.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class PolicyResourceTest {

    @Test
    @TestSecurity(user = "admin", roles = {"policies:read"})
    public void testListPolicies() {
        given()
            .when().get("/policies")
            .then()
            .statusCode(200)
            .body("items", notNullValue())
            .body("total", notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"policies:write"})
    public void testCreatePolicy() {
        String payload = """
            {
                "name": "Test Policy",
                "description": "Valid description",
                "policyType": "set",
                "status": "draft",
                "odrlPolicy": {
                    "uid": "urn:uuid:123",
                    "type": "Set"
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when().post("/policies")
            .then()
            .statusCode(201)
            .header("Location", containsString("/policies/"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"policies:write"})
    public void testCreatePolicyValidationFail() {
        String invalidPayload = """
            {
                "name": "Invalid Policy",
                "policyType": "set",
                "status": "draft",
                "odrlPolicy": {}
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(invalidPayload)
            .when().post("/policies")
            .then()
            .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"policies:read"})
    public void testPaginationSemantics() {
        given()
            .queryParam("offset", 0)
            .queryParam("limit", 1)
            .when().get("/policies")
            .then()
            .statusCode(200)
            .body("limit", equalTo(1))
            .body("offset", equalTo(0));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"policies:write"})
    public void testPatchPolicy() {
        String id = createPolicyAndGetId();

        String patchPayload = """
            {
                "name": "Patched Name",
                "description": null
            }
            """;

        given()
            .contentType("application/merge-patch+json")
            .body(patchPayload)
            .when().patch("/policies/" + id)
            .then()
            .statusCode(200)
            .body("name", equalTo("Patched Name"))
            .body("description", nullValue());
    }

    @Test
    @TestSecurity(user = "reader", roles = {"policies:read"})
    public void testWriteForbiddenForReader() {
        String payload = """
            {
                "name": "Hacker Policy",
                "description": "Should fail",
                "policyType": "set",
                "status": "draft",
                "odrlPolicy": {}
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when().post("/policies")
            .then()
            .statusCode(403);
    }

    private String createPolicyAndGetId() {
        String payload = """
            {
                "name": "Temp Policy",
                "description": "Temp Desc",
                "policyType": "set",
                "status": "draft",
                "odrlPolicy": {"uid": "1"}
            }
            """;

        String location = given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when().post("/policies")
            .then()
            .statusCode(201)
            .extract().header("Location");

        return location.substring(location.lastIndexOf("/") + 1);
    }
}
