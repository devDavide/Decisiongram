package org.pollgram.decision.service;

/**
 * Created by davide on 10/11/15.
 */
public class PollgramFactory {

    private static volatile PollgramDAO INSTANCE = null;
    private static volatile PollgramService POLLGRAM_SERVICE_INSTANCE = null;
    private static volatile PollgramMessagesManager MESSAGEE_MANAGER_INSTANCE = null;

    // TODO factorize getter code

    public static PollgramMessagesManager getMessagesManager() {
        return new PollgramMessagesManagerImpl();
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

    public static PollgramDAO getDAO() {
        return new PollgramDAODBImpl();
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

    public static PollgramService getService() {
        return new PollgramServiceImpl();
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
