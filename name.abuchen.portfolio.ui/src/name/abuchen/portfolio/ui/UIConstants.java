package name.abuchen.portfolio.ui;

public interface UIConstants
{
    interface Part
    {
        String PORTFOLIO = "name.abuchen.portfolio.ui.part.portfolio"; //$NON-NLS-1$
        String ERROR_LOG = "name.abuchen.portfolio.ui.part.errorlog"; //$NON-NLS-1$
        String TEXT_VIEWER = "name.abuchen.portfolio.ui.part.textviewer"; //$NON-NLS-1$
    }

    interface PartStack
    {
        String MAIN = "name.abuchen.portfolio.ui.partstack.main"; //$NON-NLS-1$
    }

    interface Event
    {
        interface Log
        {
            String CREATED = "errorlog/created"; //$NON-NLS-1$
            String CLEARED = "errorlog/cleared"; //$NON-NLS-1$
        }
    }

    interface File
    {
        String ENCRYPTED_EXTENSION = "portfolio"; //$NON-NLS-1$
    }

    interface Parameter
    {
        String PART = "name.abuchen.portfolio.ui.param.part"; //$NON-NLS-1$
        String FILE = "file"; //$NON-NLS-1$
        String EXTENSION = "name.abuchen.portfolio.ui.param.extension"; //$NON-NLS-1$
        String ENCRYPTION_METHOD = "name.abuchen.portfolio.ui.param.encryptionmethod"; //$NON-NLS-1$
        String SAMPLE_FILE = "name.abuchen.portfolio.ui.param.samplefile"; //$NON-NLS-1$
    }
}
