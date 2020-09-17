package com.company.statload.service;

import com.company.statload.entity.*;
import com.esotericsoftware.minlog.Log;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.DataManager;
import org.apache.poi.hpsf.Decimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Service(DBStatSyncService.NAME)
public class DBStatSyncServiceBean implements DBStatSyncService {
    private String status;
    private static Logger log2 = LoggerFactory.getLogger(DBStatSyncServiceBean.class);
    @Inject
    private Persistence persistence;
    @Inject
    private DataManager dataManager;

    @Override
    public String FullSync(){
        setStatus("");
        setStatus(FullSyncStatPokaz());
        setStatus(getStatus() + "\n"+FullSyncStatForm());
        setStatus(getStatus() + "\n"+FullSyncStatSprav());
        setStatus(getStatus() + "\n"+FullSyncStatBanks());
        return getStatus();
    }

    @Override
    public String FullSyncStatPokaz(){
        int i = 0;
        try (Transaction tx = persistence.createTransaction()) {

            Transaction tx2 = persistence.getTransaction("dbstat");

            EntityManager entityManager = persistence.getEntityManager("dbstat");

            persistence.getEntityManager().createNativeQuery("update STATLOAD_VAR set id_statpokaz = null")
                    .executeUpdate();

            persistence.getEntityManager().createNativeQuery("delete from STATLOAD_STAT_POKAZ")
                    .executeUpdate();

            List l = entityManager.createNativeQuery("select id,code_pokaz,id_Form,name_pokaz from stat.s_pokaz where sysdate between dat_beg and nvl(dat_end,to_Date('01.01.4700','dd.mm.yyyy')) and not code_pokaz is null and id_form<>0")
                    .getResultList();
            tx2.commit();
            BigDecimal id_form = null;
            BigDecimal id_pokaz = null;
            String code_pokaz = null;
            String name = "";
            for (Iterator it = l.iterator(); it.hasNext(); ) {
                Object[] row = (Object[]) it.next();
                id_pokaz = (BigDecimal) row[0];
                code_pokaz = (String) row[1];
                id_form = (BigDecimal) row[2];
                name = (String) row[3];
                StatPokaz F = new StatPokaz();
                F.setId_form(id_form.longValue());
                F.setId_pokaz(id_pokaz.longValue());
                F.setCode(code_pokaz);
                if (name!=null)
                  if (code_pokaz.length()+name.length()<=95)
                      F.setFname(code_pokaz+"("+name+")");
                  else
                    F.setFname(code_pokaz+"("+name.substring(0,100-code_pokaz.length()-5)+"...)");

                else
                    F.setFname(code_pokaz);
                persistence.getEntityManager().persist(F);
                i = i + 1;
            }
            tx.commit();
            return "Справочник показателей обновился успешно. Количество обновленных строк " + i;
        }
    }

    @Override
    public String FullSyncStatForm(){
        int i = 0;
        try (Transaction tx = persistence.createTransaction()) {

            Transaction tx2 = persistence.getTransaction("dbstat");

            EntityManager entityManager = persistence.getEntityManager("dbstat");

            persistence.getEntityManager().createNativeQuery("update STATLOAD_REPORT set id_stat = null")
                    .executeUpdate();

            persistence.getEntityManager().createNativeQuery("delete from STATLOAD_STAT_FORM")
                    .executeUpdate();

            List l = entityManager.createNativeQuery("select id,name,name_form from stat.s_forms where sysdate between dat_beg and nvl(dat_end,to_Date('01.01.4700','dd.mm.yyyy'))")
                    .getResultList();
            tx2.commit();
            BigDecimal id_form = null;
            String name = null;
            String name_form = null;
            for (Iterator it = l.iterator(); it.hasNext(); ) {
                Object[] row = (Object[]) it.next();
                id_form = (BigDecimal) row[0];
                name = (String) row[1];
                name_form = (String) row[2];
                StatForm F = new StatForm();
                F.setId_form(id_form.longValue());
                F.setShort_name(name);
                F.setLong_name(name_form);
                persistence.getEntityManager().persist(F);
                i = i + 1;
            }
            tx.commit();
            return "Справочник форм обновился успешно. Количество обновленных строк " + i;
        }
    }


    @Override
    public String FullSyncStatSprav(){
        int i = 0;
        try (Transaction tx = persistence.createTransaction()) {

            Transaction tx2 = persistence.getTransaction("dbstat");

            EntityManager entityManager = persistence.getEntityManager("dbstat");

            persistence.getEntityManager().createNativeQuery("update STATLOAD_VAR set id_sprav = null")
                    .executeUpdate();

            persistence.getEntityManager().createNativeQuery("delete from STATLOAD_STAT_SPRAV")
                    .executeUpdate();

            List l = entityManager.createNativeQuery("select id,a.owner||'.'||name_table name_table  from stat.S_NAME_TABLES t, all_tables a \n" +
                    "where a.table_name=upper(t.name_table)")
                    .getResultList();
            tx2.commit();
            BigDecimal id_ = null;
            String name_table = null;
            for (Iterator it = l.iterator(); it.hasNext(); ) {
                Object[] row = (Object[]) it.next();
                id_ = (BigDecimal) row[0];
                name_table = (String) row[1];
                StatSprav F = new StatSprav();
                F.setId_table(id_.longValue());
                F.setNameTable(name_table.toUpperCase());
                persistence.getEntityManager().persist(F);
                i = i + 1;
                }
            tx.commit();
            return "Справочник справочников обновился успешно. Количество обновленных строк " + i;
        }
    }

    @Override
    public String FullSyncStatBanks(){
        int i = 0;
        try (Transaction tx = persistence.createTransaction()) {

            Transaction tx2 = persistence.getTransaction("dbstat");

            EntityManager entityManager = persistence.getEntityManager("dbstat");


            persistence.getEntityManager().createNativeQuery("delete from STATLOAD_STAT_BANK_HISTORY")
                    .executeUpdate();

            List l = entityManager.createNativeQuery("select name,mfo,id_bank,city region from stat.BANK_HISTORY where (id_bank,date_begin) in (select id_bank,max(date_begin) from stat.BANK_HISTORY where date_begin<=sysdate group by id_bank) \n"+
                                                     "and date_close is null and not city is null")
                    .getResultList();
            tx2.commit();
            String name = null;
            String mfo = null;
            BigDecimal idbank = null;
            BigDecimal region = null;
            for (Iterator it = l.iterator(); it.hasNext(); ) {
                Object[] row = (Object[]) it.next();
                name = (String) row[0];
                mfo = (String) row[1];
                idbank = (BigDecimal) row[2];
                region = (BigDecimal) row[3];
                StatBankHistory F = new StatBankHistory();
                F.setName(name);
                F.setMfo(mfo);
                F.setId_filial(idbank.longValue());
                F.setRegion(region.longValue());
                persistence.getEntityManager().persist(F);
                i = i + 1;

            }
            tx.commit();
            return "Справочник банков обновился успешно. Количество обновленных строк " + i;
        }
    }


    @Override
    public String Sync(){
        setStatus("");
        setStatus(SyncStatPokaz());
        setStatus(getStatus() + "\n"+SyncStatForm());
        setStatus(getStatus() + "\n"+SyncStatSprav());
        setStatus(getStatus() + "\n"+SyncStatBanks());
        return getStatus();
    }
    @Override
    public String SyncStatPokaz(){
        int i = 0;
        try (Transaction tx = persistence.createTransaction()) {

            Transaction tx2 = persistence.getTransaction("dbstat");

            EntityManager entityManager = persistence.getEntityManager("dbstat");

            List l = entityManager.createNativeQuery("select id,code_pokaz,id_Form,name_pokaz from stat.s_pokaz where sysdate between dat_beg and nvl(dat_end,to_Date('01.01.4700','dd.mm.yyyy')) and not code_pokaz is null and id_form<>0")
                    .getResultList();
            tx2.commit();
            BigDecimal id_form = null;
            BigDecimal id_pokaz = null;
            String code_pokaz = null;
            String name = "";
            for (Iterator it = l.iterator(); it.hasNext(); ) {
                Object[] row = (Object[]) it.next();
                id_pokaz = (BigDecimal) row[0];
                code_pokaz = (String) row[1];
                id_form = (BigDecimal) row[2];
                name = (String) row[3];
                StatPokaz F = new StatPokaz();
                F.setId_form(id_form.longValue());
                F.setId_pokaz(id_pokaz.longValue());
                F.setCode(code_pokaz);
                if (name!=null)
                    if (code_pokaz.length()+name.length()<=95)
                        F.setFname(code_pokaz+"("+name+")");
                    else
                        F.setFname(code_pokaz+"("+name.substring(0,100-code_pokaz.length()-5)+"...)");

                else
                    F.setFname(code_pokaz);
                //StatPokaz F_old = persistence.getEntityManager().find(StatPokaz.class,F.getId_pokaz());
                StatPokaz F_old = null;
                try {
                    F_old = dataManager
                            .load(StatPokaz.class)
                            .view("_local")
                            .query("e.id_pokaz=:id")

                            .parameter("id", F.getId_pokaz())
                            .one();

                } catch (Exception e) {
                    F_old = null;
                }
                if (F_old==null) {
                    persistence.getEntityManager().persist(F);
                    i = i + 1;
                }
            }

            tx.commit();
            return "Справочник показателей обновился успешно. Количество импортированных строк " + i;
        }
    }

    @Override
    public String SyncStatForm(){
        int i = 0;
        try (Transaction tx = persistence.createTransaction()) {

            Transaction tx2 = persistence.getTransaction("dbstat");

            EntityManager entityManager = persistence.getEntityManager("dbstat");

            List l = entityManager.createNativeQuery("select id,name,name_form from stat.s_forms where sysdate between dat_beg and nvl(dat_end,to_Date('01.01.4700','dd.mm.yyyy'))")
                    .getResultList();
            tx2.commit();
            BigDecimal id_form = null;
            String name = null;
            String name_form = null;
            for (Iterator it = l.iterator(); it.hasNext(); ) {
                Object[] row = (Object[]) it.next();
                id_form = (BigDecimal) row[0];
                name = (String) row[1];
                name_form = (String) row[2];
                StatForm F = new StatForm();
                F.setId_form(id_form.longValue());
                F.setShort_name(name);
                F.setLong_name(name_form);
                StatForm F_old = null;
                try {
                    F_old = dataManager
                            .load(StatForm.class)
                            .view("_local")
                            .query("e.id_form=:id")

                            .parameter("id", F.getId_form())
                            .one();
                } catch (Exception e) {
                    F_old = null;
                }
                if (F_old==null) {
                    persistence.getEntityManager().persist(F);
                    i = i + 1;
                }
            }
            tx.commit();
            return "Справочник форм обновился успешно. Количество импортированных строк " + i;
        }
    }


    @Override
    public String SyncStatSprav(){
        int i = 0;
        try (Transaction tx = persistence.createTransaction()) {

            Transaction tx2 = persistence.getTransaction("dbstat");

            EntityManager entityManager = persistence.getEntityManager("dbstat");

            List l = entityManager.createNativeQuery("select id,a.owner||'.'||a.table_name name_table  from stat.S_NAME_TABLES t, all_tables a \n" +
                    "where a.table_name=upper(t.name_table)")
                    .getResultList();
            tx2.commit();
            BigDecimal id_ = null;
            String name_table = null;

            for (Iterator it = l.iterator(); it.hasNext(); ) {
                Object[] row = (Object[]) it.next();
                id_ = (BigDecimal) row[0];
                name_table = (String) row[1];
                StatSprav F = new StatSprav();
                F.setId_table(id_.longValue());
                F.setNameTable(name_table.toUpperCase());
               // log2.info(id_+" , "+name_table);
                StatSprav F_old = null;
                try {
                    F_old = dataManager
                            .load(StatSprav.class)
                            .view("_local")
                            .query("e.id_table=:id")

                            .parameter("id", F.getId_table())
                            .one();
                } catch (Exception e) {
                    F_old = null;
                }
                if (F_old==null) {
                    persistence.getEntityManager().persist(F);
                    i = i + 1;
                }
            }
            tx.commit();
            return "Справочник справочников обновился успешно. Количество импортированных строк " + i;
        }
    }

    @Override
    public String SyncStatBanks(){
        int i = 0;
        try (Transaction tx = persistence.createTransaction()) {

            Transaction tx2 = persistence.getTransaction("dbstat");

            EntityManager entityManager = persistence.getEntityManager("dbstat");


           persistence.getEntityManager().createNativeQuery("delete from STATLOAD_STAT_BANK_HISTORY")
                    .executeUpdate();

            List l = entityManager.createNativeQuery("select name,mfo,id_bank,city region from stat.BANK_HISTORY where (id_bank,date_begin) in (select id_bank,max(date_begin) from stat.BANK_HISTORY where date_begin<=sysdate group by id_bank) \n"+
                    "and date_close is null and not city is null")
                    .getResultList();
            tx2.commit();
            String name = null;
            String mfo = null;
            BigDecimal idbank = null;
            BigDecimal region = null;
            for (Iterator it = l.iterator(); it.hasNext(); ) {
                Object[] row = (Object[]) it.next();
                name = (String) row[0];
                mfo = (String) row[1];
                idbank = (BigDecimal) row[2];
                region = (BigDecimal) row[3];
                StatBankHistory F = new StatBankHistory();
                F.setName(name);
                F.setMfo(mfo);
                F.setId_filial(idbank.longValue());
                F.setRegion(region.longValue());
                persistence.getEntityManager().persist(F);
                i = i + 1;

            }
            tx.commit();
            return "Справочник банков обновился успешно. Количество обновленных строк " + i;
        }
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}