package name.abuchen.portfolio.ui.wizards.security;

import java.util.HashSet;
import java.util.Set;

import name.abuchen.portfolio.model.AttributeType;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.ui.PortfolioPlugin;
import name.abuchen.portfolio.ui.util.BindingHelper;
import name.abuchen.portfolio.ui.util.LabelOnly;
import name.abuchen.portfolio.ui.wizards.security.EditSecurityModel.AttributeDesignation;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

public class AttributesPage extends AbstractPage implements IMenuListener
{
    private static final class ToAttributeStringConverter implements IConverter
    {
        private final AttributeDesignation attribute;

        private ToAttributeStringConverter(AttributeDesignation attribute)
        {
            this.attribute = attribute;
        }

        @Override
        public Object getToType()
        {
            return String.class;
        }

        @Override
        public Object getFromType()
        {
            return Object.class;
        }

        @Override
        public Object convert(Object fromObject)
        {
            return attribute.getType().getConverter().toString(fromObject);
        }
    }

    private static final class ToAttributeObjectConverter implements IConverter
    {
        private final AttributeDesignation attribute;

        private ToAttributeObjectConverter(AttributeDesignation attribute)
        {
            this.attribute = attribute;
        }

        @Override
        public Object getToType()
        {
            return Object.class;
        }

        @Override
        public Object getFromType()
        {
            return String.class;
        }

        @Override
        public Object convert(Object fromObject)
        {
            return attribute.getType().getConverter().fromString((String) fromObject);
        }
    }

    private final EditSecurityModel model;
    private final BindingHelper bindings;

    private Composite attributeContainer;
    private Menu menu;

    public AttributesPage(EditSecurityModel model, BindingHelper bindings)
    {
        this.model = model;
        this.bindings = bindings;
        setTitle(Messages.EditWizardAttributesTitle);
    }

    @Override
    public void createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NULL);
        setControl(composite);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(composite);

        attributeContainer = new Composite(composite, SWT.NULL);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(attributeContainer);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(5, 5).applyTo(attributeContainer);

        for (AttributeDesignation attribute : model.getAttributes())
            addAttributeBlock(attributeContainer, attribute);

        // add button
        final Button addButton = new Button(composite, SWT.PUSH);
        addButton.setImage(PortfolioPlugin.image(PortfolioPlugin.IMG_ADD));
        addButton.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                showAdditionalAttributes();
            }
        });

        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(addButton);

        parent.addDisposeListener(new DisposeListener()
        {
            @Override
            public void widgetDisposed(DisposeEvent e)
            {
                if (menu != null && !menu.isDisposed())
                    menu.dispose();
            }
        });
    }

    private void addAttributeBlock(Composite container, final AttributeDesignation attribute)
    {
        // label
        final Label label = new Label(container, SWT.NONE);
        label.setText(attribute.getType().getName());

        // input
        final Text value = new Text(container, SWT.BORDER);
        value.setText(attribute.getType().getConverter().toString(attribute.getValue()));
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(value);

        // delete button
        final Button deleteButton = new Button(container, SWT.PUSH);
        deleteButton.setImage(PortfolioPlugin.image(PortfolioPlugin.IMG_REMOVE));

        // model binding
        final Binding binding = bindings.getBindingContext().bindValue(
                        SWTObservables.observeText(value, SWT.Modify),
                        BeansObservables.observeValue(attribute, "value"), //$NON-NLS-1$
                        new UpdateValueStrategy().setConverter(new ToAttributeObjectConverter(attribute)),
                        new UpdateValueStrategy().setConverter(new ToAttributeStringConverter(attribute)));

        // delete selection listener
        deleteButton.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                model.getAttributes().remove(attribute);
                bindings.getBindingContext().removeBinding(binding);

                Composite parent = deleteButton.getParent();
                label.dispose();
                value.dispose();
                deleteButton.dispose();
                parent.getParent().layout(true);
            }
        });
    }

    protected void showAdditionalAttributes()
    {
        if (menu == null)
        {
            MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
            menuMgr.setRemoveAllWhenShown(true);
            menuMgr.addMenuListener(this);

            menu = menuMgr.createContextMenu(getShell());
        }

        menu.setVisible(true);
    }

    @Override
    public void menuAboutToShow(IMenuManager manager)
    {
        manager.add(new LabelOnly(Messages.LabelAvailableAttributes));

        Set<AttributeType> existing = new HashSet<AttributeType>();
        for (AttributeDesignation d : model.getAttributes())
            existing.add(d.getType());

        model.getClient() //
                        .getSettings() //
                        .getAttributeTypes() //
                        .filter(a -> !existing.contains(a)) //
                        .filter(a -> a.supports(Security.class)) //
                        .forEach(attribute -> addMenu(manager, attribute));
    }

    private void addMenu(IMenuManager manager, final AttributeType attribute)
    {
        manager.add(new Action(attribute.getName())
        {
            @Override
            public void run()
            {
                AttributeDesignation a = new AttributeDesignation(attribute, null);
                model.getAttributes().add(a);
                addAttributeBlock(attributeContainer, a);
                attributeContainer.getParent().layout(true);
            }
        });
    }
}
