package gr.grnet.rciam.odrl.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class PolicyResourceTest {

    @Test
    @TestSecurity(authorizationEnabled = false)
    public void testCreateAndGetPolicy() {
        String jsonPayload = """
            {
                "name": "Integration Test Policy",
                "description": "Created by RestAssured",
                "version": "1.0",
                "policyType": "set",
                "status": "published",
                "labels": ["test"],
                "odrlPolicy": {
                    "uid": "urn:uuid:1234",
                    "permission": [{"target": "urn:asset:test-01", "action": "use"}]
                }
            }
        """;

        String location = given()
            .contentType(ContentType.JSON)
            .body(jsonPayload)
            .when().post("/policies")
            .then()
            .statusCode(201)
            .extract().header("Location");

        String id = location.substring(location.lastIndexOf('/') + 1);

        given()
            .pathParam("id", id)
            .when().get("/policies/{id}")
            .then()
            .statusCode(200)
            .body("odrlPolicy.permission[0].target", equalTo("urn:asset:test-01"));
    }
}
