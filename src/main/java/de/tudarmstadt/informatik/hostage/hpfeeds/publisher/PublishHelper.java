package de.tudarmstadt.informatik.hostage.hpfeeds.publisher;

import java.io.File;
import java.io.IOException;

import de.tudarmstadt.informatik.hostage.HostageApplication;
import de.tudarmstadt.informatik.hostage.commons.JSONHelper;
import de.tudarmstadt.informatik.hostage.logging.DaoSession;
import de.tudarmstadt.informatik.hostage.logging.RecordAll;
import de.tudarmstadt.informatik.hostage.persistence.DAO.DAOHelper;
import de.tudarmstadt.informatik.hostage.ui.model.LogFilter;

public class PublishHelper {
    private DaoSession dbSession;
    private DAOHelper daoHelper;
    private int offset=0;
    private int limit=10;
    private int attackRecordOffset=0;
    private int attackRecordLimit=9999;
    LogFilter filter = null;
    JSONHelper jsonHelper = new JSONHelper();


    public PublishHelper(){
        this.dbSession = HostageApplication.getInstances().getDaoSession();
        this.daoHelper = new DAOHelper(dbSession);
    }

    public void uploadRecordHpfeeds(){
        persistRecord();
        try {
            publisher();
        } catch (Hpfeeds.ReadTimeOutException | Hpfeeds.EOSException | Hpfeeds.InvalidStateException | Hpfeeds.LargeMessageException | IOException e) {
            e.printStackTrace();
        }
    }

    private void persistRecord(){
        jsonHelper.persistData(getLastInsertedRecord());
    }

    private void publisher() throws Hpfeeds.ReadTimeOutException, Hpfeeds.EOSException, Hpfeeds.InvalidStateException, Hpfeeds.LargeMessageException, IOException {
        Publisher publisher = new Publisher();
        String initialConfigurationUrl = jsonHelper.getFilePath();

        publisher.setCommand("192.168.1.3",20000,"testing","secretkey","chan2",initialConfigurationUrl);

        publisher.publishFile();

    }

    private RecordAll getLastInsertedRecord(){
        RecordAll recordAll = daoHelper.getAttackRecordDAO().getRecordsForFilter(filter,offset,limit,attackRecordOffset,attackRecordLimit).get(0);
        return recordAll;
    }
}