package name.abuchen.portfolio.ui;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.ui.util.ProgressMonitorFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class ClientProgressProvider extends ProgressProvider
{
    private class MonitorImpl implements IProgressMonitor
    {
        @Override
        public void beginTask(final String name, int totalWork)
        {
            internalSetText(name);
        }

        @Override
        public void done()
        {
            internalSetText(""); //$NON-NLS-1$
        }

        @Override
        public void internalWorked(double work)
        {}

        @Override
        public boolean isCanceled()
        {
            return false;
        }

        @Override
        public void setCanceled(boolean value)
        {}

        @Override
        public void setTaskName(String name)
        {
            internalSetText(name);
        }

        @Override
        public void subTask(final String name)
        {
            internalSetText(name);
        }

        @Override
        public void worked(int work)
        {}

        private void internalSetText(final String text)
        {
            sync.asyncExec(new Runnable()
            {
                @Override
                public void run()
                {
                    if (!label.isDisposed())
                        label.setText(text);
                }
            });
        }
    }

    @Inject
    private Client client;

    @Inject
    private ProgressMonitorFactory factory;

    @Inject
    private UISynchronize sync;

    private CLabel label;

    @PostConstruct
    public void setup()
    {
        factory.addProgressProvider(this);
    }

    protected void disposed()
    {
        factory.removeProgressProvider(this);
    }

    @PostConstruct
    public void createComposite(Composite parent) throws IOException
    {
        final Color backgroundColor = new Color(null, 233, 241, 248);

        label = new CLabel(parent, SWT.LEFT);
        label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
        label.setBackground(new Color[] { backgroundColor, Display.getDefault().getSystemColor(SWT.COLOR_WHITE) },
                        new int[] { 100 });
        label.setText(""); //$NON-NLS-1$

        parent.addDisposeListener(new DisposeListener()
        {
            @Override
            public void widgetDisposed(DisposeEvent e)
            {
                disposed();
                backgroundColor.dispose();
            }
        });
    }

    @Override
    public IProgressMonitor createMonitor(Job job)
    {
        if (job.belongsTo(client))
        {
            final MonitorImpl monitor = new MonitorImpl();
            job.addJobChangeListener(new JobChangeAdapter()
            {
                @Override
                public void done(IJobChangeEvent event)
                {
                    monitor.done();
                }
            });
            return monitor;
        }
        else
        {
            return null;
        }
    }

    public Control getControl()
    {
        return label;
    }
}
