package org.decisiongram.service;

/**
 * Created by davide on 10/11/15.
 */
public class DecisiongramFactory {

    private static volatile DecisionDAO INSTANCE = null;
    private static volatile DecisionService DECISIONGRAM_SERVICE_INSTANCE = null;
    private static volatile MessagesManager MESSAGEE_MANAGER_INSTANCE = null;

    // TODO factorize getter code

    public static MessagesManager getMessagesManager() {
        return new MessagesManagerImpl();
//        DecisiongramMessagesManager localInstance = MESSAGEE_MANAGER_INSTANCE;
//        if (localInstance == null) {
//            synchronized (DecisiongramMessagesManager.class) {
//                localInstance = MESSAGEE_MANAGER_INSTANCE;
//                if (localInstance == null) {
//                    MESSAGEE_MANAGER_INSTANCE = localInstance = new DecisiongramMessagesManagerImpl();
//                }
//            }
//        }
//        return localInstance;
    }

    public static DecisionDAO getDAO() {
        return new DecisionDAODBImpl();
//        DecisiongramDAO localInstance = INSTANCE;
//        if (localInstance == null) {
//            synchronized (DecisiongramDAO.class) {
//                localInstance = INSTANCE;
//                if (localInstance == null) {
//                    INSTANCE = localInstance = new DecisiongramDAODBImpl();
//                }
//            }
//        }
//        return localInstance;
    }

    public static DecisionService getService() {
        return new DecisionServiceImpl();
//        DecisiongramService localInstance = DECISIONGRAM_SERVICE_INSTANCE;
//        if (localInstance == null) {
//            synchronized (DecisiongramService.class) {
//                localInstance = DECISIONGRAM_SERVICE_INSTANCE;
//                if (localInstance == null) {
//                    DECISIONGRAM_SERVICE_INSTANCE = localInstance = new DecisiongramServiceImpl();
//                }
//            }
//        }
//        return localInstance;
    }

}
