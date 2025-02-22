create schema DefSchmea;
create schema XMLSchName;

CREATE TABLE XMLSchName.XMLTableName (id INT NOT NULL, XMLName VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE PKGAO_GAOE (id INT NOT NULL, annotatedEagerName VARCHAR(255), annotatedLazyName VARCHAR(255), annotatedNonUniqueName VARCHAR(255) NOT NULL, annotatedUniqueName VARCHAR(255), lengthBoundString VARCHAR(20), name VARCHAR(255), PRIMARY KEY (id), CONSTRAINT U_PKGO_GO_ANNOTATEDNONUNIQUENAME UNIQUE (annotatedNonUniqueName));
              
CREATE TABLE DefSchmea.EntListTestEntity (id INT NOT NULL, name VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE DefSchmea.MFDEntity1 (id INT NOT NULL, city VARCHAR(255), name VARCHAR(255), state VARCHAR(255), street VARCHAR(255), zip VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE DefSchmea.MFDEntity2 (id INT NOT NULL, city VARCHAR(255), name VARCHAR(255), state VARCHAR(255), street VARCHAR(255), zip VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE DefSchmea.MFDFQEmbedEnt (id INT NOT NULL, name VARCHAR(255), city VARCHAR(255), state VARCHAR(255), street VARCHAR(255), zip VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE DefSchmea.MFDMSC1Ent (id INT NOT NULL, name VARCHAR(255), city VARCHAR(255), state VARCHAR(255), street VARCHAR(255), zip VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE DefSchmea.MFDMSC2Ent (id INT NOT NULL, name VARCHAR(255), city VARCHAR(255), state VARCHAR(255), street VARCHAR(255), zip VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE DefSchmea.MFDNFQEmbedEnt (id INT NOT NULL, name VARCHAR(255), city VARCHAR(255), state VARCHAR(255), street VARCHAR(255), zip VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE DefSchmea.MFDRelationalEntA (id INT NOT NULL, name VARCHAR(255), MANYXONEENTITYB_ID INT, ONEXONEENTITYB_ID INT, PRIMARY KEY (id));
CREATE TABLE DefSchmea.MFDRelationalMMB (id INT NOT NULL, name VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE DefSchmea.MFDRelationalMOB (id INT NOT NULL, name VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE DefSchmea.MFDRelationalOMB (id INT NOT NULL, name VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE DefSchmea.MFDRelationalOOB (id INT NOT NULL, name VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE DefSchmea.PKGMFD_REA_MMEBC_TBL (MFDRelationalEntA INT, MFDRelationalMMB INT);
CREATE TABLE DefSchmea.PKGMFD_REA_OMEBC_TBL (MFDRelationalEntA INT, MFDRelationalOMB INT);
CREATE INDEX I_MFDRLNT_MANYXONEENTITYB ON DefSchmea.MFDRelationalEntA (MANYXONEENTITYB_ID);
CREATE INDEX I_MFDRLNT_ONEXONEENTITYB ON DefSchmea.MFDRelationalEntA (ONEXONEENTITYB_ID);
CREATE INDEX I_PKGMTBL_ELEMENT ON DefSchmea.PKGMFD_REA_MMEBC_TBL (MFDRelationalMMB);
CREATE INDEX I_PKGMTBL_MFDRELATIONALENTA ON DefSchmea.PKGMFD_REA_MMEBC_TBL (MFDRelationalEntA);
CREATE INDEX I_PKGMTBL_ELEMENT1 ON DefSchmea.PKGMFD_REA_OMEBC_TBL (MFDRelationalOMB);
CREATE INDEX I_PKGMTBL_MFDRELATIONALENTA1 ON DefSchmea.PKGMFD_REA_OMEBC_TBL (MFDRelationalEntA);

CREATE TABLE MDCEmbedEntity (id INTEGER NOT NULL, name VARCHAR(30), PRIMARY KEY (id));
CREATE TABLE MDCEntity (id INTEGER NOT NULL, name VARCHAR(30), PRIMARY KEY (id));
CREATE TABLE MDCMSCEntity (id INTEGER NOT NULL, name VARCHAR(30), PRIMARY KEY (id));

CREATE TABLE AnnotationOnlyEntity (id INT NOT NULL, name VARCHAR(12), PRIMARY KEY (id));
CREATE TABLE XMLCompleteTestEntity (id INT NOT NULL, nonOptionalName VARCHAR(255), PRIMARY KEY (id));


