package net.yapbam.gui.dialogs;

import net.yapbam.data.Category;
import net.yapbam.data.GlobalData;
import net.yapbam.data.event.CategoryAddedEvent;
import net.yapbam.data.event.CategoryPropertyChangedEvent;
import net.yapbam.data.event.CategoryRemovedEvent;
import net.yapbam.data.event.DataEvent;
import net.yapbam.data.event.DataListener;

import net.yapbam.gui.LocalizationData;
import net.yapbam.gui.widget.AbstractSelector;

@SuppressWarnings("serial")
/** A category selector widget. */
public class CategoryWidget extends AbstractSelector<Category, GlobalData> {
	public static final String CATEGORY_PROPERTY = "category"; //$NON-NLS-1$
	
	public CategoryWidget(GlobalData data) {
		super(data);
		if (data!=null) {
			data.addListener(new DataListener() {
				@Override
				public void processEvent(DataEvent event) {
					if ((event instanceof CategoryAddedEvent) || (event instanceof CategoryRemovedEvent)) {
						refresh();
					} else if ((event instanceof CategoryPropertyChangedEvent) && ((CategoryPropertyChangedEvent)event).getProperty().equals(CategoryPropertyChangedEvent.NAME)) {
						refresh();
					}
				}
			});
		}
	}

	@Override
	protected String getLabel() {
		return LocalizationData.get("TransactionDialog.category"); //$NON-NLS-1$
	}
	
	@Override
	protected String getComboTip() {
		return LocalizationData.get("TransactionDialog.category.tooltip"); //$NON-NLS-1$
	}
	
	@Override
	protected String getNewButtonTip() {
		return LocalizationData.get("TransactionDialog.category.new.tooltip"); //$NON-NLS-1$
	}

	@Override
	protected String getPropertyName() {
		return CATEGORY_PROPERTY;
	}

	@Override
	protected void populateCombo() {
		if (getParameters()!=null) {
			for (int i = 0; i < getParameters().getCategoriesNumber(); i++) {
				getCombo().addItem(getParameters().getCategory(i));
			}
		}
	}
		
	@Override
	protected Object getDefaultRenderedValue(Category category) {
		return category==null ? category : category.getName();
	}

	@Override
	protected Category createNew() {
		if (getParameters()!=null) {
			return CategoryDialog.open(getParameters(), CategoryDialog.getOwnerWindow(this), null);
		} else {
			return null;
		}
	}
}
