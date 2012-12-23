package org.mael.utils.hibernate.conversation;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class OsivicFilterTests {

	protected static final Logger log = LoggerFactory
			.getLogger(OsivicFilterTests.class);

	@SuppressWarnings("unused")
	private ApplicationContext context;

	private OpenSessionInViewInsideConversationFilter filter;

	@Before
	public void init() {
		this.context = new ClassPathXmlApplicationContext(
				"org/mael/utils/hibernate/conversation/filter-context.xml");

		this.filter = new OpenSessionInViewInsideConversationFilter();

	}

	@Test
	public void testConversationCookieAndParameterAdded()
			throws ServletException, IOException {

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		NoopAssertingFilterChain chain = new NoopAssertingFilterChain();

		filter.doFilter(request, response, chain);

		verify(response).addCookie(
				new Cookie(filter.getActiveConversationCookieName(),
						anyString()));

		verify(request)
				.setAttribute(
						eq(OpenSessionInViewInsideConversationFilter.ACTIVE_CONVERSATION_ATTRIBUTE_NAME),
						any(UUID.class));

	}

	@Test
	public void testAlreadyHasConversation() throws ServletException,
			IOException {

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		NoopAssertingFilterChain chain = new NoopAssertingFilterChain();

		UUID conversationId = ConversationManager.createConversation();

		Cookie[] fakeCookies = new Cookie[2];

		fakeCookies[0] = new Cookie("JSESSIONID", "9876543216549876543521");
		fakeCookies[1] = new Cookie(filter.getActiveConversationCookieName(),
				conversationId.toString());

		when(request.getCookies()).thenReturn(fakeCookies);

		filter.doFilter(request, response, chain);

		verify(request)
				.setAttribute(
						OpenSessionInViewInsideConversationFilter.ACTIVE_CONVERSATION_ATTRIBUTE_NAME,
						conversationId);

		ConversationManager.endConversation(conversationId);

	}

	public void testOnRequestServing(ServletRequest request,
			ServletResponse response) {
		// Is the request registered for this thread?
		assertEquals(request, ThreadedRequestRegistry.getCurrentThreadRequest());
	}

	public class NoopAssertingFilterChain implements FilterChain {

		private HttpServletRequest request;
		private HttpServletResponse response;

		@Override
		public void doFilter(ServletRequest request, ServletResponse response)
				throws IOException, ServletException {

			this.request = (HttpServletRequest) request;
			this.response = (HttpServletResponse) response;

			testOnRequestServing(request, response);

		}

		public HttpServletResponse getResponse() {
			return response;
		}

		public void setResponse(HttpServletResponse response) {
			this.response = response;
		}

		public HttpServletRequest getRequest() {
			return request;
		}

		public void setRequest(HttpServletRequest request) {
			this.request = request;
		}

	}

}
