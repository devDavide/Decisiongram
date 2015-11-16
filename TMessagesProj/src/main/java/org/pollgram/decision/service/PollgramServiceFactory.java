package org.pollgram.decision.service;

/**
 * Created by davide on 10/11/15.
 */
public class PollgramServiceFactory {

    private static volatile PollgramDAO INSTANCE = null;
    private static volatile PollgramService POLLGRAM_SERVICE_INSTANCE = null;


    public static PollgramDAO getPollgramDAO() {
        PollgramDAO localInstance = INSTANCE;
        if (localInstance == null) {
            synchronized (PollgramDAO.class) {
                localInstance = INSTANCE;
                if (localInstance == null) {
                    INSTANCE = localInstance = new PollgramDAODBImpl();
                }
            }
        }
        return localInstance;
    }

    public static PollgramService getPollgramService() {
        PollgramService localInstance = POLLGRAM_SERVICE_INSTANCE;
        if (localInstance == null) {
            synchronized (PollgramService.class) {
                localInstance = POLLGRAM_SERVICE_INSTANCE;
                if (localInstance == null) {
                    POLLGRAM_SERVICE_INSTANCE = localInstance = new PollgramServiceImpl();
                }
            }
        }
        return localInstance;
    }

}
