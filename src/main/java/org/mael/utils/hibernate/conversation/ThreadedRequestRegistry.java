package org.mael.utils.hibernate.conversation;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Singleton registry for {@link HttpServletRequest} that bounds them to the
 * thread that calls {@link #setCurrentRequest(HttpServletRequest)}.
 * </p>
 * 
 * <p>
 * Needed by {@link ConversationalCurrentSessionContext} to retrieve the request
 * processed by the container in the current worker thread.
 * </p>
 * 
 * <p>
 * Note: Internally, this registry uses a {@link ThreadLocal}. That means that
 * threads spawned by the container's worker thread will NOT be able to get the
 * request.
 * </p>
 * 
 * @author ElderMael
 * 
 */
public class ThreadedRequestRegistry {

	private ThreadedRequestRegistry() {
	}

	private static ThreadLocal<HttpServletRequest> requestsRegistry = new ThreadLocal<HttpServletRequest>() {
		@Override
		protected HttpServletRequest initialValue() {
			return null;
		}
	};

	/**
	 * Retrieves the request associated to this thread registered by
	 * {@link OpenSessionInViewInsideConversationFilter}.
	 * 
	 * @return the request bound to the thread executing this method.
	 */
	public static HttpServletRequest getCurrentThreadRequest() {
		return requestsRegistry.get();
	}

	/**
	 * Will register the request bounding it to the thread calling this method.
	 * 
	 * @param request
	 *            - the request to be registered to the thread calling this
	 *            method.
	 */
	public static void setCurrentRequest(HttpServletRequest request) {

		if (request == null) {
			requestsRegistry.remove();
		} else {
			requestsRegistry.set(request);
		}

	}

}
