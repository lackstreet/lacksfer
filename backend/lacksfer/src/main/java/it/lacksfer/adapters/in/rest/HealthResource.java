package it.lacksfer.adapters.in.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/health")
public class HealthResource {
    @GET
    public String health(){
        return "Lacksfer backend is running";
    }
}
