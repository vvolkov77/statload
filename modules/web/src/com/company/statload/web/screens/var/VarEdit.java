package com.company.statload.web.screens.var;

import com.company.statload.entity.Report;
import com.company.statload.entity.StatPokaz;
import com.company.statload.entity.StatSprav;
import com.company.statload.service.LoadFileService;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.app.core.inputdialog.InputParameter;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.inputdialog.InputDialogAction;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.CollectionLoader;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.model.InstanceContainer;
import com.haulmont.cuba.gui.screen.*;
import com.company.statload.entity.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;

@UiController("statload_Var.edit")
@UiDescriptor("var-edit.xml")
@EditedEntityContainer("varDc")
@LoadDataBeforeShow
public class VarEdit extends StandardEditor<Var> {
    private static Logger log = LoggerFactory.getLogger(VarEdit.class);
    protected StatSprav N;

    public StatSprav getN() {
        return N;
    }

    public void setN(StatSprav n) {
        this.N = n;
    }

    @Inject
    private CollectionLoader<StatPokaz> ref_pokazesLc;

    @Inject
    private InstanceContainer<Var> varDc;

    @Inject
    private Form form;

    @Inject
    private LoadFileService loadFileSvc;



    @Inject
    private CollectionContainer<StatSprav> ref_spravsDc;

    @Inject
    private Dialogs dialogs;

    @Inject
    private LookupField<StatSprav> ref_spravField;

    @Subscribe(id = "ref_reportsDc", target = Target.DATA_CONTAINER)
    public void onRef_reportsDcItemChange(InstanceContainer.ItemChangeEvent<Report> event) {
        if (event.getItem() != null) {
            if (event.getItem().getRef_stat_form_id()==null){
                ref_pokazesLc.removeParameter("id");
            } else ref_pokazesLc.setParameter("id", event.getItem().getRef_stat_form_id().getId_form());
        } else {
            ref_pokazesLc.removeParameter("id");
        }
        ref_pokazesLc.load();


    }

    @Subscribe(id = "ref_spravsDc", target = Target.DATA_CONTAINER)
    public void onRef_spravsDcItemChange(InstanceContainer.ItemChangeEvent<StatSprav> event) {
        if (event.getItem() == null) {
            varDc.getItem().setStat_key_field("");
            varDc.getItem().setStat_res_field("");
            form.getComponentNN("stat_key_fieldField").setEnabled(false);
            form.getComponentNN("stat_res_fieldField").setEnabled(false);

        } else
        {
            form.getComponentNN("stat_key_fieldField").setEnabled(true);
            form.getComponentNN("stat_res_fieldField").setEnabled(true);
        }
    }

    @Subscribe(id = "varDc", target = Target.DATA_CONTAINER)
    public void onVarDcItemChange(InstanceContainer.ItemChangeEvent<Var> event) {
        if (event.getItem().getRef_pokaz() == null) {
            event.getItem().setRef_sprav(null);

            event.getItem().setStat_key_field("");
            event.getItem().setStat_res_field("");
            form.getComponentNN("ref_spravField").setEnabled(false);
            form.getComponentNN("stat_key_fieldField").setEnabled(false);
            form.getComponentNN("stat_res_fieldField").setEnabled(false);

        } else
        {
            form.getComponentNN("ref_spravField").setEnabled(true);
        }
        if (event.getItem().getRef_sprav() == null) {
            event.getItem().setStat_key_field("");
            event.getItem().setStat_res_field("");
            form.getComponentNN("stat_key_fieldField").setEnabled(false);
            form.getComponentNN("stat_res_fieldField").setEnabled(false);

        } else
        {
            form.getComponentNN("stat_key_fieldField").setEnabled(true);
            form.getComponentNN("stat_res_fieldField").setEnabled(true);
        }
    }

    @Subscribe(id = "ref_pokazesDc", target = Target.DATA_CONTAINER)
    public void onRef_pokazesDcItemChange(InstanceContainer.ItemChangeEvent<StatPokaz> event) {
        if (event.getItem() == null) {
            varDc.getItem().setRef_sprav(null);
            form.getComponentNN("ref_spravField").setEnabled(false);
            form.getComponentNN("stat_key_fieldField").setEnabled(false);
            form.getComponentNN("stat_res_fieldField").setEnabled(false);
            varDc.getItem().setStat_key_field("");
            varDc.getItem().setStat_res_field("");

        } else
        {
            form.getComponentNN("ref_spravField").setEnabled(true);
        }

    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        // Определяем справочник и находим стандартные поля
        if (varDc.getItem().getRef_pokaz()!=null&&varDc.getItem().getRef_sprav()==null&&varDc.getItem().getRef_pokaz()!=null&&
                varDc.getItem().getRef_report().getStat_schema()!=null
        ) {
            log.debug(varDc.getItem().getRef_pokaz().getId_pokaz()+","+ varDc.getItem().getRef_pokaz().getId_form()+","+ varDc.getItem().getRef_report().getStat_schema().getId());
            int sprav = loadFileSvc.getidsprav(varDc.getItem().getRef_pokaz().getId_pokaz(), varDc.getItem().getRef_pokaz().getId_form(), varDc.getItem().getRef_report().getStat_schema().getId());
            if (sprav != -1) {
                //dialogs.createMessageDialog().withCaption("Результат сервиса нахождения справочника").withMessage(String.valueOf(sprav)).show();


                for (Iterator i = ref_spravsDc.getItems().iterator();i.hasNext();){
                    setN((StatSprav) i.next());
                    if (getN().getId_table().intValue()==sprav) {

                        dialogs.createOptionDialog()
                                .withCaption("Внимание!")
                                .withMessage("В сформированных отчетах Статистики найдена привязка показателя к справочнику. Применить эти настройки?")
                                .withActions(
                                        new DialogAction(DialogAction.Type.OK).withHandler(e -> {

                                            // resume with default behavior
                                            //event.resume();

                                           // ref_spravsDc.setItem(getN());
                                           // ref_spravField.setValue(getN());
                                            getEditedEntity().setRef_sprav(getN());
                                            if (loadFileSvc.fieldisvalid("ID",getN().getNameTable())) getEditedEntity().setStat_key_field("ID");
                                            if (loadFileSvc.fieldisvalid("CODE",getN().getNameTable())) getEditedEntity().setStat_res_field("CODE");
                                            //event.preventCommit();
                                            event.resume();
                                          //  commitChanges();

                                          //
                                        }),
                                        new DialogAction(DialogAction.Type.CANCEL).withHandler(e -> {}
                                        )
                                )

                                .show();

                        break;
                    }
                }

            }
            //else dialogs.createMessageDialog().withCaption("Результат сервиса нахождения справочника").withMessage("Справочник не найден").show();
        }

    }





}