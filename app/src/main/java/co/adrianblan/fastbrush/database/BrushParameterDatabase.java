package co.adrianblan.fastbrush.database;

/**
 * Native database for brush values.
 */
public class BrushParameterDatabase {
    public BrushKey[] brushKeys;
    public BristleParameters[] bristleParameters;
    public int size;

    private int capacity;

    public BrushParameterDatabase(int capacity) {
        this.capacity = capacity;
        brushKeys = new BrushKey[capacity];
        bristleParameters = new BristleParameters[capacity];
    }

    public void put(BrushKey brushKey, BristleParameters bristleParameter) {
        if(size < capacity) {
            brushKeys[size] = brushKey;
            bristleParameters[size] = bristleParameter;
            size++;
        }
    }

    public BristleParameters get(BrushKey brushKey) {
        for(int i = 0; i < size; i++) {
            if(brushKeys[i].equals(brushKey)) {
                return bristleParameters[i];
            }
        }

        return null;
    }

    public BrushKey getKey(int index) {
        if(index < size) {
            return brushKeys[index];
        } else {
            return null;
        }
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return (size == 0);
    }
}
