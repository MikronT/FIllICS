package com.mikront.fillics.gui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class JCheckBoxList extends JScrollPane {
    public static final int
            ORIENTATION_VERTICAL = 0,
            ORIENTATION_HORIZONTAL = 1;

    private int orientation = ORIENTATION_VERTICAL;

    private final GroupLayout layout;
    private final List<JCheckBox> boxes = new ArrayList<>();


    public JCheckBoxList() {
        super();

        getVerticalScrollBar().setUnitIncrement(12);
        getHorizontalScrollBar().setUnitIncrement(8);

        JPanel container = new JPanel();
        setViewportView(container);

        layout = new GroupLayout(container);
        container.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
    }


    /**
     * Creates a new checkbox and puts it into the list layout
     *
     * @param title checkbox title and id
     */
    public void put(String title) {
        put(title, true);
    }

    /**
     * Creates a new checkbox. You can specify {@code shouldUpdateLayout = false} if you want
     * to update layout at certain moment using {@link #updateLayout()}
     *
     * @param title              checkbox title and id
     * @param shouldUpdateLayout specifies whether to notify the layout about this change
     */
    public void put(String title, boolean shouldUpdateLayout) {
        for (var box : boxes)
            if (box.getText().equals(title))
                return;

        var box = new JCheckBox(title);
        boxes.add(box);

        if (shouldUpdateLayout)
            updateLayout();
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    /**
     * Updates layout. Useful after using {@link #put(String)}
     */
    public void updateLayout() {
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

        Components.applyDefaults(this);
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