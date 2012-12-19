package org.mael.utils.hibernate.conversation;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * <p>
 * Filter class that will look up for a conversation id in the request cookies.
 * </p>
 * 
 * <p>
 * If the cookie is found, the filter will set an attribute to the request that
 * contains the {@link UUID} of the conversation. Also, it will register the
 * procesed request to the {@link ThreadedRequestRegistry} for retrieval in the
 * {@link ConversationalCurrentSessionContext}.
 * </p>
 * 
 * <p>
 * If no cookie is found, a new conversation will be created and a new cookie
 * will be created; the process of registration mentioned before will happen
 * invariably.
 * </p>
 * 
 * @author ElderMael
 * 
 */
public class OpenSessionInViewInsideConversationFilter extends
		OncePerRequestFilter {

	/**
	 * Default cookie name if not specified by init-param
	 */
	public static final String DEFAULT_ACTIVE_CONVERSATION_COOKIE_NAME = "org.mael.hibernate.conversation";

	/**
	 * Request parameter name for the UUID of the conversation.
	 */
	public static final String ACTIVE_CONVERSATION_ATTRIBUTE_NAME = "hibernate.conversation.id";

	/**
	 * Logger to be used by subclasses.
	 */
	protected static final Logger log = LoggerFactory
			.getLogger(OpenSessionInViewInsideConversationFilter.class);

	/**
	 * <p>
	 * The cookie name that will be look up.
	 * </p>
	 * <p>
	 * It can be configured in the deployment descriptor using an init parameter
	 * named after this field as a convenience of subclassing
	 * {@link OncePerRequestFilter}
	 * </p>
	 */
	private String activeConversationCookieName = DEFAULT_ACTIVE_CONVERSATION_COOKIE_NAME;

	/**
	 * Flag to know if this filter should process a request in servlet 3.0 async
	 * dispatches. Defaults to <code>true</code> i.e. it won't process async
	 * dispatches.
	 * 
	 * @see OpenSessionInViewInsideConversationFilter#shouldNotFilterAsyncDispatch()
	 * 
	 */
	private boolean shouldNotFilterAsyncDispatch = true;

	@Override
	protected void initFilterBean() throws ServletException {

	}

	/**
	 * <p>
	 * Will check in the request for a conversation id. If not found, it will
	 * create a new conversation and register it.
	 * </p>
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		UUID conversationId = lookupConversationOrCreateIfNecessary(request,
				response);

		log.debug("Binding conversation '{}' to request '{}'", conversationId,
				request);
		bindConversation(conversationId, request);

		try {
			filterChain.doFilter(request, response);
		} finally {
			log.debug("Unbinding conversation '{}' from request '{}'",
					conversationId, request);
			unbindConversation(conversationId, request);
		}

	}

	/**
	 * Set a request attribute containing the conversation id of the cookie. It
	 * will also register the request to the {@link ThreadedRequestRegistry} so
	 * it can be retrieved by the {@link ConversationalCurrentSessionContext}
	 * can return the {@link Session} corresponding to the conversation.
	 * 
	 * @param conversationId
	 *            - conversation id to bound to the request.
	 * @param request
	 *            - the request processed by this filter to be registered.
	 */
	private void bindConversation(UUID conversationId,
			HttpServletRequest request) {

		request.setAttribute(ACTIVE_CONVERSATION_ATTRIBUTE_NAME, conversationId);

		// Register conversation to be thread-bound
		ThreadedRequestRegistry.setCurrentRequest(request);
	}

	/**
	 * Cleans up the request attribute bound in
	 * {@link OpenSessionInViewInsideConversationFilter#bindConversation(UUID, HttpServletRequest)}
	 * and dereferences the request from the {@link ThreadedRequestRegistry} so
	 * it can be garbage collected.
	 * 
	 * @param conversationId
	 * @param request
	 */
	private void unbindConversation(UUID conversationId,
			HttpServletRequest request) {

		request.removeAttribute(ACTIVE_CONVERSATION_ATTRIBUTE_NAME);

		// Unregister the request to be gc'ed.
		ThreadedRequestRegistry.setCurrentRequest(null);

	}

	/**
	 * Should the filter must process on async dispatches.
	 * 
	 * @see super{@link #shouldNotFilterAsyncDispatch()}
	 */
	@Override
	protected boolean shouldNotFilterAsyncDispatch() {
		return this.shouldNotFilterAsyncDispatch;
	}

	/**
	 * 
	 * Will look for the cookie containing the conversation id. If no cookie is
	 * found, it will create a new conversation and will add a cookie to the
	 * response containing such id.
	 * 
	 * @param request
	 *            - request processed by this filter
	 * @param response
	 *            - response processed by this filter.
	 * @return - the conversation id found in the cookie or a newly created one.
	 */
	private UUID lookupConversationOrCreateIfNecessary(
			HttpServletRequest request, HttpServletResponse response) {
		UUID conversationId = null;

		conversationId = lookupConversationIdOnCookies(request);

		if (conversationId == null) {
			conversationId = createConversationAndStoreCookie(request, response);
		}
		return conversationId;
	}

	/**
	 * Will ask the {@link ConversationManager} for a new {@link UUID} that
	 * represents the conversation id for subsecuent requests. It will store the
	 * UUID (as a string) in a cookie to be added in the response.
	 * 
	 * @param request
	 *            - the request processed by this filter.
	 * @param response
	 *            - the response processed by this filter.
	 * @return a newly created UUID that will serve as a conversation id mapping
	 *         to a Hibernate {@link Session}.
	 */
	private UUID createConversationAndStoreCookie(HttpServletRequest request,
			HttpServletResponse response) {
		UUID conversationId;
		log.debug(
				"No conversation cookie found in request {}, creating new conversation.",
				request);

		conversationId = ConversationManager.createConversation();

		Cookie cookie = new Cookie(this.activeConversationCookieName,
				conversationId.toString());

		cookie.setSecure(true);
		cookie.setMaxAge(-1); // It will expire after browser shut-down

		response.addCookie(cookie);
		return conversationId;
	}

	/**
	 * Retrieves the conversation id (represented by an instance of {@link UUID}
	 * ) from the cookies in the request processed by this filter. If no cookie
	 * named equals to {@link #activeConversationCookieName} is found, it will
	 * return <code>null</code>.
	 * 
	 * @param request
	 *            - the request processed by this filter.
	 * @return the {@link UUID} found in the cookie or null if no cookie is
	 *         found.
	 */
	private UUID lookupConversationIdOnCookies(HttpServletRequest request) {

		if (request.getCookies() == null || request.getCookies().length == 0)
			return null;

		UUID conversationId = null;

		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals(this.activeConversationCookieName)) {
				log.debug("Conversation cookie found in request {}.", request);
				conversationId = UUID.fromString(cookie.getValue());
				break;
			}
		}

		return conversationId;
	}

	/**
	 * Gets the cookie name that will be stored or retrieved by this filter to
	 * to found/store a conversation id.
	 * 
	 * @return the conversation cookie name. Defaults to
	 *         {@link #DEFAULT_ACTIVE_CONVERSATION_COOKIE_NAME}.
	 */
	public String getActiveConversationCookieName() {
		return activeConversationCookieName;
	}

	/**
	 * Sets the name of the cookie that will be stored/retrieved that contains
	 * the conversation id.
	 * 
	 * @param activeConversationCookieName
	 */
	public void setActiveConversationCookieName(
			String activeConversationCookieName) {
		this.activeConversationCookieName = activeConversationCookieName;
	}

	/**
	 * Wheter this filter is processing async dispatches.
	 * 
	 * @return <code>true</code> if this filter is processing async dispatches.
	 *         <code>false</code> otherwise.
	 */
	public boolean isShouldNotFilterAsyncDispatch() {
		return shouldNotFilterAsyncDispatch;
	}

	/**
	 * 
	 * Sets wheter this filter should process async dispatches.
	 * 
	 * @param shouldNotFilterAsyncDispatch
	 *            - <code>true</code> if this filter should process async
	 *            dispatches.
	 */
	public void setShouldNotFilterAsyncDispatch(
			boolean shouldNotFilterAsyncDispatch) {
		this.shouldNotFilterAsyncDispatch = shouldNotFilterAsyncDispatch;
	}

}
