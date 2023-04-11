package com.mikront.gui;

import com.mikront.fillics.resource.Dimens;
import com.mikront.fillics.resource.Strings;
import com.mikront.util.Utils;
import com.mikront.util.debug.Log;

import javax.swing.*;
import java.awt.*;


public class Form extends Context {
    private final JFrame frame = new JFrame();
    private final JPanel container = new JPanel();


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
            Log.e("Form::onCreate: unable to set system default look and feel");
            Log.e("Form::onCreate:   = catching: ", e);
        }

        frame.setTitle(getString(Strings.APP_NAME));
        frame.setLocationByPlatform(true); //Let the system decide
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        frame.setContentPane(container);

        container.setBorder(BorderFactory.createEmptyBorder(
                Dimens.FORM_PADDING,
                Dimens.FORM_PADDING,
                Dimens.FORM_PADDING,
                Dimens.FORM_PADDING));

        onCreate();

        //Apply default styles to components after layout creation
        Components.applyDefaults(Components.getWholeTree(container));
    }

    protected void onCreate() {}

    private void show() {
        frame.pack();
        frame.setVisible(true);
        frame.setMinimumSize(frame.getSize());

        onPostShow();
    }

    protected void onPostShow() {}


    public JFrame getFrame() {
        return frame;
    }

    public JPanel getContainer() {
        return container;
    }
}