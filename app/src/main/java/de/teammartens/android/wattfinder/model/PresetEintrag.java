package de.teammartens.android.wattfinder.model;

/**
 * Created by felix on 28.06.15.
 */
public class PresetEintrag {

    String titel = null;

    boolean selected = false;




    boolean neu = false;

    public PresetEintrag(String titel, String neuertitel, boolean selected) {
        super();
        this.titel = titel;

        this.neu = false;
    }
    public PresetEintrag(String titel, String neuertitel, boolean selected, boolean neu) {
        super();
        this.titel = titel;

        this.selected = selected;

        this.neu = neu;
    }

    public PresetEintrag(String titel, boolean selected) {
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


    public boolean isNeu() {
        return neu;
    }

    public void setNeu(boolean neu) {
        this.neu = neu;
    }

}
