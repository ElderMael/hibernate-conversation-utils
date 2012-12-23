package org.mael.utils.hibernate.conversation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OsivicFilterTests {

	protected static final Logger log = LoggerFactory
			.getLogger(OsivicFilterTests.class);

	@SuppressWarnings("unused")
	private ApplicationContext context;

	@Before
	public void initApplicationContext() {
		context = new ClassPathXmlApplicationContext(
				"org/mael/utils/hibernate/conversation/filter-context.xml");
	}

	@Test
	public void testConversationCookieAdded() throws ServletException,
			IOException {

		OpenSessionInViewInsideConversationFilter filter = new OpenSessionInViewInsideConversationFilter();

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		NoopFilterChain chain = new NoopFilterChain();

		filter.doFilter(request, response, chain);

		verify(response).addCookie(
				new Cookie(filter.getActiveConversationCookieName(),
						anyString()));

	}

	public class NoopFilterChain implements FilterChain {

		private HttpServletRequest request;
		private HttpServletResponse response;

		@Override
		public void doFilter(ServletRequest request, ServletResponse response)
				throws IOException, ServletException {

			this.request = (HttpServletRequest) request;
			this.response = (HttpServletResponse) response;

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
