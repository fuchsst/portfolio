package name.abuchen.portfolio.ui.util.htmlchart;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class HtmlChartConfigTimeline implements HtmlChartConfig
{
    private String title;
    private String interpolation = "monotone";
    private String numberFormat = "#,##0.00";
    private String numberFormatLocale = "de";
    private boolean noLegend = false;
    private Date minX = null;
    private Date maxX = null;
    private Double minY = Double.NaN;
    private Double maxY = Double.NaN;
    private HtmlChartConfigTimelineVerticalMarkerList verticalMarker;
    private List<HtmlChartConfigTimelineSeries> series = new ArrayList<HtmlChartConfigTimelineSeries>();

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getInterpolation()
    {
        return interpolation;
    }

    public void setInterpolation(String interpolation)
    {
        this.interpolation = interpolation;
    }

    public String getNumberFormat()
    {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat)
    {
        this.numberFormat = numberFormat;
    }

    public String getNumberFormatLocale()
    {
        return numberFormatLocale;
    }

    public void setNumberFormatLocale(String numberFormatLocale)
    {
        this.numberFormatLocale = numberFormatLocale;
    }

    public boolean isNoLegend()
    {
        return noLegend;
    }

    public void setNoLegend(boolean noLegend)
    {
        this.noLegend = noLegend;
    }

    public Double getMinY()
    {
        return minY;
    }

    public void setMinY(Double minY)
    {
        this.minY = minY;
    }

    public Double getMaxY()
    {
        return maxY;
    }

    public void setMaxY(Double maxY)
    {
        this.maxY = maxY;
    }

    public Date getMinX()
    {
        return minX;
    }

    public void setMinX(Date minX)
    {
        this.minX = minX;
    }

    public Date getMaxX()
    {
        return maxX;
    }

    public void setMaxX(Date maxX)
    {
        this.maxX = maxX;
    }

    public void resetX()
    {
        this.minX = null;
        this.maxX = null;
    }

    public void resetY()
    {
        this.minY = Double.NaN;
        this.maxY = Double.NaN;
    }

    public void resetXY()
    {
        this.resetX();
        this.resetY();
    }

    public void zoomInX()
    {
        // TODO: Implement zoomIn (see org.swtchartinternal.axis.AxisSet)
    };

    public void zoomInX(Double coordinate)
    {
        // TODO: Implement zoomIn (see org.swtchartinternal.axis.AxisSet)
    };

    public void zoomOutX()
    {
        // TODO: Implement zoomOut (see org.swtchartinternal.axis.AxisSet)
    }

    public void zoomInY()
    {
        // TODO: Implement zoomIn (see org.swtchartinternal.axis.AxisSet)
    };

    public void zoomInY(Double coordinate)
    {
        // TODO: Implement zoomIn (see org.swtchartinternal.axis.AxisSet)
    };

    public void zoomOutY()
    {
        // TODO: Implement zoomOut (see org.swtchartinternal.axis.AxisSet)
    }

    public void zoomInXY()
    {
        this.zoomInX();
        this.zoomInY();
    }

    public void zoomOutXY()
    {
        this.zoomOutX();
        this.zoomOutY();
    }
    
    public void scrollLeft()
    {
        // TODO: Implement scrolling
    }

    public void scrollRight()
    {
        // TODO: Implement scrolling
    }

    public void scrollUp()
    {
        // TODO: Implement scrolling
    }

    public void scrollDown()
    {
        //TODO: Implement scrolling
    }

    public HtmlChartConfigTimelineVerticalMarkerList getVerticalMarker()
    {
        return verticalMarker;
    }

    public void setVerticalMarker(HtmlChartConfigTimelineVerticalMarkerList verticalMarker)
    {
        this.verticalMarker = verticalMarker;
    }

    public List<HtmlChartConfigTimelineSeries> series()
    {
        return this.series;
    }

    @SuppressWarnings("unchecked")
    private void buildJsonSeries(JSONObject json)
    {
        JSONArray jsonList = new JSONArray();
        for (HtmlChartConfigTimelineSeries s : series)
        {
            jsonList.add(s.getJson());
        }
        json.put("series", jsonList);
    }

    @SuppressWarnings("unchecked")
    public JSONObject getJson()
    {
        JSONObject json = new JSONObject();
        json.put("title", title);
        json.put("interpolation", interpolation);
        json.put("numberFormat", numberFormat);
        json.put("numberFormatLocale", numberFormatLocale);
        json.put("noLegend", noLegend);

        if (verticalMarker != null)
            json.put("verticalMarker", verticalMarker.getJson());

        if (minX != null)
            json.put("minX", minX);

        if (maxX != null)
            json.put("maxX", maxX);

        if (!Double.isNaN(minY))
            json.put("minY", minY);

        if (!Double.isNaN(maxY))
            json.put("maxY", maxY);

        buildJsonSeries(json);
        return json;
    }

    public String getJsonString()
    {
        return this.getJson().toString();
    }

    public String getHtmlPageUri()
    {
        return "/META-INF/html/line_chart.html";
    };

}
