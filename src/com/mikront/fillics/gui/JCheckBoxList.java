package com.mikront.fillics.gui;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;


public class JCheckBoxList extends JScrollPane {
    private final GroupLayout layout;
    private final Map<String, JCheckBox> boxes = new HashMap<>();


    public JCheckBoxList() {
        super();

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
     * @param key   checkbox identifier
     * @param title checkbox title
     */
    public void add(String key, String title) {
        add(key, title, true);
    }

    /**
     * Creates a new checkbox. You can specify {@code shouldUpdateLayout = false} if you want
     * to update layout at certain moment using {@link #updateLayout()}
     *
     * @param key                checkbox identifier
     * @param title              checkbox title
     * @param shouldUpdateLayout specifies whether to notify the layout about this change
     */
    public void add(String key, String title, boolean shouldUpdateLayout) {
        if (boxes.containsKey(key))
            throw new IllegalArgumentException("Key already exists");

        var box = new JCheckBox(title);
        boxes.put(key, box);

        if (shouldUpdateLayout)
            updateLayout();
    }

    /**
     * Updates layout. Useful after using {@link #add(String, String)}
     */
    public void updateLayout() {
        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup();
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();

        boxes.forEach((key, box) -> {
            horizontalGroup.addComponent(box);
            verticalGroup.addComponent(box);
        });

        layout.setHorizontalGroup(horizontalGroup);
        layout.setVerticalGroup(verticalGroup);

        Components.applyDefaults(this);
    }


    public JCheckBox get(String key) {
        return boxes.get(key);
    }

    public boolean isChecked(String key) {
        return get(key).isSelected();
    }
}