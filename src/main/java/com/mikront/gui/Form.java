package com.mikront.gui;

import com.mikront.fillics.resource.Dimens;
import com.mikront.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;


public class Form {
    private static final Logger log = LogManager.getLogger();
    protected static final ResourceBundle lang = ResourceBundle.getBundle("com.mikront.fillics.Lang");

    private final JFrame frame = new JFrame();
    private final JPanel rootPanel = new JPanel();


    @SuppressWarnings("SameParameterValue")
    protected static <T extends Form> void load(Class<T> formClass) {
        Form form = Utils.getNewInstanceOrThrow(formClass);
        form.create();
        EventQueue.invokeLater(form::show);
    }

    private void create() {
        try {
            //Apply Windows look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            log.error("Unable to set system default look and feel", e);
        }

        frame.setTitle(lang.getString("app_name"));
        frame.setLocationByPlatform(true); //Let the system decide
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        frame.setContentPane(rootPanel);

        rootPanel.setBorder(BorderFactory.createEmptyBorder(
                Dimens.FORM_PADDING,
                Dimens.FORM_PADDING,
                Dimens.FORM_PADDING,
                Dimens.FORM_PADDING));

        onCreate();

        //Apply default styles to components after layout creation
        Components.applyDefaults(Components.getWholeTree(rootPanel));
    }

    protected void onCreate() {}

    private void show() {
        frame.pack();
        frame.setVisible(true);
        frame.setMinimumSize(frame.getSize());

        onPostShow();
    }

    protected void onPostShow() {}


    public JPanel getRootPanel() {
        return rootPanel;
    }
}