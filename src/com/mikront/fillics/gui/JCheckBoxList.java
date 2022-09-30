package com.mikront.fillics.gui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class JCheckBoxList {
    public static final int ORIENTATION_VERTICAL = 0, ORIENTATION_HORIZONTAL = 1;

    private boolean shouldRefreshLayout = false; //Do not refresh at start
    private int orientation = ORIENTATION_VERTICAL;

    private final GroupLayout layout;
    private final JScrollPane pane;
    private final List<JCheckBox> boxes = new ArrayList<>();


    public JCheckBoxList() {
        super();

        pane = new JScrollPane();
        pane.getVerticalScrollBar().setUnitIncrement(12);
        pane.getHorizontalScrollBar().setUnitIncrement(8);

        JPanel container = new JPanel();
        pane.setViewportView(container);

        layout = new GroupLayout(container);
        container.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
    }


    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }


    public void add(List<String> titles, boolean checked) {
        shouldRefreshLayout = false; //Disable refreshing

        titles.forEach(s -> add(s, checked));

        shouldRefreshLayout = true; //Re-enable it
        refresh();
    }

    public void add(String title, boolean checked) {
        for (var box : boxes)
            if (box.getText().equals(title))
                return;

        var box = new JCheckBox(title, checked);
        boxes.add(box);


        //Refresh only after pushing to UI
        if (shouldRefreshLayout)
            refresh();
    }


    private void refresh() {

        GroupLayout.Group horizontalGroup, verticalGroup;
        if (orientation == ORIENTATION_VERTICAL) {
            horizontalGroup = layout.createParallelGroup();
            verticalGroup = layout.createSequentialGroup();
        } else {
            horizontalGroup = layout.createSequentialGroup();
            verticalGroup = layout.createParallelGroup();
        }

        boxes.forEach(box -> {
            horizontalGroup.addComponent(box);
            verticalGroup.addComponent(box);
        });

        layout.setHorizontalGroup(horizontalGroup);
        layout.setVerticalGroup(verticalGroup);

        Components.applyDefaults(pane);
    }

    public JScrollPane make() {
        if (!shouldRefreshLayout) { //Refresh after all the checkboxes were added
            shouldRefreshLayout = true;
            refresh();
        }
        return pane;
    }


    public JCheckBox get(String title) {
        for (var box : boxes)
            if (box.getText().equals(title))
                return box;
        return null;
    }

    public boolean isChecked(String title) {
        return get(title).isSelected();
    }
}