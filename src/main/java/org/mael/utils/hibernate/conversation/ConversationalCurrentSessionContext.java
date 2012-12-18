package org.mael.utils.hibernate.conversation;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.context.spi.CurrentSessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;

public class ConversationalCurrentSessionContext implements
		CurrentSessionContext {

	private static final long serialVersionUID = -2_329_784_674_496_993_600L;

	public ConversationalCurrentSessionContext() {

	}

	public ConversationalCurrentSessionContext(
			SessionFactoryImplementor sessionFactoryImplementor) {
		ConversationManager.setSessionFactory(sessionFactoryImplementor);
	}

	@Override
	public Session currentSession() throws HibernateException {
		return null;
	}

}
