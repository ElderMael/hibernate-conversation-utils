package org.mael.utils.hibernate.conversation;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.context.spi.CurrentSessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.SessionFactoryBuilder;

/**
 * Implementation of {@link CurrentSessionContext} that gets a {@link Session}
 * from the {@link ConversationManager} by retrieving the id of the conversation
 * from the thread-bound {@link HttpServletRequest} in the
 * {@link ThreadedRequestRegistry}. //Woot.
 * 
 * @author ElderMael
 * 
 */
public class ConversationalCurrentSessionContext implements
		CurrentSessionContext {

	private static final long serialVersionUID = -2_329_784_674_496_993_600L;

	public ConversationalCurrentSessionContext() {

	}

	/**
	 * Constructor called by the {@link SessionFactoryBuilder}. It sets the
	 * {@link SessionFactoryImplementor} to be used by the
	 * {@link ConversationManager}.
	 * 
	 * TODO: Add a strategy to bind particular instances of this class to a
	 * ConversationManager.
	 * 
	 * @param sessionFactoryImplementor
	 */
	public ConversationalCurrentSessionContext(
			SessionFactoryImplementor sessionFactoryImplementor) {
		ConversationManager.setSessionFactory(sessionFactoryImplementor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Session currentSession() throws HibernateException {
		HttpServletRequest request = lookUpRequest();

		UUID conversationId = (UUID) request
				.getAttribute(OpenSessionInViewInsideConversationFilter.ACTIVE_CONVERSATION_ATTRIBUTE_NAME);

		return ConversationManager.getSessionFromConversation(conversationId);
	}

	/**
	 * Looks up the thread-bound {@link HttpServletRequest} in the
	 * {@link ThreadedRequestRegistry}.
	 * 
	 * @return the {@link HttpServletRequest} bound to the current thread.
	 * 
	 * @throws IllegalStateException
	 *             if no request is bound to the calling thread.
	 */
	private HttpServletRequest lookUpRequest() {

		HttpServletRequest request = ThreadedRequestRegistry
				.getCurrentThreadRequest();

		if (request == null)
			throw new IllegalStateException(
					"Cannot find HttpServletRequest bound to current thread");

		return request;
	}

}
