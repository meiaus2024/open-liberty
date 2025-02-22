/*******************************************************************************
 * Copyright (c) 2014, 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.tx.jta.ut.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

public class LastingXAResourceImpl extends XAResourceImpl {

    /**  */
    private static final long serialVersionUID = -620973956139953529L;

    public static final String STATE_FILE_ROOT = "CloudResources.dat";

    protected static boolean _attemptedStateLoad;
    protected static int _numberloadedResources;
    protected static int _recoveredResourceCount;
    protected static boolean _attemptedFileInit;

    public static boolean STORE_STATE_IN_DATABASE = true;
    public static boolean INTERRUPT_IN_RECOVERY;
    private static dbStore _dbStore;

    static {
        stateKeeper = new LastingStateKeeperImpl();
    }
  
    public static LastingXAResourceImpl getLastingXAResourceImpl(int i) {
        return new LastingXAResourceImpl(i);
    }
    
    public LastingXAResourceImpl(int i) {
        super(i);

        if (STORE_STATE_IN_DATABASE) {
            if (_dbStore == null) {
                try {
					_dbStore = new dbStore();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        } else if (!_attemptedFileInit) {
            // Storing state data in a file
            _attemptedFileInit = true;

            resetFile();
        }
    }

    /**
     *
     */
    private void resetFile() {}

    public LastingXAResourceImpl(String i) throws Exception {
        super(i);
        if (STORE_STATE_IN_DATABASE) {
            if (_dbStore == null) {
                _dbStore = new dbStore();
                _dbStore.clear();
            }
        } else if (!_attemptedFileInit) {
            // Storing state data in a file
            _attemptedFileInit = true;

            resetFile();
        }
    }

    public LastingXAResourceImpl() {
        super();

        if (STORE_STATE_IN_DATABASE) {
            if (_dbStore == null) {
                try {
					_dbStore = new dbStore();
	                _dbStore.clear();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        } else if (!_attemptedFileInit) {
            // Storing state data in a file
            _attemptedFileInit = true;

            resetFile();
        }
    }

	@Override
    public Xid[] recover(int flag) throws XAException {
        Xid[] theXids = null;
        try {
            if (DEBUG_OUTPUT)
                System.out.println("recover entry");
            if (STORE_STATE_IN_DATABASE) {
                if (_dbStore == null) {
                    _dbStore = new dbStore();
                }
                if (!_attemptedStateLoad) {
                    if (DEBUG_OUTPUT)
                        System.out.println("recover doing state load");
                    _attemptedStateLoad = true;
                    // Use this class' version of loadState()
                    _numberloadedResources = loadState();
                    _recoveredResourceCount = 0;
                }
            } else {
                if (!_attemptedStateLoad) {
                    _attemptedStateLoad = true;

                    _numberloadedResources = super.loadState();
                    _recoveredResourceCount = 0;
                }
            }

            theXids = super.recover(flag);
            _recoveredResourceCount++;
            if (DEBUG_OUTPUT)
                System.out.println("recover resource count is " + _recoveredResourceCount);

            // If the INTERRUPT_IN_RECOVERY flag has been set, simulate the re-acquisition
            // of its logs by the home server
            if(INTERRUPT_IN_RECOVERY)
            {
            	dbStore.updateLogOwnership();
            }

            if (_recoveredResourceCount >= _numberloadedResources) {
                if (DEBUG_OUTPUT)
                    System.out.println("recover: Final call to recover");
                // This is the final call to recover() so reset the flag to allow resources to
                // be reloaded.
                _attemptedStateLoad = false;
                if (STORE_STATE_IN_DATABASE) {
                    _dbStore.clear();
                } else if (!_attemptedFileInit) {
                    _attemptedFileInit = true;

                    resetFile();
                }
            }
        } catch (Throwable t) {
            XAException xae = new XAException();
            xae.errorCode = XAException.XAER_RMFAIL;
            xae.initCause(t);
            xae.printStackTrace();
            throw xae;
        }
        return theXids;
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {

        super.commit(xid, onePhase);
        if (DEBUG_OUTPUT)
            System.out.println("commit - clean up old resource for key: " + _key);
        _resources.remove(_key);

    }

    @Override
    public void rollback(Xid xid) throws XAException {

        super.rollback(xid);
        if (DEBUG_OUTPUT)
            System.out.println("rollback - clean up old resource for key: " + _key);
        _resources.remove(_key);
    }

    static class LastingStateKeeperImpl implements StateKeeper {

    	@Override
        public void dumpState(boolean quietly) {
        	dumped = !quietly; // For some tests (e.g. XAFlow) we need to continue after dumping state.
            if (STORE_STATE_IN_DATABASE) {
                System.out.println("Dumping state to database");
                // Defect 168553 - this string needs to be written in order for the test infrastructure to see that the
                // server has been halted.
                System.out.println("Dump State: " + _resources.values().size());
                dbStore.putXAResources();
            } else {
                XAResourceImpl.dumpState();
            }
        }

        @Override
        public int loadState() {
            int resourceCount = 0;
            if (STORE_STATE_IN_DATABASE) {
                resourceCount = dbStore.getXAResources();
            } else {
                resourceCount = XAResourceImpl.loadState();
            }

            return resourceCount;
        }
    }

    public static class dbStore {
        static DataSource _theDS = null;

        public dbStore() throws Exception {
            init();
        }

        public void init() throws Exception {
            Connection con1 = null;
            try {
                // Do the lookups on the DS
                if (_theDS == null)
                    _theDS = lookupDataSource();

                // Get connection to database via datasource
                con1 = _theDS.getConnection();
                if (DEBUG_OUTPUT)
                    System.out.println("init Got connection: " + con1);
                DatabaseMetaData mdata = con1.getMetaData();
                if (DEBUG_OUTPUT)
                    System.out.println("init Got metadata: " + mdata);
                String dbName = mdata.getDatabaseProductName();
                String dbVersion = mdata.getDatabaseProductVersion();
                if (DEBUG_OUTPUT)
                    System.out.println("init is now connected to " + dbName + ", version " + dbVersion);

                // Execute a Query against first connection, to see if we need to create a table
                System.out.println("create a statement");
                Statement stmtBasic = con1.createStatement();
                try {
                    if (DEBUG_OUTPUT)
                        System.out.println("Execute a query to determine if we need to create a table");
                    stmtBasic.executeQuery("SELECT RESOURCE_ID, DATA" +
                                           " FROM WAS_XA_RESOURCES" +
                                           " WHERE RESOURCE_ID='1'");
                } catch (Exception e) {
                    if (DEBUG_OUTPUT)
                        System.out.println("couldn't find the table ... so create it");
                    // couldn't find the table ... so create it
                    Statement stmt2 = con1.createStatement();

                    stmt2.executeUpdate("CREATE TABLE WAS_XA_RESOURCES( " +
                                        "RESOURCE_ID VARCHAR(60), " +
                                        "DATA LONG VARCHAR FOR BIT DATA) ");
                    if (DEBUG_OUTPUT)
                        System.out.println("Have created the table");

                    stmt2.close();
                    con1.commit();
                }

            } finally {
                if (con1 != null) {
                    try {
                        if (DEBUG_OUTPUT)
                            System.out.println("Drive COMMIT processing");
                        con1.commit();
                    } catch (Exception e) {
                        System.out.println("Exception thrown when committing: " + e);
                    }
                }
            }
        }

        public static int getXAResources() {
            Connection con1 = null;
            Statement stmtBasic = null;
            ResultSet rsBasic = null;
            int resourceCount = 0;

            try {
                // Do the lookups on the DS
                if (_theDS == null)
                    _theDS = lookupDataSource();

                // Get connection to database via datasource
                con1 = _theDS.getConnection();
                if (DEBUG_OUTPUT)
                    new Throwable("getXAResources Got connection: " + con1).printStackTrace();;

                // Execute a Query against connection
                stmtBasic = con1.createStatement();

                rsBasic = stmtBasic.executeQuery("SELECT RESOURCE_ID, DATA" +
                                                 " FROM WAS_XA_RESOURCES");
                // Now process through the rows we need to handle
                while (rsBasic.next()) {
                    final String resId = rsBasic.getString(1);
                    final byte[] data = rsBasic.getBytes(2);
                    if (DEBUG_OUTPUT) {
                        System.out.println("Resource Table: read rid: " + resId);
                        System.out.println("Resource Table: read data: " + data);
                    }
                    
                    // Test for presence of special key
                    if(resId.equals("-99")) {
                        if (DEBUG_OUTPUT)
                        	System.out.println("getXAResources found special interrupt key");
                    	INTERRUPT_IN_RECOVERY = true;
                    } else {

                    	ObjectInputStream objectIn = null;
                    	if (data != null)
                    		objectIn = new ObjectInputStream(new ByteArrayInputStream(data));
                    	final XAResourceData xares = (XAResourceData) objectIn.readObject();
                    	System.out.println("_resources.put(\""+resId+"\", "+xares+")");
                    	_resources.put(resId, xares);
                    	resourceCount++;
                    }
                }
                if (DEBUG_OUTPUT)
                    System.out.println("getXAResources Loaded " + resourceCount + " resources");
            } catch (Exception e) {
                System.out.println("getXAResources, Exception thrown when retrieving data: " + e);
                e.printStackTrace();
            } finally {
                try {
                    if (stmtBasic != null)
                        stmtBasic.close();
                    if (con1 != null) {
                        con1.commit();
                        con1.close();
                    }
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    // Do you need FFDC here? Remember FFDC instrumentation and @FFDCIgnore
                    // http://was.pok.ibm.com/xwiki/bin/view/Liberty/LoggingFFDC
                    e.printStackTrace();
                }
            }
            return resourceCount;
        }

        /**
         * Used to simulate the acquisition of the cloud001 logs by the "home server"
         */
        public static void updateLogOwnership() {
            Connection con1 = null;
            Statement stmtBasic = null;

            try {
                // Do the lookups on the DS
                if (_theDS == null)
                    _theDS = lookupDataSource();

                // Get connection to database via datasource
                con1 = _theDS.getConnection();
                if (DEBUG_OUTPUT)
                    new Throwable("updateLogOwnership Got connection: " + con1).printStackTrace();;

                // Statement used to update log ownership
                Statement stmt = con1.createStatement();

                try {
                    System.out.println("updateLogOwnership: sel-for-update against partner log table");
                    String selForUpdateString = "SELECT SERVER_NAME" +
                                                " FROM WAS_PARTNER_LOGcloud001" +
                                                " WHERE RU_ID = -1 FOR UPDATE OF SERVER_NAME";
                    ResultSet rs = stmt.executeQuery(selForUpdateString);
                    while (rs.next()) {
                        String owner = rs.getString("SERVER_NAME");
                        System.out.println("updateLogOwnership: owner is - " + owner);
                    }
                    rs.close();
                    String updateString = "UPDATE " + "WAS_PARTNER_LOGcloud001" +
                                    " SET SERVER_NAME = 'cloud001'" +
                                   " WHERE RU_ID = -1";
                    stmt.executeUpdate(updateString);
                } catch (SQLException x) {
                    System.out.println("updateLogOwnership: caught exception - " + x);
                }

                System.out.println("updateLogOwnership: commit changes to database");
                con1.commit();
            } catch (Exception ex) {
                System.out.println("updateLogOwnership: caught exception " + ex);
            } finally {
                try {
                    if (stmtBasic != null)
                        stmtBasic.close();
                    if (con1 != null) {
                        con1.commit();
                        con1.close();
                    }
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    // Do you need FFDC here? Remember FFDC instrumentation and @FFDCIgnore
                    // http://was.pok.ibm.com/xwiki/bin/view/Liberty/LoggingFFDC
                    e.printStackTrace();
                }
            }
        }

        public void clear() {
            if (DEBUG_OUTPUT)
                System.out.println("clear XAResources table");
            Connection con1 = null;
            Statement stmtBasic = null;
            try {
                // Do the lookups on the DS
                if (_theDS == null)
                    _theDS = lookupDataSource();

                // Get connection to database via datasource
                con1 = _theDS.getConnection();
                if (DEBUG_OUTPUT)
                    System.out.println("clear Got connection: " + con1);

                // Construct the DELETE string
                String deleteString = "DELETE FROM WAS_XA_RESOURCES";
                if (DEBUG_OUTPUT)
                    System.out.println("clear: Delete all rows in  WAS_XA_RESOURCES");

                stmtBasic = con1.createStatement();

                stmtBasic.executeUpdate(deleteString);

            } catch (Exception e) {
                System.out.println("caught Exception in dbStore.clear " + e);

            } finally {
                try {
                    if (stmtBasic != null)
                        stmtBasic.close();
                    if (con1 != null) {
                        con1.commit();
                        con1.close();
                    }
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    // Do you need FFDC here? Remember FFDC instrumentation and @FFDCIgnore
                    // http://was.pok.ibm.com/xwiki/bin/view/Liberty/LoggingFFDC
                    e.printStackTrace();
                }
            }

        }

        /**
         * @param _key
         * @param xaResourceData
         */
        public static void putXAResources() {
            Connection con1 = null;
            PreparedStatement insertStatement = null;

            try {
                // Do the lookups on the DS
                if (_theDS == null)
                    _theDS = lookupDataSource();

                // Get connection to database via datasource
                con1 = _theDS.getConnection();
                if (DEBUG_OUTPUT)
                    System.out.println("putXAResources Got connection: " + con1);

                String insertString = "INSERT INTO WAS_XA_RESOURCES (RESOURCE_ID, DATA)" +
                                      " VALUES (?,?)";

                insertStatement = con1.prepareStatement(insertString);
                if (DEBUG_OUTPUT)
                    System.out.println("putXAResources prepare to insert " + _resources.size() + " resources");
                for (XAResourceData xares : _resources.values()) {
                    String resKey = xares.key;
                    if (DEBUG_OUTPUT) {
                        System.out.println("putXAResources Insert row for key: " + resKey);
                        System.out.println("And data with XID: " + xares.getXids());
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(xares);
                    byte[] data = baos.toByteArray();

                    insertStatement.setString(1, resKey);
                    insertStatement.setBytes(2, data);

                    int ret = insertStatement.executeUpdate();
                    if (DEBUG_OUTPUT)
                        System.out.println("putXAResources Inserted row with return: " + ret);
                }
                
                // If there are recoverable resources and the INTERRUPT_IN_RECOVERY flag has been set,
                // then store a special key in the table
                if(_resources.size() > 0 && INTERRUPT_IN_RECOVERY)
                {
                	if (DEBUG_OUTPUT)
                		System.out.println("putXAResources insert special interrupt key");
                    insertStatement.setInt(1, -99);
                    insertStatement.setBytes(2, null);
                    int ret = insertStatement.executeUpdate();
                    if (DEBUG_OUTPUT)
                        System.out.println("putXAResources Inserted row with return: " + ret);
                }

            } catch (Exception e) {
                System.out.println("putXAResources, Exception thrown when inserting data: " + e);
                e.printStackTrace();
            } finally {
                try {
                    if (insertStatement != null)
                        insertStatement.close();
                    if (con1 != null) {
                        con1.commit();
                        con1.close();
                    }
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    // Do you need FFDC here? Remember FFDC instrumentation and @FFDCIgnore
                    // http://was.pok.ibm.com/xwiki/bin/view/Liberty/LoggingFFDC
                    e.printStackTrace();
                }
            }

        }

        private static DataSource lookupDataSource() throws Exception {
            // Do the lookups on the DS
            final InitialContext ctx = new InitialContext();
            System.out.println("Context is: " + ctx.toString());
            DataSource ds = null;
            try {
                ds = (DataSource) ctx.lookup("java:comp/env/jdbc/tranlogDataSource");
            } catch (javax.naming.NamingException nex) {
                // try alternate lookup
                System.out.println("Try alternate lookup");
                ds = (DataSource) ctx.lookup("jdbc/tranlogDataSource");
            }
            if (DEBUG_OUTPUT)
            	System.out.println("TEST UTIL VERSION, GOT DATASOURCE: " + ds);
            // Do the lookups on the DS
//            if (DEBUG_OUTPUT)
//                System.out.println("INITIALISE DATASOURCE");
//            ResourceFactory nontranDSResourceFactory = ConfigurationProviderManager.getConfigurationProvider().getResourceFactory();
//            if (DEBUG_OUTPUT)
//                System.out.println("Retrieved non tran DS Resource Factory, " + nontranDSResourceFactory);

//            Properties cp = new Properties();
//            CustomLogProperties clp = new CustomLogProperties(0, "XARESLOG", "ID", cp);
//            clp.setResourceFactory(nontranDSResourceFactory);
//            SQLNonTransactionalDataSource sqlNonTranDS = new SQLNonTransactionalDataSource("jdbc/tranlogDataSource", clp);

//            DataSource ds = null;
//            ds = sqlNonTranDS.getDataSource();
            if (DEBUG_OUTPUT)
                System.out.println("HAVE LOOKED UP DATASOURCE " + ds);

            return ds;
        }
    }

	public static LastingXAResourceImpl getLastingXAResourceImpl() {
        return new LastingXAResourceImpl();
	}

    public synchronized static int loadState() {
        final int numResources = stateKeeper.loadState();
        _stateLoaded = true;
        printState();
        return numResources;
    }
}
