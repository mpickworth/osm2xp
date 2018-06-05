package com.onpositive.facadeviewer.ui;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

public class SimpleListCellRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 1L;

	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, (Object) null, index, isSelected, cellHasFocus);
		this.setOpaque(true);
		this.setIcon(this.buildIcon(list, value, index, isSelected, cellHasFocus));
		this.setText(this.buildText(list, value, index, isSelected, cellHasFocus));
		this.setToolTipText(this.buildToolTipText(list, value, index, isSelected, cellHasFocus));
		if (this.accessibleContext != null) {
			this.accessibleContext
					.setAccessibleName(this.buildAccessibleName(list, value, index, isSelected, cellHasFocus));
		}

		return this;
	}

	protected Icon buildIcon(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		return this.buildIcon(value);
	}

	protected Icon buildIcon(Object value) {
		return value instanceof Icon ? (Icon) value : null;
	}

	protected String buildText(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		return this.buildText(value);
	}

	protected String buildText(Object value) {
		return value instanceof Icon ? "" : (value == null ? "" : value.toString());
	}

	protected String buildToolTipText(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		return this.buildToolTipText(value);
	}

	protected String buildToolTipText(Object value) {
		return null;
	}

	protected String buildAccessibleName(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		return this.buildAccessibleName(value);
	}

	protected String buildAccessibleName(Object value) {
		return null;
	}
}