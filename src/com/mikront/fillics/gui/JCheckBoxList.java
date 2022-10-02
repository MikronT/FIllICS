package com.mikront.fillics.gui;

import com.mikront.util.Utils;
import com.mikront.util.debug.Log;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class JCheckBoxList extends JScrollPane {
    public static final int ORIENTATION_VERTICAL = 0, ORIENTATION_HORIZONTAL = 1;

    private boolean newItemChecked = false;
    private int orientation = ORIENTATION_VERTICAL;

    private OnItemCheckedListener onItemCheckedListener;
    private final List<JCheckBox> boxes = new ArrayList<>();


    public JCheckBoxList() {
        super();

        getVerticalScrollBar().setUnitIncrement(12);
        getHorizontalScrollBar().setUnitIncrement(8);
    }

    public JCheckBoxList(boolean newItemChecked) {
        this();

        setNewItemChecked(newItemChecked);
    }


    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public interface OnItemCheckedListener {
        void itemChecked(String title, boolean checked);
    }

    public void setOnItemCheckedListener(OnItemCheckedListener onItemCheckedListener) {
        this.onItemCheckedListener = onItemCheckedListener;
    }


    public void add(String title) {
        if (create(title))
            buildUI();
    }

    public void add(List<String> titles) {
        boolean shouldRebuildUI = false;

        for (String s : titles)
            if (create(s))
                shouldRebuildUI = true;

        if (shouldRebuildUI)
            buildUI();
    }

    private boolean create(String title) {
        for (var box : boxes)
            if (box.getText().equals(title))
                return false;

        var box = new JCheckBox(title, newItemChecked);
        boxes.add(box);

        if (onItemCheckedListener != null)
            box.addActionListener(e -> onItemCheckedListener.itemChecked(box.getText(), box.isSelected()));
        return true;
    }

    public void remove(String title) {
        if (destroy(title))
            buildUI();
    }

    public void remove(List<String> titles) {
        boolean shouldRebuildUI = false;

        for (String s : titles)
            if (destroy(s))
                shouldRebuildUI = true;

        if (shouldRebuildUI)
            buildUI();
    }

    private boolean destroy(String title) {
        return boxes.remove(get(title));
    }

    public void clear() {
        boxes.clear();
        buildUI();
    }

    public void replaceWith(List<String> newList) {
        DiffUtil.oldList(this).newList(newList).applyChanges();
    }

    private static class DiffUtil {
        private List<String> newList;
        private final JCheckBoxList checkBoxes;
        private final List<String> oldList;


        private DiffUtil(JCheckBoxList checkBoxes) {
            this.checkBoxes = checkBoxes;
            this.oldList = checkBoxes.getOldList();
        }

        public static DiffUtil oldList(JCheckBoxList checkBoxes) {
            return new DiffUtil(checkBoxes);
        }

        public DiffUtil newList(List<String> newList) {
            this.newList = newList.stream()
                    .sorted(Utils.COLLATOR)
                    .distinct()
                    .toList();
            return this;
        }

        public void applyChanges() {
            List<String> removed = new ArrayList<>(oldList);
            removed.removeAll(newList);
            Log.v("JCheckBoxList::DiffUtil::commit: removed = " + removed);
            removed.forEach(checkBoxes::destroy);

            List<String> added = new ArrayList<>(newList);
            added.removeAll(oldList);
            Log.v("JCheckBoxList::DiffUtil::commit: added = " + added);
            added.forEach(checkBoxes::create);

            if (!removed.isEmpty() || !added.isEmpty())
                checkBoxes.buildUI();
        }
    }


    public void setChecked(String title, boolean checked) {
        get(title).setSelected(checked);
    }

    public void setNewItemChecked(boolean value) {
        newItemChecked = value;
    }


    private void buildUI() {
        EventQueue.invokeLater(buildUI_runnable);
    }

    private final Runnable buildUI_runnable = () -> {
        JPanel panel = new JPanel();
        setViewportView(panel);

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

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
    };


    public JCheckBox get(String title) {
        for (var box : boxes)
            if (box.getText().equalsIgnoreCase(title))
                return box;
        return null;
    }

    private List<String> getOldList() {
        return boxes.stream()
                .map(AbstractButton::getText)
                .toList();
    }

    public boolean isChecked(String title) {
        var item = get(title);
        return item != null && item.isSelected();
    }
}