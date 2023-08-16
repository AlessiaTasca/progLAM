package com.example.proglam;

import android.content.Context;
import android.util.Log;

import com.example.proglam.database.utility.misurazioni;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FirebaseSyncUtils {
    private static final String TAG = "FirebaseSyncUtils";
    private static final String COLLECTION_NAME = "measurements";

    public static void syncMeasurements(Context context, List<misurazioni> measurements) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firestore.collection(COLLECTION_NAME);

        for (misurazioni measurement : measurements) {
            collectionRef.add(measurement)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Misurazione sincronizzata con successo: " + documentReference.getId());
                        // Puoi eseguire altre operazioni se necessario
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Errore durante la sincronizzazione della misurazione", e);
                        // Gestisci l'errore in base alle tue esigenze
                    });
        }
    }
}

