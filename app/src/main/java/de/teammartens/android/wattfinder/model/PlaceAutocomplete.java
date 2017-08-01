package de.teammartens.android.wattfinder.model;

/**
 * Created by felix on 10.05.15.
 */

    public class PlaceAutocomplete {
        public CharSequence placeId;
        public CharSequence description;
        PlaceAutocomplete(CharSequence placeId, CharSequence description) {
            this.placeId = placeId;
            this.description = description;
        }
        @Override
        public String toString() {
            return description.toString();
        }
    }

