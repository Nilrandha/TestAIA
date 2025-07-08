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

public class FunctionDelete {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @FunctionName("deleteUser")
    public HttpResponseMessage deleteUser(
        @HttpTrigger(name = "req", methods = {HttpMethod.DELETE}, authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context) {

        context.getLogger().info("Processing DELETE request...");

        try {
            Optional<String> requestBody = request.getBody();
            if (!requestBody.isPresent() || requestBody.get().isEmpty()) {
                return errorResponse(request, "Empty request body");
            }

            JsonNode jsonNode = objectMapper.readTree(requestBody.get());
            int id = jsonNode.path("id").asInt(-1);

            if (id <= 0) {
                return errorResponse(request, "Invalid or missing ID for delete");
            }

            String url = System.getenv("DB_SETTINGS");

            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "DELETE FROM users WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, id);
                int rows = stmt.executeUpdate();

                if (rows == 0) {
                    return errorResponse(request, "No user found with the given ID");
                }
            }

            ResponseEntity responseEntity = new ResponseEntity(0, "User deleted successfully", null, null);
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(responseEntity)
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error deleting user: " + e.getMessage());
            return errorResponse(request, "Exception occurred while deleting user");
        }
    }

    private HttpResponseMessage errorResponse(HttpRequestMessage<Optional<String>> request, String message) {
        ResponseEntity responseEntity = new ResponseEntity(-1, message, null, null);
        return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(responseEntity)
                .build();
    }
}
