package service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import service.ContentTypeHeader;
import service.ContentTypeUnit;

/**
 * 
 * General template class for a web service.
 * 
 * @author Francisco Frias
 * @author Abel Guzman
 * @author Amin Guzman
 * 
 */
public abstract class GenericWebService<T> {
	final static String CONTENT_TYPE_WILDCARD = "*/*";

	/**
	 * 
	 * Parses the accept header from an HTTP request into a
	 * {@link #util.ContentTypeHeader}.
	 * 
	 * @param acceptHeader
	 *            The raw accept header from the request.
	 * @return A {@link #util.ContentTypeHeader} object representing the header.
	 *         Empty or null headers default to a wildcard value.
	 */
	public ContentTypeHeader parseAcceptHeader(String acceptHeader) {
		ContentTypeHeader header = ContentTypeHeader.parse(CONTENT_TYPE_WILDCARD);

		if (acceptHeader != null && acceptHeader.trim().length() > 0) {
			try {
				header = ContentTypeHeader.parse(acceptHeader);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		return header;
	}

	/**
	 * 
	 * Determines which media type from those produced by the service is the most
	 * suitable from the options specified in the header.
	 * 
	 * @param acceptHeader
	 *            Accept header representing media types specified as accepted in
	 *            the request.
	 * @return The preferred {@link #javax.ws.rs.core.MediaType} that we can
	 *         provide, or null if we cannot provide any of the requested media
	 *         types.
	 */
	public MediaType getPreferredMediaType(ContentTypeHeader acceptHeader) {
		MediaType mediaType = null;
		for (ContentTypeUnit unit : acceptHeader) {
			if (unit.getMediaType().equals(MediaType.valueOf(ServicePresets.PRIMARY_OBJECT_MEDIA_TYPE))
					|| unit.getMediaType().equals(MediaType.valueOf(ServicePresets.SECONDARY_OBJECT_MEDIA_TYPE))) {
				mediaType = unit.getMediaType();
				break;
			}
		}
		return mediaType;
	}

	public abstract Response create(T entity);

	public abstract Response read(String acceptHeader, String entityId);

	public abstract Response update(String entityId, T entity);

	public abstract Response delete(String entityId);
}
