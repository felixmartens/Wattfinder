package de.teammartens.android.wattfinder.model;

/**
 * Created by felix on 28.06.15.
 */
public class FilterEintrag implements Comparable<FilterEintrag>{

    String titel = null;

    boolean selected = false;

    public FilterEintrag(String titel,  boolean selected) {
        super();
        this.titel = titel;

        this.selected = selected;
    }

    public String getTitel() {
        return titel;
    }
    public void setTitel(String titel) {
        this.titel = titel;
    }



    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int compareTo(FilterEintrag fE) {
        return this.titel.toLowerCase().compareTo(fE.getTitel().toLowerCase());
    }
}
