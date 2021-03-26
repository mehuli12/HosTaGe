package dk.aau.netsec.hostage.persistence;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import dk.aau.netsec.hostage.logging.AttackRecord;
import dk.aau.netsec.hostage.logging.DaoMaster;
import dk.aau.netsec.hostage.logging.DaoSession;
import dk.aau.netsec.hostage.logging.NetworkRecord;
import dk.aau.netsec.hostage.logging.NetworkRecordDao;
import dk.aau.netsec.hostage.persistence.DAO.NetworkRecordDAO;
import dk.aau.netsec.hostage.ui.model.LogFilter;
import dk.aau.netsec.hostage.ui.model.PlotComparisonItem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class NetworkRecordDAOTest {
    private DaoSession daoSession;
    private NetworkRecordDao networkRecordDao; //greenDao

    private NetworkRecordDAO networkRecorDAO; //persistence DAO
    private NetworkRecord record;

    @Before
    public void setUp() {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null);
        Database db = openHelper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        networkRecordDao = daoSession.getNetworkRecordDao();

        networkRecorDAO = new NetworkRecordDAO(this.daoSession);
        record= new NetworkRecord();

    }

    @Test
    public void testBasics() {
        record.setBssid("1");
        record.setTimestampLocation(1);
        daoSession.insert(record);
        assertNotNull(record.getAttack_id());
        assertNotNull(networkRecordDao.load(record.getTimestampLocation()));
        assertEquals(1, networkRecordDao.count());
        assertEquals(1, daoSession.loadAll(NetworkRecord.class).size());

        daoSession.update(record);
        daoSession.delete(record);
        assertNull(networkRecordDao.load(record.getTimestampLocation()));
    }

    @Test
    public void testQueryForCurrentThread() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final Query<NetworkRecord>[] queryHolder = new Query[1];
        new Thread() {
            @Override
            public void run() {
                try {
                    queryHolder[0] = networkRecordDao.queryBuilder().build();
                    queryHolder[0].list();
                } finally {
                    latch.countDown();
                }
            }
        }.start();
        latch.await();
        Query<NetworkRecord> query = queryHolder[0].forCurrentThread();
        query.list();
    }

    @Test
    public void testInsert(){
        record.setBssid("1");
        record.setTimestampLocation(1);
        networkRecorDAO.insert(record);
        assertNotNull(record.getAttack_id());
        assertNotNull(networkRecordDao.load(record.getTimestampLocation()));
    }

    @Test
    public void testGetUniqueESSIDRecords(){
        NetworkRecord recordSecond = new NetworkRecord();
        NetworkRecord recordThird = new NetworkRecord();
        String essid = "dk/aau/netsec/hostage/fragment";
        String essidSecond = "ssid";
        record.setBssid("1");
        record.setSsid(essid);
        recordSecond.setBssid("2");
        recordSecond.setTimestampLocation(1);
        recordSecond.setSsid(essidSecond);
        recordThird.setBssid("3");
        recordThird.setTimestampLocation(2);
        recordThird.setSsid(essidSecond);

        daoSession.insert(record);
        daoSession.insert(recordSecond);
        daoSession.insert(recordThird);

        ArrayList<String> essids = networkRecorDAO.getUniqueESSIDRecords();

        assertNotNull(essids);
        assertEquals(2,essids.size());
        assertEquals(essidSecond,essids.get(0));
        assertEquals(essid,essids.get(1));

    }

    @Test
    public void testGetUniqueBSSIDRecords(){
        NetworkRecord recordSecond = new NetworkRecord();
        NetworkRecord recordThird = new NetworkRecord();

        record.setBssid("1");
        recordSecond.setBssid("2");
        recordSecond.setTimestampLocation(1);
        recordThird.setBssid("3");
        recordThird.setTimestampLocation(2);

        daoSession.insert(record);
        daoSession.insert(recordSecond);
        daoSession.insert(recordThird);

        ArrayList<String> bssids = networkRecorDAO.getUniqueBSSIDRecords();

        assertNotNull(bssids);
        assertEquals(3,bssids.size());
        assertEquals("3",bssids.get(0));
        assertEquals("2",bssids.get(1));
        assertEquals("1",bssids.get(2));

    }

    @Test
    public void testUpdateNetworkInformation(){
        String before = "dk/aau/netsec/hostage/fragment";
        String after = "newTest";
        record.setBssid("1");
        record.setTimestampLocation(1);
        record.setSsid(before);

        daoSession.insert(record);
        assertEquals(before,record.getSsid());
        record.setSsid(after);

        networkRecorDAO.updateNetworkInformation(record);

        assertEquals(after,networkRecordDao.load(record.getTimestampLocation()).getSsid());

    }

    @Test
    public void testGetMissingNetworkRecords(){
        ArrayList<String> otherBSSIDs = new ArrayList<>();
        String other = "dk/aau/netsec/hostage/fragment";
        String secondOther = "other";
        String normal = "normal";
        otherBSSIDs.add(other);
        otherBSSIDs.add(secondOther);

        record.setBssid(normal);
        daoSession.insert(record);
        ArrayList<NetworkRecord> records = networkRecorDAO.getMissingNetworkRecords(otherBSSIDs);

        assertEquals(2,records.size());
        assertEquals(other,records.get(0).getBssid());
        assertEquals(secondOther,records.get(1).getBssid());

    }

    @Test
    public void testSelectionBSSIDFromFilter(){
        LogFilter filter = new LogFilter();
        NetworkRecord networkRecord = new NetworkRecord();
        ArrayList<String> filterBSSIDs = new ArrayList<>();
        String filter1 = "http";
        String filter2 = "smb";
        String filter3 = "random";

        filterBSSIDs.add(filter1);
        filterBSSIDs.add(filter2);
        filterBSSIDs.add(filter3);
        filter.setBSSIDs(filterBSSIDs);
        record.setBssid(filter1);
        networkRecord.setBssid(filter2);
        networkRecord.setTimestampLocation(1);

        daoSession.insert(record);
        daoSession.insert(networkRecord);

        ArrayList<NetworkRecord> records = networkRecorDAO.selectionBSSIDFromFilter(filter,0,50);

        assertEquals(filter1,records.get(0).getBssid());
        assertEquals(filter2,records.get(1).getBssid());
        assertEquals(2,records.size());

    }

    @Test
    public void testSelectionESSIDFromFilter(){
        LogFilter filter = new LogFilter();
        NetworkRecord networkRecord = new NetworkRecord();
        ArrayList<String> filterESSIDs = new ArrayList<>();
        String filter1 = "http";
        String filter2 = "smb";
        String filter3 = "random";

        filterESSIDs.add(filter1);
        filterESSIDs.add(filter2);
        filterESSIDs.add(filter3);
        filter.setESSIDs(filterESSIDs);
        record.setBssid(filter1);
        record.setSsid(filter1);
        networkRecord.setBssid(filter2);
        networkRecord.setSsid(filter2);
        networkRecord.setTimestampLocation(1);

        daoSession.insert(record);
        daoSession.insert(networkRecord);

        ArrayList<NetworkRecord> records = networkRecorDAO.selectionESSIDFromFilter(filter,0,50);

        assertEquals(filter1,records.get(0).getSsid());
        assertEquals(filter2,records.get(1).getSsid());
        assertEquals(2,records.size());

    }

    @Test
    public void testGetUniqueESSIDRecordsForProtocol(){
        String protocol= "http";
        String essid = "dk/aau/netsec/hostage/fragment";

        AttackRecord attackRecord = new AttackRecord();
        attackRecord.setAttack_id(2);
        attackRecord.setProtocol(protocol);

        AttackRecord attackRecord2 = new AttackRecord();
        attackRecord.setAttack_id(3);
        attackRecord2.setProtocol(protocol);

        NetworkRecord networkRecord = new NetworkRecord();
        NetworkRecord networkRecord1 = new NetworkRecord();

        networkRecord.setBssid("1");
        networkRecord.setSsid(essid);
        networkRecord1.setBssid("2");
        networkRecord1.setSsid(essid);

        attackRecord.setRecord(networkRecord);
        attackRecord2.setRecord(networkRecord1);

        daoSession.insert(attackRecord);
        daoSession.insert(attackRecord2);

        ArrayList<String>  essids =networkRecorDAO.getUniqueESSIDRecordsForProtocol(protocol);

        assertNotNull(essids);
        assertEquals(1,essids.size());
        assertEquals(essid,essids.get(0));

    }

    @Test
    public void testGetUniqueBSSIDRecordsForProtocol(){
        String protocol= "http";
        String bssid = "dk/aau/netsec/hostage/fragment";

        AttackRecord attackRecord = new AttackRecord();
        attackRecord.setAttack_id(2);
        attackRecord.setProtocol(protocol);

        AttackRecord attackRecord2 = new AttackRecord();
        attackRecord.setAttack_id(3);
        attackRecord2.setProtocol(protocol);

        NetworkRecord networkRecord = new NetworkRecord();
        NetworkRecord networkRecord1 = new NetworkRecord();

        networkRecord.setBssid(bssid);
        networkRecord.setSsid(bssid);
        networkRecord1.setBssid(bssid);
        networkRecord1.setSsid(bssid);

        attackRecord.setRecord(networkRecord);
        attackRecord2.setRecord(networkRecord1);

        daoSession.insert(attackRecord);
        daoSession.insert(attackRecord2);

        ArrayList<String> bssids =networkRecorDAO.getUniqueBSSIDRecordsForProtocol(protocol);

        assertNotNull(bssids);
        assertEquals(1,bssids.size());
        assertEquals(bssid,bssids.get(0));

    }

    @Test
    public void  attacksPerESSID(){
        String protocol= "http";
        String essid = "dk/aau/netsec/hostage/fragment";

        LogFilter filter = new LogFilter();
        ArrayList<String> protocols = new ArrayList<>();
        protocols.add(protocol);
        filter.setProtocols(protocols);

        AttackRecord attackRecord = new AttackRecord();
        attackRecord.setAttack_id(2);
        attackRecord.setProtocol(protocol);
        record.setBssid("1");
        record.setSsid(essid);
        attackRecord.setRecord(record);

        daoSession.insert(attackRecord);

        ArrayList<PlotComparisonItem> plots = networkRecorDAO.attacksPerESSID(filter);
        assertNotNull(plots);

    }

    @Test
    public void testJoins(){
        AttackRecord attackRecord = new AttackRecord();

        String bssid = "dk/aau/netsec/hostage/fragment";
        String protocol = "protocol";
        String packet = "packet";

        attackRecord.setAttack_id(2);
        attackRecord.setProtocol(protocol);
        attackRecord.setBssid(bssid);
        attackRecord.setTimestampLocation(9);
        record.setBssid(bssid);
        record.setTimestampLocation(9);
        record.setPacket(packet);

        daoSession.insert(attackRecord);
        daoSession.insert(record);

        ArrayList<NetworkRecord> records = networkRecorDAO.joinAttacks(protocol);

        assertEquals(1,records.size());
        assertEquals(bssid,records.get(0).getBssid());
        assertEquals(packet,records.get(0).getPacket());

    }



    @After
    public void breakdown(){
        daoSession.clear();
        record = null;

    }

}
