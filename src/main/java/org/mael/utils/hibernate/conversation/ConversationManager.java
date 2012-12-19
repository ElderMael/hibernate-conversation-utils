package org.mael.utils.hibernate.conversation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;

/**
 * <p>
 * Manages the mapping between conversation ids and {@link Session}s.
 * </p>
 * 
 * <p>
 * Creates or destroys conversations by generating UUIDs and closing Hibernate
 * sessions.
 * </p>
 * 
 * @author ElderMael
 * 
 */
public class ConversationManager {

	private static Map<UUID, Session> conversationMap = new HashMap<UUID, Session>();

	private static SessionFactoryImplementor sessionFactory;

	private ConversationManager() {

	}

	/**
	 * Generates a random {@link UUID} and maps it to a {@link Session} to be
	 * retrieved later.
	 * 
	 * @return - a new random generated {@linkUUID} to be used as a conversation
	 *         id.
	 */
	public static UUID createConversation() {
		UUID conversationId = UUID.randomUUID();

		Session sessionForConversation = sessionFactory.openSession();

		conversationMap.put(conversationId, sessionForConversation);

		return conversationId;
	}

	public static Session getSessionFromConversation(UUID conversationId) {

		return conversationMap.get(conversationId);
	}

	/**
	 * Ends the conversation identified by the {@link UUID} provided. It also
	 * closes the {@link Session} mapped by such id.
	 * 
	 * @see Session#close()
	 * 
	 * @param conversationId
	 */
	public static void endConversation(UUID conversationId) {

		conversationMap.remove(conversationId).close();
	}

	/**
	 * Retrieves the {@link SessionFactoryImplementor} used to generate
	 * {@link Session}s managed by this class.
	 * 
	 * @return
	 */
	public static SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Set the {@link SessionFactoryImplementor} that will be used to create
	 * {@link Session}s for conversations created.
	 * 
	 * @param sessionFactory
	 *            - the {@link SessionFactoryImplementor} to be used to create
	 *            {@link Session}s.
	 */
	public static void setSessionFactory(
			SessionFactoryImplementor sessionFactory) {
		ConversationManager.sessionFactory = sessionFactory;
	}

}
