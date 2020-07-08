package de.braincooler.gwhelper.model;

import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import java.util.List;

public class SektorHtmlTablePair {
    private Sektor sektor;
    private List<HtmlTableRow> htmlTableRows;

    public SektorHtmlTablePair(Sektor sektor, List<HtmlTableRow> htmlTableRows) {
        this.sektor = sektor;
        this.htmlTableRows = htmlTableRows;
    }

    public Sektor getSektor() {
        return sektor;
    }

    public void setSektor(Sektor sektor) {
        this.sektor = sektor;
    }

    public List<HtmlTableRow> getHtmlTableRows() {
        return htmlTableRows;
    }

    public void setHtmlTableRows(List<HtmlTableRow> htmlTableRows) {
        this.htmlTableRows = htmlTableRows;
    }
}
