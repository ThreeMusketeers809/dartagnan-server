package service;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import core.entities.Employee;
import core.service.ContentTypeHeader;
import core.service.ServicePresets;
import dao.EmployeeDao;

@Path(ServicePresets.QUERY_SERVICES_PATH)
@Produces({ ServicePresets.PRIMARY_OBJECT_MEDIA_TYPE, ServicePresets.SECONDARY_OBJECT_MEDIA_TYPE })
public class EmployeeService extends AbstractWebService<Employee> {
	private final String SERVICE_ROOT = "/employees";
	private final String ENTITY_ID = "uuid";
	private final String ENTITY_PATH = "/{" + ENTITY_ID + "}";

	private EmployeeDao employeeDao;

	@GET
	@Path(SERVICE_ROOT)
	public Response readAll(@HeaderParam("accept") String acceptHeader, @QueryParam("cedula") String cedula) {
		Response response;

		// Try to honor the ACCEPT header
		ContentTypeHeader header = parseAcceptHeader(acceptHeader);
		MediaType responseMediaType = getPreferredMediaType(header);
		if (responseMediaType == null) {
			responseMediaType = MediaType.valueOf(ServicePresets.PRIMARY_OBJECT_MEDIA_TYPE);
		}

		if (cedula != null && cedula.trim().length() > 0) {
			Employee employee = employeeDao.getByCedula(cedula);
			if (employee == null) {
				response = Response.status(Response.Status.NOT_FOUND).entity("Employee not found for cedula: " + cedula)
						.build();
			} else {
				response = Response.ok(employee, responseMediaType).build();
			}
		} else {
			List<Employee> employees = employeeDao.getAll();
			GenericEntity<List<Employee>> employeesEntity = new GenericEntity<List<Employee>>(employees) {
			};
			response = Response.ok(employeesEntity, responseMediaType).build();
		}

		return response;
	}

	@GET
	@Path(SERVICE_ROOT + ENTITY_PATH)
	public Response read(@HeaderParam("accept") String acceptHeader, @PathParam(ENTITY_ID) String entityId) {
		Response response;

		// Try to honor the ACCEPT header
		ContentTypeHeader header = parseAcceptHeader(acceptHeader);
		MediaType responseMediaType = getPreferredMediaType(header);
		if (responseMediaType == null) {
			responseMediaType = MediaType.valueOf(ServicePresets.PRIMARY_OBJECT_MEDIA_TYPE);
		}

		Employee employee = employeeDao.get(entityId);
		if (employee == null) {
			response = Response.status(Response.Status.NOT_FOUND).entity("Student not found for UUID: " + entityId).build();
		} else {
			response = Response.ok(employee, responseMediaType).build();
		}

		return response;
	}

	@Override
	@POST
	@Path(SERVICE_ROOT)
	@Consumes({ ServicePresets.PRIMARY_OBJECT_MEDIA_TYPE, ServicePresets.SECONDARY_OBJECT_MEDIA_TYPE })
	public Response create(@Context UriInfo uriInfo, Employee entity) {
		Response response;
		Employee createdEmployee = null;
		
		if (entity != null) {
			createdEmployee = employeeDao.create(entity);
		}
		if (createdEmployee != null) {
			EntityTag entityTag = new EntityTag(createdEmployee.getEntityId());
			URI entityUri = URI.create(String.format("%s/%s", uriInfo.getRequestUri(), entityTag.getValue()));
			response = Response.created(entityUri).tag(entityTag).build();
		} else {
			response = Response.status(Status.BAD_REQUEST).build();
		}
		return response;
	}

	@Override
	@PUT
	@Path(SERVICE_ROOT + ENTITY_PATH)
	@Consumes({ ServicePresets.PRIMARY_OBJECT_MEDIA_TYPE, ServicePresets.SECONDARY_OBJECT_MEDIA_TYPE })
	public Response update(@PathParam(ENTITY_ID) String entityId, Employee entity) {
		Response response;

		if (entityId != null && entityId.trim().length() > 0 && entity != null) {
			boolean opResult = employeeDao.update(entityId, entity);
			if (opResult != true) {
				response = Response.status(Response.Status.NOT_FOUND).entity("Student not found for uuid: " + entityId)
						.build();
			} else {
				response = Response.ok().build();
			}
		} else {
			response = Response.status(Status.BAD_REQUEST).build();
		}
		return response;
	}

	@Override
	@DELETE
	@Path(SERVICE_ROOT + ENTITY_PATH)
	public Response delete(@PathParam(ENTITY_ID) String entityId) {
		Response response;

		if (entityId != null && entityId.trim().length() > 0) {
			boolean opResult = employeeDao.delete(entityId);
			if (opResult != true) {
				response = Response.status(Response.Status.NOT_FOUND).entity("Student not found for uuid: " + entityId)
						.build();
			} else {
				response = Response.ok().build();
			}
		} else {
			response = Response.status(Status.BAD_REQUEST).build();
		}
		return response;
	}
	
	public EmployeeService() {
		employeeDao = new EmployeeDao();
	}
}