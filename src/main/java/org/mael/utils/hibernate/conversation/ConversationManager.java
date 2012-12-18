package org.mael.utils.hibernate.conversation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;

public class ConversationManager {

	private static Map<UUID, Session> conversationMap = new HashMap<UUID, Session>();

	private static SessionFactoryImplementor sessionFactory;

	private ConversationManager() {

	}

	public static UUID createConversation() {
		UUID conversationId = UUID.randomUUID();

		Session sessionForConversation = sessionFactory.openSession();

		conversationMap.put(conversationId, sessionForConversation);

		return conversationId;
	}

	public static Session getSessionFromConversation(UUID conversationId) {

		return conversationMap.get(conversationId);
	}

	public static void endConversation(UUID conversationId) {

		conversationMap.remove(conversationId);
	}

	public static SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	public static void setSessionFactory(
			SessionFactoryImplementor sessionFactory) {
		ConversationManager.sessionFactory = sessionFactory;
	}

}
