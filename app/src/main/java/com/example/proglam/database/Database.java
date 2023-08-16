package com.example.proglam.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.proglam.FirebaseSyncUtils;
import com.example.proglam.database.utility.DAO;
import com.example.proglam.database.utility.misurazioni;
import com.example.proglam.database.utility.Selector;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper implements DAO {
    private static final String DB_NAME = "tracker";
    private static final int DB_VERSION = 1;
    private Context context;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crea la tabella "misurazioni"
        db.execSQL("CREATE TABLE misurazioni (" +
                "Codice INTEGER PRIMARY KEY AUTOINCREMENT," +
                "Categoria TEXT," +
                "Rilevazione INTEGER," +
                "Coordinate TEXT," +
                "Data TIMESTAMP" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Esegui l'upgrade del database (elimina e ricrea la tabella)
        db.execSQL("DROP TABLE IF EXISTS misurazioni");
        onCreate(db);
    }

    @Override
    public void Insert(misurazioni misurazione) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Categoria", misurazione.getCategoria());
        values.put("Rilevazione", misurazione.getRilevazione());
        values.put("Coordinate", misurazione.getCoordinate());
        values.put("Data", misurazione.getData().getTime());
        db.insert("misurazioni", null, values);

        // Ottieni i dati dal tuo database locale SQLite
        String mgrsArea = ""; // Inserisci qui il valore dell'area MGRS
        List<misurazioni> measurements = getAllMeasurementsFromDatabase(mgrsArea);

        // Esegui la sincronizzazione con il cloud
        FirebaseSyncUtils.syncMeasurements(context, measurements);
    }

    @Override
    public LiveData<List<misurazioni>> getAll(String mgrsArea) {
        return new MutableLiveData<>(getAllMeasurementsFromDatabase(mgrsArea));
    }

    @Override
    public LiveData<List<misurazioni>> getFromAreaAndType(String mgrsArea, String type) {
        return new MutableLiveData<>(getMeasurementsFromAreaAndType(mgrsArea, type));
    }

    private List<misurazioni> getAllMeasurementsFromDatabase(String mgrsArea) {
        List<misurazioni> measurements = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM misurazioni", null);
        try {
            while (cursor.moveToNext()) {
                misurazioni measurement = misurazioni.fromCursor(cursor);
                measurements.add(measurement);
            }
        } finally {
            cursor.close();
        }
        return measurements;
    }

    private List<misurazioni> getMeasurementsFromAreaAndType(String mgrsArea, String type) {
        List<misurazioni> measurements = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM misurazioni WHERE Coordinate = ? AND Categoria = ?",
                new String[]{mgrsArea, type});
        try {
            while (cursor.moveToNext()) {
                misurazioni measurement = misurazioni.fromCursor(cursor);
                measurements.add(measurement);
            }
        } finally {
            cursor.close();
        }
        return measurements;
    }

    public LiveData<List<misurazioni>> getFromAreaAndType(String area, Selector selector) {
        String type;
        if (selector.equals(Selector.UMTS)) {
            type = "UMTS";
        } else if (selector.equals(Selector.Wifi)) {
            type = "wifi";
        } else {
            type = "rumore";
        }
        return getFromAreaAndType(area, type);
    }
}
