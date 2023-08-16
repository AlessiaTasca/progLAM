package com.example.proglam;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.proglam.database.Database;
import com.example.proglam.database.utility.Selector;
import com.example.proglam.database.utility.misurazioni;
import java.sql.Timestamp;
import java.util.List;

public class DbConnection extends ViewModel {
    private Selector currentSelector = Selector.UMTS;
    // user location
    private Location userLocation;
    private String area;

    // user parameters
    private MutableLiveData<String> userParams;

    // db connection
    private Database db;
    private LiveData<List<misurazioni>> cacheMeasurements;

    public void connettiDb(Context context) {
        db = new Database(context);
        Log.d(TAG, "connettiDb: connessione creata!");
    }

    public LiveData<List<misurazioni>> getMeasurements() {
        if (cacheMeasurements == null) {
            if (area == null) {
                cacheMeasurements = db.getFromAreaAndType("32TPQ", currentSelector);
            } else {
                cacheMeasurements = db.getFromAreaAndType(area, currentSelector);
            }
        }
        return cacheMeasurements;
    }

    public void resetCache() {
        if (area != null) {
            cacheMeasurements = null;
        }
    }

    public void setUserLocation(Location location) {
        this.userLocation = location;
    }

    public Location getUserLocation() {
        return this.userLocation;
    }

    public void saveCurrentMeasurement(misurazioni currM, String MGRS) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        misurazioni measurement = new misurazioni(currM.getType(), currM.getValue(), MGRS, timestamp);
        db.Insert(measurement);
    }

    public Selector getCurrentSelector() {
        return currentSelector;
    }

    public void setCurrentSelector(Selector currentSelector) {
        this.currentSelector = currentSelector;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public LiveData<String> getUserParams() {
        if (userParams == null) {
            userParams = new MutableLiveData<>();
        }
        return userParams;
    }

    public void setUserParams(String params) {
        this.userParams.setValue(params);
    }
}
