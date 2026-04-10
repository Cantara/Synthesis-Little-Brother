package {{groupId}};

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/hello")
@Tag(name = "Greeting", description = "Standard greeting endpoint")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Say hello", description = "Returns a simple hello message")
    public String hello() {
        return "Hello from {{serviceName}} (Quarkus)";
    }
}
