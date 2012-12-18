package org.mael.utils.hibernate.conversation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;

public class ConversationManager {

	private Map<UUID, Session> conversationMap = new HashMap<UUID, Session>();

	private SessionFactoryImplementor sessionFactory;

	public ConversationManager() {

	}

	public UUID createConversation() {
		UUID conversationId = UUID.randomUUID();

		Session sessionForConversation = this.sessionFactory.openSession();

		conversationMap.put(conversationId, sessionForConversation);

		return conversationId;
	}

	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactoryImplementor sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

}
