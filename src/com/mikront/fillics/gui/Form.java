package com.mikront.fillics.gui;

import com.mikront.fillics.resource.Dimens;
import com.mikront.fillics.resource.Strings;
import com.mikront.util.Log;

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
            Log.e("Components::load: catching exception: ", e);
            return;
        }

        form.onCreate();
        EventQueue.invokeLater(form::show);
    }

    protected void onCreate() {
        frame.setTitle(APP_NAME);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setContentPane(container);

        container.setBorder(BorderFactory.createEmptyBorder(FORM_PADDING, FORM_PADDING, FORM_PADDING, FORM_PADDING));
    }

    public void show() {
        frame.pack();
        frame.setVisible(true);
    }


    public JFrame getFrame() {
        return frame;
    }

    public JPanel getContainer() {
        return container;
    }
}