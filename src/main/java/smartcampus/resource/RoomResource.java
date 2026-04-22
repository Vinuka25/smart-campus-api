package smartcampus.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;
import smartcampus.exception.RoomNotEmptyException;
import smartcampus.model.Room;
import smartcampus.repository.CampusRepository;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {
    private static final Logger LOGGER = Logger.getLogger(RoomResource.class.getName());
    private final CampusRepository repository = CampusRepository.getInstance();

    @GET
    public List<Room> getAllRooms() {
        LOGGER.info("Fetching all rooms");
        return repository.getAllRooms();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("room id is required").build();
        }
        repository.createRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        LOGGER.info("Room created: " + room.getId());
        return Response.created(location).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Room getRoom(@PathParam("roomId") String roomId) {
        Room room = repository.getRoom(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found: " + roomId);
        }
        LOGGER.info("Fetched room " + roomId);
        return room;
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = repository.getRoom(roomId);
        if (room == null) {
            return Response.noContent().build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room " + roomId + " still has active sensors and cannot be deleted.");
        }
        repository.deleteRoom(roomId);
        LOGGER.info("Deleted room " + roomId);
        return Response.noContent().build();
    }
}
