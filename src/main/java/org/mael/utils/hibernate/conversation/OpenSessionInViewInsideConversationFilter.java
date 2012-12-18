package org.mael.utils.hibernate.conversation;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class OpenSessionInViewInsideConversationFilter extends
		OncePerRequestFilter {

	public static final String DEFAULT_ACTIVE_CONVERSATION_COOKIE_NAME = "org.mael.hibernate.conversation";

	public static final String DEFAULT_ACTIVE_CONVERSATION_ATTRIBUTE_NAME = DEFAULT_ACTIVE_CONVERSATION_COOKIE_NAME;

	protected static final Logger log = LoggerFactory
			.getLogger(OpenSessionInViewInsideConversationFilter.class);

	private String activeConversationCookieName = DEFAULT_ACTIVE_CONVERSATION_COOKIE_NAME;

	private String activeConversationParameterName = DEFAULT_ACTIVE_CONVERSATION_ATTRIBUTE_NAME;

	private boolean shouldNotFilterAsyncDispatch = true;

	private ConversationManager conversationManager;

	@Override
	protected void initFilterBean() throws ServletException {

		this.conversationManager = WebApplicationContextUtils
				.getRequiredWebApplicationContext(this.getServletContext())
				.getBean(ConversationManager.class);

	}

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

	private void bindConversation(UUID conversationId,
			HttpServletRequest request) {

		request.setAttribute(DEFAULT_ACTIVE_CONVERSATION_ATTRIBUTE_NAME,
				conversationId);

	}

	private void unbindConversation(UUID conversationId,
			HttpServletRequest request) {

		request.removeAttribute(DEFAULT_ACTIVE_CONVERSATION_ATTRIBUTE_NAME);
	}

	/**
	 * Needed for setup thread locals.
	 * 
	 * @see super{@link #shouldNotFilterAsyncDispatch()}
	 */
	@Override
	protected boolean shouldNotFilterAsyncDispatch() {
		return this.shouldNotFilterAsyncDispatch;
	}

	private UUID lookupConversationOrCreateIfNecessary(
			HttpServletRequest request, HttpServletResponse response) {
		UUID conversationId = null;

		conversationId = lookupConversationIdOnCookies(request);

		if (conversationId == null) {
			conversationId = startNewConversationAndStoreCookie(request,
					response);
		}
		return conversationId;
	}

	private UUID startNewConversationAndStoreCookie(HttpServletRequest request,
			HttpServletResponse response) {
		UUID conversationId;
		log.debug(
				"No conversation cookie found in request {}, creating new conversation.",
				request);

		conversationId = this.conversationManager.createConversation();

		Cookie cookie = new Cookie(this.activeConversationCookieName,
				conversationId.toString());

		cookie.setMaxAge(-1); // It will expire after browser shut-down

		response.addCookie(cookie);
		return conversationId;
	}

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

	public String getActiveConversationCookieName() {
		return activeConversationCookieName;
	}

	public void setActiveConversationCookieName(
			String activeConversationCookieName) {
		this.activeConversationCookieName = activeConversationCookieName;
	}

	public boolean isShouldNotFilterAsyncDispatch() {
		return shouldNotFilterAsyncDispatch;
	}

	public void setShouldNotFilterAsyncDispatch(
			boolean shouldNotFilterAsyncDispatch) {
		this.shouldNotFilterAsyncDispatch = shouldNotFilterAsyncDispatch;
	}

	public ConversationManager getConversationManager() {
		return conversationManager;
	}

	public void setConversationManager(ConversationManager conversationManager) {
		this.conversationManager = conversationManager;
	}

	public String getActiveConversationParameterName() {
		return activeConversationParameterName;
	}

	public void setActiveConversationParameterName(
			String activeConversationParameterName) {
		this.activeConversationParameterName = activeConversationParameterName;
	}

}
