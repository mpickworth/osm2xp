package com.osm2xp.gui.views.panels;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Widget;

/**
 * Osm2xp root Panel.
 * 
 * @author Benjamin Blanchet
 * 
 */
public abstract class Osm2xpPanel extends Composite {

	protected final DataBindingContext bindingContext = new DataBindingContext();

	public Osm2xpPanel(Composite parent, int style) {
		super(parent, style);
		initComponents();
		bindComponents();
		addComponentsListeners();
		initLayout();
	}

	/**
	 * Bind a widget to a bean property
	 * 
	 * @param component
	 * @param bean
	 * @param property
	 */
	@SuppressWarnings("unchecked")
	protected void bindComponent(Widget component, Object bean, String property) {
		if (component instanceof Spinner || component instanceof Button) {
			bindingContext.bindValue(WidgetProperties.selection().observe(component),		
					PojoProperties.value(property).observe(bean));
//			bindingContext.bindValue(
//					SWTObservables.observeSelection(component),
//					PojoObservables.observeValue(bean, property));
		} else if (component instanceof Combo) {
			bindingContext.bindValue(WidgetProperties.text().observe(component),		
					PojoProperties.value(property).observe(bean));
//			bindingContext.bindValue(SWTObservables.observeText(component),
//					PojoObservables.observeValue(bean, property));
		}
	}

	/**
	 * Layout initialization .
	 */
	protected abstract void initLayout();

	/**
	 * Components initialization .
	 */
	protected abstract void initComponents();

	/**
	 * Components binding .
	 */
	protected abstract void bindComponents();

	/**
	 * Components action listeners .
	 */
	protected abstract void addComponentsListeners();
}
