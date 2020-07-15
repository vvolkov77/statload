package com.company.statload.service;

import com.company.statload.entity.*;
import com.esotericsoftware.minlog.Log;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.app.FileStorageAPI;
import com.haulmont.cuba.core.app.ServerConfig;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.FileStorageException;

import org.apache.poi.ss.usermodel.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.poi.ss.usermodel.CellType.BLANK;

@Service(LoadFileService.NAME)
public class LoadFileServiceBean implements LoadFileService {
    private static Logger log = LoggerFactory.getLogger(LoadFileServiceBean.class);

    private static String getCellText(Cell cell) {
        String result = "";
        SimpleDateFormat dateForm = new SimpleDateFormat("yyyy.MM.dd");
        switch (cell.getCellType()) {
            case STRING:
                result = cell.getRichStringCellValue().getString();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    result = dateForm.format(cell.getDateCellValue());
                } else {
                    result = Double.toString(cell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                result = Boolean.toString(cell.getBooleanCellValue());
                break;
            case FORMULA:
                result = cell.getCellFormula().toString();
                break;
            case BLANK:
                break;
            default:
                break;
        }
        return result;
    }

    @Inject
    private FileStorageAPI fileStorageAPI;
    @Inject
    private DataManager dataManager;
    @Inject
    private Persistence persistence;


    public void delFile(FileDescriptor fileDescr) throws FileStorageException {
        /*
        Удаляет файл из временного хранилища файлов сервера и из таблицы учета этих файлов
        fileDescr - дескриптор файла во временном хранилище файлов
         */

        EntityManager em;
        try (Transaction tx = persistence.createTransaction()) {
            em = persistence.getEntityManager();
            Query query;

            query = em.createNativeQuery("DELETE FROM public.sys_file where id = ?1");
            query.setParameter(1, fileDescr.getUuid());
            query.executeUpdate();
            tx.commit();

            fileStorageAPI.removeFile(fileDescr);
        }
    }

    @Override
    public void loadtemplate(Report rep, FileDescriptor fdscr) throws FileStorageException {

        Workbook workbook = null;
        if (fileStorageAPI.fileExists(fdscr)) {//

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager entityManager=persistence.getEntityManager();

            entityManager.createNativeQuery(
                    "delete from STATLOAD_VAR where id_report=#rep")
                    .setParameter("rep",rep.getId())
                    .executeUpdate();

            String NamePokaz;
            String ValCell;
            workbook = WorkbookFactory.create(fileStorageAPI.openStream(fdscr));
            Sheet firstSheet = workbook.getSheetAt(0);
            Var U = null;
            for (Row row : firstSheet) {
                for (Cell cell : row) {
                    ValCell=getCellText(cell);
                    log.debug("loadtemplate.Вижу значение ячейки "+ValCell);
                    if ((rep.getVid().getId()==2&&ValCell.indexOf('[')>=0&&ValCell.indexOf(']')>ValCell.indexOf('[')/*==ValCell.length()-1*/)||// Для матричных отчетов показатель ограничивается символами [ ]
                            ((rep.getVid().getId()==1||rep.getVid().getId()==3)&&ValCell.indexOf('{')>=0&&ValCell.indexOf('}')>ValCell.indexOf('{')/*==ValCell.length()-1*/) // Для отчетов переменной длинны показатель ограничивается символами { }
                    ) {
                        // Выделяем название показателя
                        if (rep.getVid().getId()==2)
                           NamePokaz = ValCell.substring(ValCell.indexOf('[')+1,ValCell.indexOf(']'));
                        else
                            NamePokaz = ValCell.substring(ValCell.indexOf('{')+1,ValCell.indexOf('}'));

                        if (!NamePokaz.contains("BANK_NAME")&&!NamePokaz.contains("DATE_BEGIN")&&!NamePokaz.contains("DATE_END")&&
                                !NamePokaz.contains("DIVISION")&&!NamePokaz.contains("CHIEF_POS")&&!NamePokaz.contains("CHIEF")&&!NamePokaz.contains("ACCOUNTANT_POST")&&
                                !NamePokaz.contains("ACCOUNTANT")&&!NamePokaz.contains("NAME_USER")) {
                            // Проверяем на уникальность показателя Оставил для примера
                           /* Var pokaz = dataManager
                                    .load(Var.class)
                                    .view("var-view-browse")
                                    .query("e.code = :code and e.ref_report.id<> :id_rep")
                                    .parameter("code", NamePokaz)
                                    .parameter("id_rep", rep.getId())
                                    .one();*/
                            // Записываем в базу
                            log.debug("loadtemplate.Стал показатель "+NamePokaz+" со строкой "+(cell.getRowIndex() + 1)+ " и столбцом "+cell.getColumnIndex() + 1);
                                U = new Var();
                                U.setNrow(cell.getRowIndex() + 1);
                                U.setNcol(cell.getColumnIndex() + 1);
                                U.setCode(NamePokaz);
                                U.setRef_report(rep);
                                entityManager.persist(U);
                         }
                    }

                }

            }
            workbook.close();
            tx.commit();
        } catch (FileStorageException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (workbook!=null) {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            delFile(fdscr);
        } catch (Exception e) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, "File storage не смог удалить файл " + fdscr.getId());
        }
        } else {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, "File storage не содержит шаблон отчета с названием " + fdscr.getId());
        }
    }

    @Override
    public void loadmaping(Report rep, FileDescriptor fdscr) throws FileStorageException {
        /*
        Загрузка файла с соответствием кода показателя модуля и кода показателя Статистики
        rep - отчет, для которого загружается соответствие
        fdscr - описатель файла во временно хранилище файлов
         */

        Workbook workbook = null;
        if (fileStorageAPI.fileExists(fdscr)) {//

            try (Transaction tx = persistence.createTransaction()) {

                String ValCell;
                workbook = WorkbookFactory.create(fileStorageAPI.openStream(fdscr));
                Sheet firstSheet = workbook.getSheetAt(0);

                String mod_var = "";
                String mod_stat = "";
                BigDecimal id_var_mod = null;
                Var v = null;
                StatPokaz v_stat = null;
                StatSprav v_sprav = null;
                String field_key= "";
                String  field_res="";

                for (Row row : firstSheet) {
                    mod_var = "";
                    mod_stat = "";
                    for (Cell cell : row) {
                        ValCell = getCellText(cell);
                        if (cell.getColumnIndex()==0) mod_var = ValCell;
                        if (cell.getColumnIndex()==1) {mod_stat = ValCell; break;}
                    }
                    try {
                        v = dataManager
                                .load(Var.class)
                                .view("map-var-view")
                                .query("e.ref_report=:rep and e.code = :code ")
                                .parameter("rep", rep)
                                .parameter("code", mod_var)
                                .one();
                    } catch(Exception e) {
                        v = null;
                    }
                    try {
                        v_stat = dataManager
                                .load(StatPokaz.class)
                                .view("_local")
                                .query("e.code = :code ")

                                .parameter("code", mod_stat)
                                .one();
                    } catch(Exception e){
                        v_stat = null;
                    }

                    if (v!=null&v_stat!=null&&v.getRef_report().getRef_stat_form_id().getId_form()==v_stat.getId_form()){
                        int sprav = getidsprav(v_stat.getId_pokaz(), v_stat.getId_form(), rep.getStat_schema().getId());
                        if (sprav != -1) {
                            field_key = "";
                            field_res = "";
                            v_sprav = dataManager
                                    .load(StatSprav.class)
                                    .view("_local")
                                    .query("e.id_table = :id ")

                                    .parameter("id", sprav)
                                    .one();
                              if (fieldisvalid("ID", v_sprav.getNameTable())) field_key = "ID";
                              if (fieldisvalid("CODE", v_sprav.getNameTable())) field_res = "CODE";

                            persistence.getEntityManager().createNativeQuery("update statload_Var set ID_STATPOKAZ=?ref,stat_key_field=?key,stat_res_field=?res,id_sprav=?sprav where id=?var")
                                    .setParameter("ref",v_stat.getId())
                                    .setParameter("key",field_key)
                                    .setParameter("res",field_res)

                                    .setParameter("sprav",v_sprav.getId())
                                    .setParameter("var",v.getId())
                                    .executeUpdate();

                        } else{
                            persistence.getEntityManager().createNativeQuery("update statload_Var set ID_STATPOKAZ=?ref where id=?var")
                                    .setParameter("ref",v_stat.getId())
                                    .setParameter("var",v.getId())
                                    .executeUpdate();

                        }

                    }
                }
              tx.commit();
                try {
                    delFile(fdscr);
                } catch (Exception e) {
                    throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, "File storage не смог удалить файл " + fdscr.getId());
                }
            } catch (IOException e) {
                throw new java.lang.Error(e.getStackTrace().toString());
            }
        }

    }

    @Override
    public boolean fieldisvalid(String field, String sprav){
/*
   Проверка наличия поля в справочнике
   - field название поля в таблице
   - sprav название таблицы в формате <схема>.<таблица>
   Нужны права доступа к представлению all_tab_cols
 */
        try (Transaction tx = persistence.createTransaction("dbstat")) {

            EntityManager entityManager = persistence.getEntityManager("dbstat");

            //   отделяем название справочника от схемы. если не удается, то выдаем ошибку
            String s = "";
            String tab = "";
            if (sprav.indexOf(".")==-1) throw new java.lang.Error("Не соответствие названия "+sprav+" формату <схема>.<таблица>");
            s = sprav.substring(0, sprav.indexOf("."));
            tab = sprav.substring(sprav.indexOf(".")+1);



            BigDecimal l = (BigDecimal) entityManager.createNativeQuery("select count(*) from all_tab_cols where table_name=?tab and owner=?sch and COLUMN_NAME = ?field"
            )
                    .setParameter("tab", tab)
                    .setParameter("sch", s)
                    .setParameter("field", field)
                    .getFirstResult();

            tx.commit();

            if (l.intValue() == 1) return true;
            else return false;

        }

    }

    @Override
    public int getidsprav(Long id_stat_pokaz, Long id_stat_form, String schema){
        List l;
        BigDecimal id_table = null;

        try (Transaction tx = persistence.createTransaction("dbstat")) {

            EntityManager entityManager = persistence.getEntityManager("dbstat");

            if (schema.equals("DIS")) {
                l = entityManager.createNativeQuery("select distinct dp.id_name_tables from dis.det_dis_property dp,dis.det_dis d\n" +
                        "where d.id_form= ?form and d.id_pokaz=?pkaz and d_report>=trunc(sysdate,'yy')-1 \n" +
                        "  and dp.id_det=d.id "
                )
                        .setParameter("form", id_stat_form)
                        .setParameter("pkaz", id_stat_pokaz)
                        .getResultList();
            } else if (schema.equals("STAT")) {
                l = entityManager.createNativeQuery("select distinct dp.id_name_tables from stat.detail_property dp,stat.detail d \n" +
                        "where d.id_form= ?form and d.id_pokaz=?pkaz and d_report>=trunc(sysdate,'yy')-1 \n" +
                        "  and dp.id_detail=d.id and not dp.id_name_tables is null"
                )
                        .setParameter("form", id_stat_form)
                        .setParameter("pkaz", id_stat_pokaz)
                        .getResultList();
            } else {
                throw new java.lang.Error("Нет обработки для схемы " + schema);
            }
            tx.commit();
        }
        int cnt= 0;

        for (Iterator it = l.iterator(); it.hasNext(); ) {
            //Object[] row = (Object[]) it.next();
            id_table = (BigDecimal) it.next();
            cnt++;
        }
         if (cnt == 1&&id_table!=null) return id_table.intValue();
         else return -1;


    }

    @Override
    public int checkstat(Date pdate, Report rep){
        /*
         Проверка наличия отчета за дату в Статистике
         Возвращает количество отчетов
         */

        try (Transaction tx = persistence.getTransaction("dbstat")) {
            EntityManager entityManager = persistence.getEntityManager("dbstat");

            BigDecimal i = (BigDecimal) entityManager.createNativeQuery("select count(*) from stat.rep_file where d_Report=#dat and id_form=#form")
                          .setParameter("dat", pdate)
                          .setParameter("form", rep.getRef_stat_form_id().getId_form())
                          .getFirstResult();
            tx.commit();
            return i.intValue();
        }
    }
    @Override
    public void copystat(Date fromdate, Date todate, Report rep){
        /*
        Копирует отчет Статистики с одной даты на другую с указанием идентификаторов показателей вместо сумм
        Предварительно надо в копируемом отчете заполнить все значения. Не должно быть пустых значений
        fromdate - за какую дату брать исходный отчет
        todate   - дата записи нового отчета
         */
        log.debug("Начало copystat");
        try (Transaction tx = persistence.getTransaction("dbstat")) {

            EntityManager entityManager = persistence.getEntityManager("dbstat");
            //Удаляем старый отчет
            entityManager.createNativeQuery("delete from stat.rep_file where d_report=#dt and id_form=#form")
                    .setParameter("dt",todate)
                    .setParameter("form",rep.getRef_stat_form_id().getId_form())
                    .executeUpdate();

            entityManager.createNativeQuery("insert into stat.rep_file(id,d_report,d_create,status,id_form,zo)\n" +
                    "(select stat.sqn_rep_file.nextval id,#drep2 d_report,sysdate d_create, '1' status,id_form,zo \n" +
                    " from stat.rep_file where d_report=#drep1 and id_form=#id_form)")
                    .setParameter("drep1", fromdate)
                    .setParameter("drep2", todate)
                    .setParameter("id_form", rep.getRef_stat_form_id().getId_form())
                    .executeUpdate();
            if (rep.getStat_schema().getId().equals("DIS")) {
                entityManager.createNativeQuery("delete from dis.det_dis where d_report=#dt and id_form=#form")
                        .setParameter("dt",todate)
                        .setParameter("form",rep.getRef_stat_form_id().getId_form())
                        .executeUpdate();
                entityManager.createNativeQuery("insert into dis.det_dis(id,id_region,d_report,id_form,id_pokaz,znac,line,pr_period,zo,id_filial) \n" +
                        "(select dis.sqn_det_dis.nextval,id_region,#drep2 d_report,id_form,id_pokaz,id_pokaz,line, \n" +
                        "to_number(to_char(#drep2,'mm'))||substr(to_char(#drep2,'yyyy'),2,3),zo,id_filial \n" +
                        "from dis.det_dis where d_report=#drep1 and id_form=#id_form and id_filial in (select min(id_filial) from dis.det_dis where d_report=#drep1 and id_form=#id_form)\n" +
                        " and (line='1' or line is null))")
                        .setParameter("drep1", fromdate)
                        .setParameter("drep2", todate)
                        .setParameter("id_form", rep.getRef_stat_form_id().getId_form())
                        .executeUpdate();
            }
            if (rep.getStat_schema().getId().equals("STAT")) {
                entityManager.createNativeQuery("delete from  stat.detail where d_report=#dt and id_form=#form")
                        .setParameter("dt",todate)
                        .setParameter("form",rep.getRef_stat_form_id().getId_form())
                        .executeUpdate();
                entityManager.createNativeQuery("insert into stat.detail(ID,ID_FORM,ID_POKAZ,D_REPORT,ZNAC,LINE,PR_PERIOD,ZO)\n" +
                        "(select stat.sqn_detail.nextval ID,ID_FORM,ID_POKAZ,#drep2 D_REPORT,id_pokaz ZNAC,LINE,to_number(to_char(#drep2,'mm'))||substr(to_char(#drep2,'yyyy'),2,3) PR_PERIOD,ZO\n" +
                        " from stat.detail where d_report=#drep1 and id_form=#id_form and (line='1' or line is null))")
                        .setParameter("drep1", fromdate)
                        .setParameter("drep2", todate)
                        .setParameter("id_form", rep.getRef_stat_form_id().getId_form())
                        .executeUpdate();

            }
            if (!rep.getStat_schema().getId().equals("STAT")&&!rep.getStat_schema().getId().equals("DIS"))
                throw new java.lang.Error("Ошибка. Нет обработчика для схемы "+rep.getStat_schema().getId());

            tx.commit();

        }

    }
    @Override
    public void expbalstat(Date dateParam, Integer depParam, String region, String zoParam, Report rep, FileDescriptor fileDescriptor) {
       /*
         Обработка отчета типа "Баланс" и загрузка в БД Статистика
         - dateParam - дата отчета в БД Статистика
         - depParam Идентификатор подразделения Саттистики
         - region Код региона в Статистике
         - zoParam Признак заключительных оборотов
         - rep Загружаемый отчет модуля
         - fileDescriptor Описатель файла во временном хранилище файлов сервера
        */
        try (Transaction tx = persistence.getTransaction("dbstat")) {

            EntityManager entityManager = persistence.getEntityManager("dbstat");

            Transaction tx2 = persistence.getTransaction();

            Workbook workbook = null;
            Query query = null;
            int max_row = -1;
            int key_col = 1;

            // 1. Удаляем старые данные
            entityManager.createNativeQuery("delete from stat.Rep_File where D_REPORT = #drep and ID_FORM = #id_form and ZO = #zo")
                    .setParameter("drep", dateParam)
                    .setParameter("id_form", rep.getRef_stat_form_id().getId_form())
                    .setParameter("zo", zoParam)
                    .executeUpdate();

            if (rep.getStat_schema().getId().equals("DIS")) {
                entityManager.createNativeQuery("delete from DIS.DET_DIS where D_REPORT = #drep and ID_FORM = #id_form and ZO = #zo and id_filial=#dep")
                        .setParameter("drep", dateParam)
                        .setParameter("id_form", rep.getRef_stat_form_id().getId_form())
                        .setParameter("zo", zoParam)
                        .setParameter("dep", depParam)
                        .executeUpdate();
            } else if (rep.getStat_schema().getId().equals("STAT")) {
                entityManager.createNativeQuery("delete from STAT.DETAIL where D_REPORT = #drep and ID_FORM = #id_form and ZO = #zo")
                        .setParameter("drep", dateParam)
                        .setParameter("id_form", rep.getRef_stat_form_id().getId_form())
                        .setParameter("zo", zoParam)
                        .executeUpdate();
            }
            // 2. Создаем запись об отчете с присвоение идентификатора отчета
            query = entityManager.createNativeQuery("select stat.sqn_rep_file.nextval from dual");
            query.executeUpdate();
            BigDecimal i = (BigDecimal) query.getFirstResult();

            entityManager.createNativeQuery(
                    "insert into stat.Rep_File(id,d_Report,status,zo,id_form,d_create) values(?,?,?,?,?,sysdate)")
                    .setParameter(1, i)
                    .setParameter(2, dateParam)
                    .setParameter(3, "1")
                    .setParameter(4, zoParam)
                    .setParameter(5, rep.getRef_stat_form_id().getId_form())
                    .executeUpdate();
            // 3. Находим номер строки и столбца начала отчета баланс
            int scol; // Столбец начала отчета
            int ecol; // Столбец окончания отчета
            int srow; // Строка начала отчета
            String posfix; // Постфикс показателя
            try {
                List l =  persistence.getEntityManager().createNativeQuery("select min(e.nrow) mrow,\n" +
                        "                        max(e.ncol) maxcol,min(e.ncol) mincol,max(e.posfix) posfix from public.statload_var e \n" +
                        "                    where e.delete_ts is null and id_report=?rep"
                )
                        .setParameter("rep", rep.getId())
                        .getResultList();
                Iterator it = l.listIterator();
                if (it.hasNext()) {
                    Object[] qrow = (Object[]) it.next();
                    if (qrow[3]!=null)
                        posfix = (String) qrow[3];
                    else
                        posfix = "";
                    if (qrow[2]!=null)
                      scol = (int) qrow[2];
                    else
                      scol = 1;
                    if (qrow[1]!=null)
                      ecol =  (int) qrow[1];
                    else
                      ecol = 3;
                    if (qrow[0]!=null)
                      srow =  (int) qrow[0];
                    else
                      srow =  1;
                } else
                {
                    scol = 1; // Столбец начала отчета
                    ecol = 3; // Столбец окончания отчета
                    srow = 1; // Строка начала отчета
                    posfix = ""; // Постфикс показателя
                }
                tx2.commit();
            } catch (Exception e){
                // Если нет параметров, то принимаем за начало отчета первую строку и первый столбец файла
                scol = 1; // Столбец начала отчета
                ecol = 3; // Столбец окончания отчета
                srow = 1; // Строка начала отчета
                posfix = ""; // Постфикс показателя
            }

            // 4. Находим признак окончания отчета
            workbook = WorkbookFactory.create(fileStorageAPI.openStream(fileDescriptor));
            try {
                Sheet firstSheet = workbook.getSheetAt(workbook.getNumberOfSheets() - 1);

                for (Row row : firstSheet) {

                    for (Cell cell : row) {
                        if ((cell.getColumnIndex() + 1) == key_col && getCellText(cell).equals("<<END>>")) {
                            max_row = cell.getRowIndex() + 1;
                            break;
                        }

                    }
                    if (max_row != -1) break;
                }
                if (max_row == -1) throw new java.lang.Error("Не найден признак окончания данных <<END>>");
                // Расчет признака периода
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateParam);
                String pr_period = (cal.get(Calendar.MONTH) + 1) + String.valueOf(cal.get(Calendar.YEAR)).substring(1, 4);
                // 5. Обрабатываем файл
                String Val;
                StatPokaz pokaz = null;
                BigDecimal NValCell;

                for (Row row : firstSheet) {

                    for (Cell cell : row) {

                        if ((cell.getRowIndex() + 1) >= srow) {
                            if ((cell.getColumnIndex() + 1) == scol) {
                                Val = getCellText(cell);
                                // Приводим к целочисленному виду
                                try {
                                    Float floatV = Float.parseFloat(Val);
                                    Val = String.valueOf(floatV.intValue());
                                } catch (Exception e) {
                                    Val = Val + "";// Сохраняем прочтенное значение
                                }
                                // Определяем по значению идентификатор
                                try {
                                    pokaz = dataManager
                                            .load(StatPokaz.class)
                                            .view("_local")
                                            .query("e.code = :code and e.id_form=:form")

                                            .parameter("code", Val + posfix)
                                            .parameter("form", rep.getRef_stat_form_id().getId_form())
                                            .one();
                                } catch (Exception e) {
                                    throw new java.lang.Error("Не найден показатель отчета Статистики с кодом " + Val + posfix);
                                }

                            }
                            if ((cell.getColumnIndex() + 1) == ecol) {
                                Val = getCellText(cell);
                                // Проверяем на число
                                try {
                                    NValCell = new BigDecimal(Val);
                                } catch (Exception e) {
                                    throw new java.lang.Error("Значение " + Val + " не является числом");
                                }
                                // Записываем значения в отчет
                                if (rep.getStat_schema().getId().equals("DIS")) { // Схема DIS

                                    query = entityManager.createNativeQuery("select dis.sqn_DET_DIS.nextval from dual");
                                    query.executeUpdate();
                                    BigDecimal ii = (BigDecimal) query.getFirstResult();
                                    try {
                                        entityManager.createNativeQuery(

                                                "insert into DIS.DET_DIS(id,id_region,d_report,id_pokaz,zo,id_filial,id_form,znac,line,pr_period) \n" +
                                                        "values(?id,?id_region,?d_report,?id_pokaz,?zo,?id_filial,?id_form,?znac,?line,?pr_period)")
                                                .setParameter("id", ii)
                                                .setParameter("id_region", region)
                                                .setParameter("d_report", dateParam)
                                                .setParameter("id_pokaz", pokaz.getId_pokaz())
                                                .setParameter("zo", zoParam)
                                                .setParameter("id_filial", depParam)
                                                .setParameter("id_form", rep.getRef_stat_form_id().getId_form().longValue())
                                                .setParameter("znac", NValCell)
                                                .setParameter("line", cell.getRowIndex() + 2 - srow)
                                                .setParameter("pr_period", pr_period)
                                                .executeUpdate();
                                    } catch (Exception e) {

                                        throw new java.lang.Error("Ошибка при вставке строки показателя DIS.DET_DIS id=" + ii + " id_region=" + region + ", d_report=" + dateParam + "\n " +
                                                " id_pokaz=" + pokaz.getId_pokaz() + " zo=" + zoParam + " id_filial=" + depParam +
                                                " id_form=" + rep.getRef_stat_form_id().getId_form().longValue() + " id_form=" + pokaz.getId_pokaz() +
                                                " znac=" + NValCell + " line=" + (cell.getRowIndex() + 1 - srow) + " pr_period " + pr_period + "\n" +
                                                " ERR: " + e.getMessage());


                                    }
                                } else {
                                    query = entityManager.createNativeQuery("select stat.sqn_detail.nextval from dual");
                                    query.executeUpdate();
                                    BigDecimal yy = (BigDecimal) query.getFirstResult();
                                    try {
                                        entityManager.createNativeQuery(
                                                "insert into STAT.DETAIL(id,id_form,id_pokaz,d_report,znac,line,pr_period,zo) values(?id,?id_form,?id_pokaz,?d_report,?znac,?line,?pr_period,?zo)")
                                                .setParameter("id", yy)
                                                .setParameter("id_form", rep.getRef_stat_form_id().getId_form().longValue())
                                                .setParameter("id_pokaz", pokaz.getId_pokaz())
                                                .setParameter("d_report", dateParam)
                                                .setParameter("znac", NValCell)
                                                .setParameter("line", cell.getRowIndex() + 2 - srow)
                                                .setParameter("pr_period", pr_period)
                                                .setParameter("zo", zoParam)
                                                .executeUpdate();
                                    } catch (Exception e) {

                                        throw new java.lang.Error("Ошибка при вставке строки показателя STAT.DETAIL id=" + yy + ", d_report=" + dateParam + "\n " +
                                                " id_pokaz=" + pokaz.getId_pokaz() + " zo=" + zoParam +
                                                " id_form=" + rep.getRef_stat_form_id().getId_form().longValue() + " id_pokaz=" + pokaz.getId_pokaz() +
                                                " znac=" + NValCell + " line=" + (cell.getRowIndex() + 2 - srow) + " pr_period " + pr_period + "\n" +
                                                " ERR: " + e.getMessage());

                                    }


                                }

                            }

                        } else break;

                    }
                    log.info("row.getRowNum() " + row.getRowNum() + " max_row " + max_row);
                    if ((row.getRowNum() + 1) == max_row - 1) break;
                }
            }
            finally{
                    if (workbook != null) workbook.close();
                }
            // фиксируем транзакцию
            tx.commit();

            try {
                delFile(fileDescriptor);
            } catch (Exception e) {
                throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, "File storage не смог удалить файл " + fileDescriptor.getId());
            }

        } catch (IOException | FileStorageException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void expstat(Date dateParam, Integer depParam, String region, String zoParam, Report rep, FileDescriptor fileDescriptor) {
        /*
         Обработка отчета типа "Матричный" и "Переменной длины" и загрузка в БД Статистика
         - dateParam - дата отчета в БД Статистика
         - depParam Идентификатор подразделения Саттистики
         - region Код региона в Статистике
         - zoParam Признак заключительных оборотов
         - rep Загружаемый отчет модуля
         - fileDescriptor Описатель файла во временном хранилище файлов сервера
        */
        try (Transaction tx = persistence.getTransaction("dbstat")) {

            EntityManager entityManager = persistence.getEntityManager("dbstat");

            Transaction tx2 = persistence.getTransaction();

            Workbook workbook = null;
            Query query = null;
            Object[] qrow = null;
            ListIterator it; // итератор для обхода по списку показзателей
            int key_col = 1; // ключевой столбец для определения окончания строк
            long num_col = -1; //номер столбца настройки показателя
            long num_row = -1; //номер строки настройки показателя
            long min_row = 0;  //минимальное значение строки для расчета номера строки статистики
            long max_row = -1; // максимальный номер строки
            long p_row = -1; // приращение для отчетов переменной длинны
            long rep_row = -1; //номер строки статистики
            long id_pokaz = -1; // идентификатор показателя
            long id_sprav = -1; // идентификатор показателя
            String sprav = ""; // наименование справочника
            String keyfield = ""; // клюючевое поле справочника
            String resfield = ""; // поле результата справочника
            String posfix = null;// набор символов, который используется при поиске в справочнике значения. добавляется справа
            BigDecimal yy = null;
            BigDecimal ii = null;
            String ValCell = null;
            BigDecimal NValCell = null;
            BigInteger IValCell = null;

            Calendar cal = null;
            // 1. Удаляем старые данные
            entityManager.createNativeQuery("delete from stat.Rep_File where D_REPORT = #drep and ID_FORM = #id_form and ZO = #zo")
                    .setParameter("drep", dateParam)
                    .setParameter("id_form", rep.getRef_stat_form_id().getId_form())
                    .setParameter("zo", zoParam)
                    .executeUpdate();

            if (rep.getStat_schema().getId().equals("DIS")) {
                entityManager.createNativeQuery("delete from DIS.DET_DIS where D_REPORT = #drep and ID_FORM = #id_form and ZO = #zo and id_filial=#dep")
                        .setParameter("drep", dateParam)
                        .setParameter("id_form", rep.getRef_stat_form_id().getId_form())
                        .setParameter("zo", zoParam)
                        .setParameter("dep", depParam)
                        .executeUpdate();
            } else if (rep.getStat_schema().getId().equals("STAT")) {
                entityManager.createNativeQuery("delete from STAT.DETAIL where D_REPORT = #drep and ID_FORM = #id_form and ZO = #zo")
                        .setParameter("drep", dateParam)
                        .setParameter("id_form", rep.getRef_stat_form_id().getId_form())
                        .setParameter("zo", zoParam)
                        .executeUpdate();
            }
            // 2. Создаем запись об отчете с присвоение идентификатора отчета
            query = entityManager.createNativeQuery("select stat.sqn_rep_file.nextval from dual");
            query.executeUpdate();
            BigDecimal i = (BigDecimal) query.getFirstResult();

            entityManager.createNativeQuery(
                    "insert into stat.Rep_File(id,d_Report,status,zo,id_form,d_create) values(?,?,?,?,?,sysdate)")
                    .setParameter(1, i)
                    .setParameter(2, dateParam)
                    .setParameter(3, "1")
                    .setParameter(4, zoParam)
                    .setParameter(5, rep.getRef_stat_form_id().getId_form())
                    .executeUpdate();

            log.debug("Обработка отчета rep.getId()=" + rep.getId());
            // Читаем показатели из справочника
            List l = persistence.getEntityManager().createNativeQuery("select e.id_report,e.nrow,\n" +
                            "                        e.ncol, s.id_pokaz,f.id_table,f.name_table,e.stat_key_field,e.stat_res_field,e.posfix from public.statload_var e \n" +
                            "                     left join public.statload_stat_pokaz s on (s.id = e.id_statpokaz) \n" +
                            "                     left join public.statload_stat_sprav f on (f.id = e.id_sprav)\n" +
                            "                    where e.delete_ts is null and id_report=?rep order by 2,3"
            )
                    .setParameter("rep", rep.getId())
                    .getResultList();

            tx2.commit();

               int cou = 0;
               for (Iterator it2 = l.iterator(); it2.hasNext(); it2.next()) {
                   cou++;
               }
               log.debug("Колличество показателей в настройках " + cou);

            if (cou==0) throw new java.lang.Error("Не найдено показателей для обработки файла. Проверьте настройку модуля и повторите импорт.");

            // Просматриваем книгу и вытаскиваем данные показателей
            workbook = WorkbookFactory.create(fileStorageAPI.openStream(fileDescriptor));
            try {
                Sheet firstSheet = workbook.getSheetAt(workbook.getNumberOfSheets() - 1);


                // Определяем максимальную строку для отчета переменной длинны
                if (rep.getVid().getId() == 1 || rep.getVid().getId() == 3) {
                    for (Row row : firstSheet) {

                        for (Cell cell : row) {
                            if ((cell.getColumnIndex() + 1) == key_col && getCellText(cell).equals("<<END>>")) {
                                max_row = cell.getRowIndex() + 1;
                                break;
                            }

                        }
                        if (max_row != -1) break;
                    }
                }
                if (max_row == -1 && (rep.getVid().getId() == 1 || rep.getVid().getId() == 3))
                    throw new java.lang.Error("Не найден признак окончания данных <<END>>");
                log.debug("Максимальная строка max_row=" + max_row);
                //----------------------------------------------------------------------

                // Расчет признака периода
                cal = Calendar.getInstance();
                cal.setTime(dateParam);
                String pr_period = (cal.get(Calendar.MONTH) + 1) + String.valueOf(cal.get(Calendar.YEAR)).substring(1, 4);

                if (rep.getVid().getId() == 2 && rep.getStat_schema().getId().equals("STAT")) // Если отчет матричный и схема STAT, то номер строки пустой
                    rep_row = -1;
                else
                    rep_row = 1;
                p_row = 0;

                // Переводим цикл по показателям в начало
                it = l.listIterator();
                if (it.hasNext()) {
                    qrow = (Object[]) it.next();
                }

                min_row = (int) qrow[1];
                num_row = (int) qrow[1] + p_row;
                num_col = (int) qrow[2];
                if (qrow[3] != null)
                    id_pokaz = (Long) qrow[3];
                else
                    id_pokaz = -1;
                if (qrow[4] != null)
                    id_sprav = (Long) qrow[4];
                else
                    id_sprav = -1;
                if (qrow[5] != null)
                    sprav = (String) qrow[5];
                else
                    sprav = "";
                if (qrow[6] != null)
                    keyfield = (String) qrow[6];
                else
                    keyfield = "";
                if (qrow[7] != null)
                    resfield = (String) qrow[7];
                else
                    resfield = "";
                if (qrow[8] != null)
                    posfix = (String) qrow[8];
                else
                    posfix = "";


                log.debug("Переменные expstat p_rowһ" + p_row + ",min_row=" + min_row + ",num_row=" + num_row + ",num_col=" + num_col + ",id_pokaz=" + id_pokaz + ",id_sprav=" + id_sprav + ",sprav=" + sprav +
                        ",keyfield=" + keyfield + ",resfield=" + resfield);

                // Находим нужную ячейку
                log.debug("Вход в цикл по ячейкам " + num_col + "," + num_row);
                for (Row row : firstSheet) {
                    for (Cell cell : row) {
                        // try {
                        log.debug("Смотрим ячейку col=" + (cell.getColumnIndex() + 1) + ",row=" + (cell.getRowIndex() + 1) + "type=" + cell.getCellType().toString());
                        if ((cell.getColumnIndex() + 1) == num_col && (cell.getRowIndex() + 1) == num_row) {
                            // Значение ячейки
                            ValCell = getCellText(cell);

                            log.debug("Найдено соответствие ячейки num_row(" + num_row + "),num_col(" + num_col + ") - " + cell.getCellType().toString());
                            log.debug("Переменные expstat p_row=" + p_row + ",min_row=" + min_row + ",num_row=" + num_row + ",num_col=" + num_col + ",id_pokaz=" + id_pokaz + ",id_sprav=" + id_sprav + ",sprav=" + sprav +
                                    ",keyfield=" + keyfield + ",resfield=" + resfield);
                            log.debug("ValCell=" + ValCell);

                            // Если преобразуется в число и нет указания на справочник, то записывать в основную таблицу
                            boolean isnum;
                            try {
                                NValCell = new BigDecimal(ValCell);
                                isnum = true;
                            } catch (Exception e) {
                                isnum = false;
                            }

                            log.debug("isnum=" + isnum);
                            log.debug("схема=" + rep.getStat_schema().getId());
                            // Обрабатываем ситуацию, когда связи с показателем Статистики не настроено
                            if (id_pokaz == -1 && isnum) {
                                // Находим показатель по коду, равным значению отчета
                                StatPokaz pokaz;
                                try {
                                    pokaz = dataManager
                                            .load(StatPokaz.class)
                                            .view("_local")
                                            .query("e.code = :code and e.id_form=:form")

                                            .parameter("code", ValCell + posfix)
                                            .parameter("form", rep.getRef_stat_form_id().getId_form())
                                            .one();
                                } catch (Exception e) {
                                    pokaz = null;
                                }
                                if (pokaz != null) {
                                    id_pokaz = pokaz.getId_pokaz();
                                    sprav = "STAT.S_POKAZ";
                                    //  Определяем идентификатор справочника
                                    try {
                                        StatSprav entsprav = dataManager
                                                .load(StatSprav.class)
                                                .view("_local")
                                                .query("e.nameTable = :code")
                                                .parameter("code", sprav)
                                                .one();
                                        id_sprav = entsprav.getId_table();
                                        keyfield = "ID";
                                        resfield = "CODE_POKAZ";
                                    } catch (Exception e) {
                                        throw new java.lang.Error("Не нашел идентификатора справочника " + sprav);
                                    }

                                }

                            }

                            if (id_pokaz != -1) {

                                // Вставляем в таблицу со значениями показателей
                                if (rep.getStat_schema().getId().equals("DIS")) {
                                    log.debug("Обработка схемы DIS");

                                    query = entityManager.createNativeQuery("select dis.sqn_DET_DIS.nextval from dual");
                                    query.executeUpdate();
                                    ii = (BigDecimal) query.getFirstResult();

                                    if (isnum && id_sprav == -1) { //Если число и нет указания на справочник, то записывать в DET_DIS

                                        try {
                                            entityManager.createNativeQuery(

                                                    "insert into DIS.DET_DIS(id,id_region,d_report,id_pokaz,zo,id_filial,id_form,znac,line,pr_period) \n" +
                                                            "values(?id,?id_region,?d_report,?id_pokaz,?zo,?id_filial,?id_form,?znac,?line,?pr_period)")
                                                    .setParameter("id", ii)
                                                    .setParameter("id_region", region)
                                                    .setParameter("d_report", dateParam)
                                                    .setParameter("id_pokaz", id_pokaz)
                                                    .setParameter("zo", zoParam)
                                                    .setParameter("id_filial", depParam)
                                                    .setParameter("id_form", rep.getRef_stat_form_id().getId_form().longValue())
                                                    .setParameter("znac", NValCell)
                                                    .setParameter("line", rep_row)
                                                    .setParameter("pr_period", pr_period)
                                                    .executeUpdate();
                                        } catch (Exception e) {

                                            throw new java.lang.Error("Ошибка при вставке строки показателя DIS.DET_DIS " + id_pokaz + " ncol=" + num_col + ", ValCell=" + ValCell + " ERR: " + e.getMessage());


                                        }

                                    } else {

                                        try {
                                            entityManager.createNativeQuery(
                                                    "insert into DIS.DET_DIS(id,id_region,d_report,id_pokaz,zo,id_filial,id_form,line,pr_period) \n" +
                                                            "values(?id,?id_region,?d_report,?id_pokaz,?zo,?id_filial,?id_form,?line,?pr_period)")
                                                    .setParameter("id", ii)
                                                    .setParameter("id_region", region)
                                                    .setParameter("d_report", dateParam)
                                                    .setParameter("id_pokaz", id_pokaz)
                                                    .setParameter("zo", zoParam)
                                                    .setParameter("id_filial", depParam)
                                                    .setParameter("id_form", rep.getRef_stat_form_id().getId_form().longValue())

                                                    .setParameter("line", rep_row)
                                                    .setParameter("pr_period", pr_period)
                                                    .executeUpdate();
                                        } catch (Exception e) {

                                            throw new java.lang.Error("Ошибка при вставке строки показателя DIS.DET_DIS " + id_pokaz + " ncol=" + num_col + ", ValCell=" + ValCell + " ERR: " + e.getMessage());

                                        }

                                        if (id_sprav == -1) {


                                            entityManager.createNativeQuery(
                                                    "insert into DIS.DET_DIS_PROPERTY(id_det,stroka) \n" +
                                                            "values(" + ii + ",'" + ValCell + "')")
                                                    .executeUpdate();
                                        } else {
                                            // делаем выборку из справочника по полю
                                            try {

                                                query = entityManager.createNativeQuery("select " + keyfield + " from " + sprav + " where " + resfield + "='" + ValCell + posfix + "' and sysdate between dat_beg and nvl(dat_end,to_date('01.01.4700','dd.mm.yyyy'))");
                                                query.executeUpdate();
                                                NValCell = (BigDecimal) query.getFirstResult();
                                                IValCell = NValCell.toBigInteger();
                                                log.debug("Идентификатор справочника" + IValCell);
                                            } catch (NullPointerException e) {
                                                throw new java.lang.Error("Не найдено значение справочника " + sprav + " в строке=" + num_row + " для значения " + ValCell + posfix);

                                            }

                                            entityManager.createNativeQuery(
                                                    "insert into DIS.DET_DIS_PROPERTY(id_det,id_name_tables,id_sprav) \n" +
                                                            "values(" + ii + "," + id_sprav + "," + IValCell + ")")
                                                    .executeUpdate();

                                        }
                                    }

                                } else if (rep.getStat_schema().getId().equals("STAT")) {
                                    log.debug("Обработка схемы STAT");
                                    query = entityManager.createNativeQuery("select stat.sqn_detail.nextval from dual");
                                    query.executeUpdate();
                                    yy = (BigDecimal) query.getFirstResult();

                                    if (isnum && id_sprav == -1) { //Если число и нет указания на справочник, то записывать в DETAIL
                                        try {
                                            if (rep_row == -1)
                                                entityManager.createNativeQuery(
                                                        "insert into STAT.DETAIL(id,id_form,id_pokaz,d_report,znac,pr_period,zo) values(?id,?id_form,?id_pokaz,?d_report,?znac,?pr_period,?zo)")
                                                        .setParameter("id", yy)
                                                        .setParameter("id_form", rep.getRef_stat_form_id().getId_form().longValue())
                                                        .setParameter("id_pokaz", id_pokaz)
                                                        .setParameter("d_report", dateParam)
                                                        .setParameter("znac", NValCell)
                                                        .setParameter("pr_period", pr_period)
                                                        .setParameter("zo", zoParam)
                                                        .executeUpdate();
                                            else
                                                entityManager.createNativeQuery(
                                                        "insert into STAT.DETAIL(id,id_form,id_pokaz,d_report,znac,line,pr_period,zo) values(?id,?id_form,?id_pokaz,?d_report,?znac,?line,?pr_period,?zo)")
                                                        .setParameter("id", yy)
                                                        .setParameter("id_form", rep.getRef_stat_form_id().getId_form().longValue())
                                                        .setParameter("id_pokaz", id_pokaz)
                                                        .setParameter("d_report", dateParam)
                                                        .setParameter("znac", NValCell)
                                                        .setParameter("line", rep_row)
                                                        .setParameter("pr_period", pr_period)
                                                        .setParameter("zo", zoParam)
                                                        .executeUpdate();
                                        } catch (Exception e) {

                                            throw new java.lang.Error("Ошибка при вставке строки показателя STAT.DETAIL " + id_pokaz + " ncol=" + num_col + ", ValCell=" + ValCell + "ERR: " + e.getMessage());

                                        }

                                    } else {
                                        try {
                                            if (rep_row == -1)
                                                entityManager.createNativeQuery(
                                                        "insert into STAT.DETAIL(id,id_form,id_pokaz,d_report,pr_period,zo) values(?id,?id_form,?id_pokaz,?d_report,?pr_period,?zo)")
                                                        .setParameter("id", yy)
                                                        .setParameter("id_form", rep.getRef_stat_form_id().getId_form().longValue())
                                                        .setParameter("id_pokaz", id_pokaz)
                                                        .setParameter("d_report", dateParam)

                                                        .setParameter("pr_period", pr_period)
                                                        .setParameter("zo", zoParam)
                                                        .executeUpdate();
                                            else
                                                entityManager.createNativeQuery(
                                                        "insert into STAT.DETAIL(id,id_form,id_pokaz,d_report,line,pr_period,zo) values(?id,?id_form,?id_pokaz,?d_report,?line,?pr_period,?zo)")
                                                        .setParameter("id", yy)
                                                        .setParameter("id_form", rep.getRef_stat_form_id().getId_form().longValue())
                                                        .setParameter("id_pokaz", id_pokaz)
                                                        .setParameter("d_report", dateParam)

                                                        .setParameter("line", rep_row)
                                                        .setParameter("pr_period", pr_period)
                                                        .setParameter("zo", zoParam)
                                                        .executeUpdate();
                                        } catch (Exception e) {

                                            throw new java.lang.Error("Ошибка при вставке строки показателя STAT.DETAIL " + id_pokaz + " ncol=" + num_col + ", ValCell=" + ValCell + "ERR: " + e.getMessage());

                                        }

                                        if (id_sprav == -1) {

                                            entityManager.createNativeQuery(

                                                    "insert into STAT.DETAIL_PROPERTY(id,id_detail,stroka) \n" +
                                                            "values(stat.sqn_detail_property.nextval," + yy + ",'" + ValCell + "')")
                                                    .executeUpdate();
                                        } else {
                                            // делаем выборку из справочника по полю
                                            try {

                                                query = entityManager.createNativeQuery("select " + keyfield + " from " + sprav + " where " + resfield + "='" + ValCell + posfix + "' and sysdate between dat_beg and nvl(dat_end,to_date('01.01.4700','dd.mm.yyyy'))");
                                                query.executeUpdate();
                                                NValCell = (BigDecimal) query.getFirstResult();
                                                IValCell = NValCell.toBigInteger();

                                            } catch (NullPointerException e) {
                                                throw new java.lang.Error("Не найдено значение справочника " + sprav + " в строке=" + num_row + " для значения " + ValCell + posfix);
                                            }

                                            entityManager.createNativeQuery(
                                                    "insert into STAT.DETAIL_PROPERTY(id,id_detail,id_name_tables,id_kod_spr) \n" +
                                                            "values(stat.sqn_detail_property.nextval," + yy + "," + id_sprav + "," + IValCell + ")")
                                                    .executeUpdate();

                                        }
                                    }
                                } else
                                    throw new java.lang.Error("Не известный код схемы Статистики " + rep.getStat_schema().getId());
                            }
                            if (!it.hasNext()) {
                                while (it.hasPrevious()) it.previous();
                                p_row = p_row + 1;
                            }

                            qrow = (Object[]) it.next();
                            num_col = (int) qrow[2];

                            num_row = (int) qrow[1] + p_row;

                            if (rep.getVid().getId() == 1) // Если отчет переменной длинны
                                rep_row = num_row - min_row + 1;

                            if (qrow[3] != null)
                                id_pokaz = (Long) qrow[3];
                            else
                                id_pokaz = -1;
                            if (qrow[4] != null)
                                id_sprav = (Long) qrow[4];
                            else
                                id_sprav = -1;
                            if (qrow[5] != null)
                                sprav = (String) qrow[5];
                            else
                                sprav = "";
                            if (qrow[6] != null)
                                keyfield = (String) qrow[6];
                            else
                                keyfield = "";
                            if (qrow[7] != null)
                                resfield = (String) qrow[7];
                            else
                                resfield = "";
                            if (qrow[8] != null)
                                posfix = (String) qrow[8];
                            else
                                posfix = "";

                        }

                    }
                    if (num_row == max_row) break;
                }
            }
            finally {
                if (workbook != null) workbook.close();
            }
            tx2.commit();
            tx.commit();

            try {
                delFile(fileDescriptor);
            } catch (Exception e) {
                throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, "File storage не смог удалить файл " + fileDescriptor.getId());
            }

        } catch (IOException | FileStorageException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getDepartmentStatId(String depcode){
        /*
        Функция определяет идентификатор филиала Статистики для указания в отчетах схемы DIS
        depcode - Код подразделения (000,120...)
         */
        try {

            Department dep = dataManager
                    .load(Department.class)
                    .view("_local")
                    .query("e.code=:code")

                    .parameter("code", depcode)
                    .one();
            return dep.getStat_bank_id();
        }
        catch(Exception e){
            throw new java.lang.Error("Ошибка при определении идентификатора статистики для подразделения с кодом "+depcode);
        }
    }
    @Override
    public String getDepartmentRegion(String depcode){
        /*
        Функция определяет код региона Статистики для подразделения
        depcode - Код подразделения (000,120...)
         */
        try {

            Department dep = dataManager
                    .load(Department.class)
                    .view("_local")
                    .query("e.code=:code")

                    .parameter("code", depcode)
                    .one();
            return dep.getRegion();
        }
        catch(Exception e){
            throw new java.lang.Error("Ошибка при определении региона для подразделения с кодом "+depcode);
        }
    }

    @Override
    public void expstatidpokaz(Report rep, FileDescriptor fileDescriptor) {
        /*
        Функция загружает из файла идентификаторы показателей Статистики и в соответствии с расположением показателей в шаблоне модуля присваивает показателю этот идентификатор
        Шаблон Статистики и шаблон модуля по показателям должны совпадать
        Перед загрузкой  идентификаторов надо проверить корректность данных показателей модуля в части столбца и строки
        Параметры:
         - rep - Отчет, для которого идет загрузка
         - fileDescriptor - Описатель файла во временном хранилище файлов на сервере
        Возможные ошибки:
         - Не найден показатель Статистики с идентификатором
         - Не найдено показателей для обработки файла. Проверьте настройку модуля и повторите импорт.
         */
        log.debug("Начало expstatidpokaz");
        try (Transaction tx = persistence.getTransaction()) {

            Workbook workbook = null;
            Object[] qrow = null;
            ListIterator it; // итератор для обхода по списку показзателей
            long num_col = -1; //номер столбца настройки показателя
            long num_row = -1; //номер строки настройки показателя
            long id_pokaz = -1; // идентификатор показателя
            String ValCell = null;
            BigDecimal NValCell = null;

            log.debug("Обработка отчета rep.getId()=" + rep.getId());
            // Читаем показатели из справочника
            List l = persistence.getEntityManager().createNativeQuery("select e.id,e.nrow,\n" +
                    "                        e.ncol,s.id_pokaz from public.statload_var e \n" +
                    "                     left join public.statload_stat_pokaz s on (s.id = e.id_statpokaz) \n" +
                    "                    where e.delete_ts is null and id_report=?rep order by 2,3"
            )
                    .setParameter("rep", rep.getId())
                    .getResultList();

            int cou = 0;
            for (Iterator it2 = l.iterator(); it2.hasNext(); it2.next()) {
                cou++;
            }
            log.debug("Колличество показателей в настройках " + cou);

            if (cou==0) throw new java.lang.Error("Не найдено показателей для обработки файла. Проверьте настройку модуля и повторите импорт.");

            // Просматриваем книгу и вытаскиваем данные показателей
            workbook = WorkbookFactory.create(fileStorageAPI.openStream(fileDescriptor));
            try {
                Sheet firstSheet = workbook.getSheetAt(0);

                it = l.listIterator();
                if (it.hasNext()) {
                    qrow = (Object[]) it.next();
                }
                UUID iid = (UUID) qrow[0];
                num_row = (int) qrow[1];
                num_col = (int) qrow[2];
                if (qrow[3] != null)
                    id_pokaz = (Long) qrow[3];
                else
                    id_pokaz = -1;

                // Находим нужную ячейку
                log.debug("Вход в цикл по ячейкам " + num_col + "," + num_row);
                boolean inloop = true;

                for (Row row : firstSheet) {
                    for (Cell cell : row) {
                        try {
                            log.debug("Смотрим ячейку col=" + (cell.getColumnIndex() + 1) + ",row=" + (cell.getRowIndex() + 1) + "type=" + cell.getCellType().toString());
                            // Если координаты для показателя модуля найдены
                            if ((cell.getColumnIndex() + 1) == num_col && (cell.getRowIndex() + 1) == num_row) {
                                // Значение ячейки
                                ValCell = getCellText(cell);

                                // Если преобразуется в число и нет указания на справочник, то записывать в основную таблицу
                                boolean isnum;
                                try {
                                    NValCell = new BigDecimal(ValCell);
                                    isnum = true;
                                } catch (Exception e) {
                                    isnum = false;
                                }
                                // Обрабатываем только показатели с числовым выражением
                                if (isnum) {
                                    // Определяем строку показателей Статистики
                                    StatPokaz pokaz;
                                    try {
                                        pokaz = dataManager
                                                .load(StatPokaz.class)
                                                .view("_local")
                                                .query("e.id_pokaz = :idpokaz and e.id_form=:form")

                                                .parameter("idpokaz", NValCell.intValue())
                                                .parameter("form", rep.getRef_stat_form_id().getId_form())
                                                .one();
                                    } catch (javax.persistence.NoResultException e) {
                                        throw new RuntimeException("Не найден показатель Статистики с идентификатором " + NValCell.longValue(), e);
                                    }
                                    // Записываем ссылку на него в настройках показателей
                                    persistence.getEntityManager().createNativeQuery("update statload_var set ID_STATPOKAZ=?pokaz where id=?iid")
                                            .setParameter("pokaz", pokaz.getId())
                                            .setParameter("iid", iid)
                                            .executeUpdate();

                                }
                                if (it.hasNext()) {
                                    qrow = (Object[]) it.next();
                                } else { // Если закончились записи, то выходи из всех циклов

                                    inloop = false;
                                    break;
                                }

                                iid = (UUID) qrow[0];
                                num_col = (int) qrow[2];

                                num_row = (int) qrow[1];

                                if (qrow[3] != null)
                                    id_pokaz = (Long) qrow[3];
                                else
                                    id_pokaz = -1;

                            }
                        } catch (Exception e) {
                            throw new java.lang.Error("Ошибка при импорте строки " + row.getRowNum() + ", не найдено импортируемое значение " + ValCell + " или иная ошибка " +
                                    "\nТекст ошибки: " + e.getMessage() +
                                    "\nИсправьте файл и импортируйте его заново!");
                        }
                        if (!inloop) break;
                    }
                    if (!inloop) break;
                }
            }
            finally {

                if (workbook != null) workbook.close();
            }

            tx.commit();

            try {
                delFile(fileDescriptor);
            } catch (Exception e) {
                throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, "File storage не смог удалить файл " + fileDescriptor.getId());
            }


        } catch (IOException | FileStorageException e) {
            e.printStackTrace();
        }
    }
}