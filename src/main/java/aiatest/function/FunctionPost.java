package aiatest.function;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * Azure Function to handle POST requests for inserting user records.
 */
public class FunctionPost {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @FunctionName("HttpAIA")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a POST request.");

        try {
            String url = System.getenv("DB_SETTINGS");

            Optional<String> requestBody = request.getBody();
            if (!requestBody.isPresent() || requestBody.get().isEmpty()) {
                return errorResponse(request, "Empty request body", null, null);
            }

            JsonNode jsonNode = objectMapper.readTree(requestBody.get());

            // Extract and validate input
            String firstName = jsonNode.path("firstName").asText(null);
            String lastName = jsonNode.path("lastName").asText(null);
            String city = jsonNode.path("city").asText(null);
            Integer age = jsonNode.has("age") && jsonNode.get("age").canConvertToInt() ? jsonNode.get("age").asInt() : null;

            if (firstName == null || lastName == null || city == null || age == null ||
                firstName.isEmpty() || lastName.isEmpty() || city.isEmpty() ||
                !firstName.matches("[a-zA-Z]+") || !lastName.matches("[a-zA-Z]+")) {
                return errorResponse(request, "Invalid or missing data for insert", null, null);
            }

            // Insert into database
            try (Connection conn = DriverManager.getConnection(url)) {
                String checkSql = "SELECT COUNT(*) FROM users WHERE first_name = ? AND last_name = ? AND city = ? AND age = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, firstName);
                checkStmt.setString(2, lastName);
                checkStmt.setString(3, city);
                checkStmt.setInt(4, age);
                var rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);
                if (count > 0) {
                    return errorResponse(request, "Duplicate user record already exists.", null, null);
                }

                String sql = "INSERT INTO users (first_name, last_name, city, age) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, city);
                stmt.setInt(4, age);
                stmt.executeUpdate();
                context.getLogger().info("User inserted successfully");
            }

            return successResponse(request, "User inserted successfully", firstName, lastName);

        } catch (Exception e) {
            context.getLogger().severe("Error processing POST request: " + e.getMessage());
            return errorResponse(request, "Exception occurred", null, null);
        }
    }

    private HttpResponseMessage successResponse(HttpRequestMessage<Optional<String>> request, String message, String firstName, String lastName) {
        ResponseEntity responseEntity = new ResponseEntity(0, message, firstName, lastName);
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(responseEntity)
                .build();
    }

    private HttpResponseMessage errorResponse(HttpRequestMessage<Optional<String>> request, String message, String firstName, String lastName) {
        ResponseEntity responseEntity = new ResponseEntity(-1, message, firstName, lastName);
        return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(responseEntity)
                .build();
    }
}
