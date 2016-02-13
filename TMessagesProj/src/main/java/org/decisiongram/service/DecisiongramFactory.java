package org.decisiongram.service;

/**
 * Created by davide on 10/11/15.
 */
public class DecisiongramFactory {

    private static volatile DecisionDAO INSTANCE = null;
    private static volatile DecisionService POLLGRAM_SERVICE_INSTANCE = null;
    private static volatile MessagesManager MESSAGEE_MANAGER_INSTANCE = null;

    // TODO factorize getter code

    public static MessagesManager getMessagesManager() {
        return new MessagesManagerImpl();
//        PollgramMessagesManager localInstance = MESSAGEE_MANAGER_INSTANCE;
//        if (localInstance == null) {
//            synchronized (PollgramMessagesManager.class) {
//                localInstance = MESSAGEE_MANAGER_INSTANCE;
//                if (localInstance == null) {
//                    MESSAGEE_MANAGER_INSTANCE = localInstance = new PollgramMessagesManagerImpl();
//                }
//            }
//        }
//        return localInstance;
    }

    public static DecisionDAO getDAO() {
        return new DecisionDAODBImpl();
//        PollgramDAO localInstance = INSTANCE;
//        if (localInstance == null) {
//            synchronized (PollgramDAO.class) {
//                localInstance = INSTANCE;
//                if (localInstance == null) {
//                    INSTANCE = localInstance = new PollgramDAODBImpl();
//                }
//            }
//        }
//        return localInstance;
    }

    public static DecisionService getService() {
        return new DecisionServiceImpl();
//        PollgramService localInstance = POLLGRAM_SERVICE_INSTANCE;
//        if (localInstance == null) {
//            synchronized (PollgramService.class) {
//                localInstance = POLLGRAM_SERVICE_INSTANCE;
//                if (localInstance == null) {
//                    POLLGRAM_SERVICE_INSTANCE = localInstance = new PollgramServiceImpl();
//                }
//            }
//        }
//        return localInstance;
    }

}
