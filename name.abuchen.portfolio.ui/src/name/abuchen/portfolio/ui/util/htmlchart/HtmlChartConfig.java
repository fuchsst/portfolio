package name.abuchen.portfolio.ui.util.htmlchart;


/**
 * Implementation of interface is passed as parameter to
 * {@link HtmlChart.createControl}, defining the appearance of the chart
 * 
 * @author fuchsst
 */
public interface HtmlChartConfig
{

    /**
     * The implementation of the interface encodes the chart configuration as a
     * Json structure that can be interpreted by the scripts contained in the
     * corresponding Html
     * 
     * @return The chart configuration as Json string
     */
    public String getJson();
    
    /**
     * The interface implementation returns a constant string that represents
     * the path to the Html that renders the chart (e.g.
     * '/META-INF/html/line_chart.html')
     * 
     * @return The URI (normally an embedded resource) to the Html that renders
     *         the chart
     */
    public String getHtmlPageUri();
}