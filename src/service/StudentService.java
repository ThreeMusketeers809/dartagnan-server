package service;

import java.net.URI;
import java.net.URISyntaxException;
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
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import core.Student;
import service.ContentTypeHeader;

@Path(ServicePresets.QUERY_SERVICES_PATH)
@Produces({ ServicePresets.PRIMARY_OBJECT_MEDIA_TYPE, ServicePresets.SECONDARY_OBJECT_MEDIA_TYPE })
public class StudentService extends GenericWebService<Student> {
	private final String SERVICE_ROOT = "/students";
	private final String ENTITY_ID = "uuid";
	private final String ENTITY_PATH = "/{" + ENTITY_ID + "}";

	private StudentDao studentDao = new StudentDao();

	@GET
	@Path(SERVICE_ROOT)
	public Response readAll(@HeaderParam("accept") String acceptHeader, @QueryParam("cedula") String cedula,
			@QueryParam("student-id") String studentId) {
		Response response;

		// Try to honor the ACCEPT header
		ContentTypeHeader header = parseAcceptHeader(acceptHeader);
		MediaType responseMediaType = getPreferredMediaType(header);
		if (responseMediaType == null) {
			responseMediaType = MediaType.valueOf(ServicePresets.PRIMARY_OBJECT_MEDIA_TYPE);
		}

		// cedula gets precedence over student-id
		if (cedula != null && cedula.trim().length() > 0) {
			Student student = studentDao.getByCedula(cedula);
			if (student == null) {
				response = Response.status(Response.Status.NOT_FOUND).entity("Student not found for cedula: " + cedula)
						.build();
			} else {
				response = Response.ok(student, responseMediaType).build();
			}
		} else if (studentId != null && studentId.trim().length() > 0) {
			Student student = studentDao.getByStudentId(studentId);
			if (student == null) {
				response = Response.status(Response.Status.NOT_FOUND)
						.entity("Student not found for student-id: " + studentId).build();
			} else {
				// If no unique identifier was provided, return all students
				response = Response.ok(student, responseMediaType).build();
			}
		} else {
			List<Student> students = studentDao.getAll();
			GenericEntity<List<Student>> studentsEntity = new GenericEntity<List<Student>>(students) {
			};
			response = Response.ok(studentsEntity, responseMediaType).build();
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

		Student student = studentDao.get(entityId);
		if (student == null) {
			response = Response.status(Response.Status.NOT_FOUND).entity("Student not found for UUID: " + entityId).build();
		} else {
			response = Response.ok(student, responseMediaType).build();
		}

		return response;
	}

	@Override
	@POST
	@Path(SERVICE_ROOT)
	@Consumes({ ServicePresets.PRIMARY_OBJECT_MEDIA_TYPE, ServicePresets.SECONDARY_OBJECT_MEDIA_TYPE })
	public Response create(Student entity) {
		Response response;
		Student createdStudent = null;
		
		if (entity != null) {
			createdStudent = studentDao.create(entity);
			System.err.println("RECEIVED NULL");
		}
		if (createdStudent != null) {
			URI createdUri = null;
			try {
				createdUri = new URI(String.format("/%s", createdStudent.getEntityId()));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			response = Response.created(createdUri).build();
		} else {
			response = Response.status(Status.BAD_REQUEST).build();
		}
		return response;
	}

	@Override
	@PUT
	@Path(SERVICE_ROOT + ENTITY_PATH)
	@Consumes({ ServicePresets.PRIMARY_OBJECT_MEDIA_TYPE, ServicePresets.SECONDARY_OBJECT_MEDIA_TYPE })
	public Response update(@PathParam(ENTITY_ID) String entityId, Student entity) {
		Response response;

		if (entityId != null && entityId.trim().length() > 0 && entity != null) {
			boolean opResult = studentDao.update(entityId, entity);
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
			boolean opResult = studentDao.delete(entityId);
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
}