package co.adrianblan.fastbrush.database;

import java.util.HashMap;

import co.adrianblan.fastbrush.globject.Bristle;

/**
 * A database which details all parameters of the brush snapshots.
 */
public class BrushSnapshotDatabase {
    HashMap<BrushKey, BristleParameters> hashMap;

    public BrushSnapshotDatabase () {
        hashMap = new HashMap<>();
        init();
    }

    /**
     * Initializes the database with default values
     */
    private void init() {
        BrushKey brushKey = new BrushKey();
        BristleParameters bristleParameters = new BristleParameters();

        // Neutral
        brushKey.set(0, 1);
        bristleParameters.set(0, 0, 1, 0);
        hashMap.put(brushKey, bristleParameters);
    }

    public BristleParameters getBristleParameters(BrushKey bk){

        return null;
    }
}
