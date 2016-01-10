package name.abuchen.portfolio.ui.dialogs.transactions;

import static name.abuchen.portfolio.ui.util.FormDataFactory.startingWith;
import static name.abuchen.portfolio.ui.util.SWTHelper.amountWidth;
import static name.abuchen.portfolio.ui.util.SWTHelper.currencyWidth;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import name.abuchen.portfolio.model.BuySellEntry;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.Portfolio;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.TransactionPair;
import name.abuchen.portfolio.money.CurrencyUnit;
import name.abuchen.portfolio.money.ExchangeRateProviderFactory;
import name.abuchen.portfolio.money.Values;
import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.ui.UIConstants;
import name.abuchen.portfolio.ui.dialogs.transactions.AbstractSecurityTransactionModel.Properties;
import name.abuchen.portfolio.ui.util.SimpleDateTimeSelectionProperty;

public class SecurityTransactionDialog extends AbstractTransactionDialog
{
    @Inject
    private Client client;

    @Preference(value = UIConstants.Preferences.USE_INDIRECT_QUOTATION)
    @Inject
    private boolean useIndirectQuotation = false;

    @Inject
    public SecurityTransactionDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell parentShell)
    {
        super(parentShell);
    }

    @PostConstruct
    private void createModel(ExchangeRateProviderFactory factory, PortfolioTransaction.Type type)
    {
        boolean isBuySell = type == PortfolioTransaction.Type.BUY || type == PortfolioTransaction.Type.SELL;
        AbstractSecurityTransactionModel model = isBuySell ? new BuySellModel(client, type)
                        : new SecurityDeliveryModel(client, type);
        model.setExchangeRateProviderFactory(factory);
        setModel(model);

        // set portfolio only if exactly one exists
        // (otherwise force user to choose)
        List<Portfolio> activePortfolios = client.getActivePortfolios();
        if (activePortfolios.size() == 1)
            model.setPortfolio(activePortfolios.get(0));

        List<Security> activeSecurities = client.getActiveSecurities();
        if (!activeSecurities.isEmpty())
            model.setSecurity(activeSecurities.get(0));
    }

    private AbstractSecurityTransactionModel model()
    {
        return (AbstractSecurityTransactionModel) this.model;
    }

    @Override
    protected void createFormElements(Composite editArea)
    {
        //
        // input elements
        //

        // security

        ComboInput securities = new ComboInput(editArea, Messages.ColumnSecurity);
        securities.value.setInput(including(client.getActiveSecurities(), model().getSecurity()));
        securities.bindValue(Properties.security.name(), Messages.MsgMissingSecurity);
        securities.bindCurrency(Properties.securityCurrencyCode.name());

        // portfolio + reference account

        ComboInput portfolio = new ComboInput(editArea, Messages.ColumnPortfolio);
        portfolio.value.setInput(including(client.getActivePortfolios(), model().getPortfolio()));
        portfolio.bindValue(Properties.portfolio.name(), Messages.MsgMissingPortfolio);

        ComboInput comboInput = new ComboInput(editArea, null);
        if (model() instanceof BuySellModel)
        {
            comboInput.value.setInput(including(client.getActiveAccounts(), ((BuySellModel) model()).getAccount()));
            comboInput.bindValue(Properties.account.name(), Messages.MsgMissingAccount);
        }
        else
        {
            List<CurrencyUnit> availableCurrencies = CurrencyUnit.getAvailableCurrencyUnits();
            Collections.sort(availableCurrencies);
            comboInput.value.setInput(availableCurrencies);
            comboInput.bindValue(Properties.transactionCurrency.name(), Messages.MsgMissingAccount);
        }

        // date

        Label lblDate = new Label(editArea, SWT.RIGHT);
        lblDate.setText(Messages.ColumnDate);
        DateTime valueDate = new DateTime(editArea, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);

        context.bindValue(new SimpleDateTimeSelectionProperty().observe(valueDate),
                        BeanProperties.value(Properties.date.name()).observe(model));

        // other input fields

        Input shares = new Input(editArea, Messages.ColumnShares);
        shares.bindValue(Properties.shares.name(), Messages.ColumnShares, Values.Share, true);

        Input quote = new Input(editArea, "x " + Messages.ColumnQuote); //$NON-NLS-1$
        quote.bindValue(Properties.quote.name(), Messages.ColumnQuote, Values.Quote, true);
        quote.bindCurrency(Properties.securityCurrencyCode.name());

        Input grossValue = new Input(editArea, "="); //$NON-NLS-1$
        grossValue.bindValue(Properties.grossValue.name(), Messages.ColumnSubTotal, Values.Amount, true);
        grossValue.bindCurrency(Properties.securityCurrencyCode.name());

        Input exchangeRate = new Input(editArea, useIndirectQuotation ? "/ " : "x "); //$NON-NLS-1$ //$NON-NLS-2$
        exchangeRate.bindExchangeRate(
                        useIndirectQuotation ? Properties.inverseExchangeRate.name() : Properties.exchangeRate.name(),
                        Messages.ColumnExchangeRate);
        exchangeRate.bindCurrency(useIndirectQuotation ? Properties.inverseExchangeRateCurrencies.name()
                        : Properties.exchangeRateCurrencies.name());

        model().addPropertyChangeListener(Properties.exchangeRate.name(),
                        e -> exchangeRate.value.setToolTipText(AbstractModel.createCurrencyToolTip(
                                        model().getExchangeRate(), model().getTransactionCurrencyCode(),
                                        model().getSecurityCurrencyCode())));

        final Input convertedGrossValue = new Input(editArea, "="); //$NON-NLS-1$
        convertedGrossValue.bindValue(Properties.convertedGrossValue.name(), Messages.ColumnSubTotal, Values.Amount,
                        true);
        convertedGrossValue.bindCurrency(Properties.transactionCurrencyCode.name());

        // fees

        Label plusForexFees = new Label(editArea, SWT.NONE);
        plusForexFees.setText("+"); //$NON-NLS-1$

        Input forexFees = new Input(editArea, sign() + Messages.ColumnFees);
        forexFees.bindValue(Properties.forexFees.name(), Messages.ColumnFees, Values.Amount, false);
        forexFees.bindCurrency(Properties.securityCurrencyCode.name());

        Input fees = new Input(editArea, sign() + Messages.ColumnFees);
        fees.bindValue(Properties.fees.name(), Messages.ColumnFees, Values.Amount, false);
        fees.bindCurrency(Properties.transactionCurrencyCode.name());

        // taxes

        Label plusForexTaxes = new Label(editArea, SWT.NONE);
        plusForexTaxes.setText("+"); //$NON-NLS-1$

        Input forexTaxes = new Input(editArea, sign() + Messages.ColumnTaxes);
        forexTaxes.bindValue(Properties.forexTaxes.name(), Messages.ColumnTaxes, Values.Amount, false);
        forexTaxes.bindCurrency(Properties.securityCurrencyCode.name());

        Input taxes = new Input(editArea, sign() + Messages.ColumnTaxes);
        taxes.bindValue(Properties.taxes.name(), Messages.ColumnTaxes, Values.Amount, false);
        taxes.bindCurrency(Properties.transactionCurrencyCode.name());

        // total

        String label = getTotalLabel();
        Input total = new Input(editArea, "= " + label); //$NON-NLS-1$
        total.bindValue(Properties.total.name(), label, Values.Amount, true);
        total.bindCurrency(Properties.transactionCurrencyCode.name());

        // note

        Label lblNote = new Label(editArea, SWT.LEFT);
        lblNote.setText(Messages.ColumnNote);
        Text valueNote = new Text(editArea, SWT.BORDER);
        context.bindValue(WidgetProperties.text(SWT.Modify).observe(valueNote),
                        BeanProperties.value(Properties.note.name()).observe(model));

        //
        // form layout
        //

        int width = amountWidth(grossValue.value);
        int currencyWidth = currencyWidth(grossValue.currency);

        startingWith(securities.value.getControl(), securities.label).suffix(securities.currency)
                        .thenBelow(portfolio.value.getControl()).label(portfolio.label)
                        .suffix(comboInput.value.getControl()).thenBelow(valueDate).label(lblDate)
                        // shares - quote - gross value
                        .thenBelow(shares.value).width(width).label(shares.label).thenRight(quote.label)
                        .thenRight(quote.value).width(width).thenRight(quote.currency).width(width)
                        .thenRight(grossValue.label).thenRight(grossValue.value).width(width)
                        .thenRight(grossValue.currency);

        startingWith(quote.value).thenBelow(exchangeRate.value).width(width).label(exchangeRate.label)
                        .thenRight(exchangeRate.currency).width(width);

        startingWith(grossValue.value)
                        // converted gross value
                        .thenBelow(convertedGrossValue.value).width(width).label(convertedGrossValue.label)
                        .suffix(convertedGrossValue.currency)
                        // fees
                        .thenBelow(fees.value).width(width).label(fees.label).suffix(fees.currency)
                        // taxes
                        .thenBelow(taxes.value).width(width).label(taxes.label).suffix(taxes.currency)
                        // total
                        .thenBelow(total.value).width(width).label(total.label).suffix(total.currency)
                        // note
                        .thenBelow(valueNote).left(securities.value.getControl()).right(total.value).label(lblNote);

        startingWith(fees.value).thenLeft(plusForexFees).thenLeft(forexFees.currency).width(currencyWidth)
                        .thenLeft(forexFees.value).width(width).thenLeft(forexFees.label);

        startingWith(taxes.value).thenLeft(plusForexTaxes).thenLeft(forexTaxes.currency).width(currencyWidth)
                        .thenLeft(forexTaxes.value).width(width).thenLeft(forexTaxes.label);

        //
        // hide / show exchange rate if necessary
        //

        model.addPropertyChangeListener(Properties.exchangeRateCurrencies.name(), event -> {
            String securityCurrency = model().getSecurityCurrencyCode();
            String accountCurrency = model().getTransactionCurrencyCode();

            // make exchange rate visible if both are set but different
            boolean visible = securityCurrency.length() > 0 && accountCurrency.length() > 0
                            && !securityCurrency.equals(accountCurrency);

            exchangeRate.setVisible(visible);
            convertedGrossValue.setVisible(visible);

            forexFees.setVisible(visible);
            plusForexFees.setVisible(visible);
            fees.label.setVisible(!visible);

            forexTaxes.setVisible(visible);
            plusForexTaxes.setVisible(visible);
            taxes.label.setVisible(!visible);
        });

        StockSplitWarningListener stockSplits = new StockSplitWarningListener(this);
        model.addPropertyChangeListener(Properties.security.name(),
                        e -> stockSplits.check(model().getSecurity(), model().getDate()));
        model.addPropertyChangeListener(Properties.date.name(),
                        e -> stockSplits.check(model().getSecurity(), model().getDate()));

        model.firePropertyChange(Properties.exchangeRateCurrencies.name(), "", model().getExchangeRateCurrencies()); //$NON-NLS-1$
    }

    /**
     * make sure drop-down boxes contain the security, portfolio and account of
     * this transaction (they might be "retired" and do not show by default)
     */
    private <T> List<T> including(List<T> list, T element)
    {
        if (element != null && !list.contains(element))
            list.add(0, element);
        return list;
    }

    private String sign()
    {
        switch (model().getType())
        {
            case BUY:
            case DELIVERY_INBOUND:
                return "+ "; //$NON-NLS-1$
            case SELL:
            case DELIVERY_OUTBOUND:
                return "- "; //$NON-NLS-1$
            default:
                throw new UnsupportedOperationException();
        }
    }

    private String getTotalLabel()
    {
        switch (model().getType())
        {
            case BUY:
                return Messages.ColumnDebitNote;
            case DELIVERY_INBOUND:
                return Messages.LabelValueInboundDelivery;
            case SELL:
                return Messages.ColumnCreditNote;
            case DELIVERY_OUTBOUND:
                return Messages.LabelValueOutboundDelivery;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void setPortfolio(Portfolio portfolio)
    {
        model().setPortfolio(portfolio);
    }

    @Override
    public void setSecurity(Security security)
    {
        model().setSecurity(security);
    }

    public void setBuySellEntry(BuySellEntry entry)
    {
        if (!model().accepts(entry.getPortfolioTransaction().getType()))
            throw new IllegalArgumentException();
        model().setSource(entry);
    }

    public void setDeliveryTransaction(TransactionPair<PortfolioTransaction> pair)
    {
        if (!model().accepts(pair.getTransaction().getType()))
            throw new IllegalArgumentException();
        model().setSource(pair);
    }
}