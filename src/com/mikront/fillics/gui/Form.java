package com.mikront.fillics.gui;

import com.mikront.fillics.resource.Dimens;
import com.mikront.fillics.resource.Strings;
import com.mikront.util.debug.Log;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;


public class Form implements Dimens, Strings {
    private final JFrame frame = new JFrame();
    private final JPanel container = new JPanel();


    @SuppressWarnings("SameParameterValue")
    protected static <T extends Form> void load(Class<T> formClass) {
        Form form;
        try {
            form = formClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            Log.e("Form::load: unable to create a form instance");
            Log.e("Form::load:   - formClass = " + formClass);
            Log.e("Form::load:   = catching: ", e);
            return;
        }

        form.onCreate();
        EventQueue.invokeLater(form::show);
    }

    protected void onCreate() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            Log.e("Form::onCreate: unable to set system default look and feel");
            Log.e("Form::onCreate:   = catching: ", e);
        }

        frame.setTitle(APP_NAME);
        frame.setLocationByPlatform(true); //Let the system decide
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        frame.setContentPane(container);

        container.setBorder(BorderFactory.createEmptyBorder(FORM_PADDING, FORM_PADDING, FORM_PADDING, FORM_PADDING));
    }

    private void show() {
        frame.pack();
        frame.setVisible(true);

        onPostShow();
    }

    protected void onPostShow() {
        frame.setMinimumSize(frame.getSize());
    }


    public JFrame getFrame() {
        return frame;
    }

    public JPanel getContainer() {
        return container;
    }
}