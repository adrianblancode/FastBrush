package co.adrianblan.calligraphy.globject;

import java.util.ArrayList;

import co.adrianblan.calligraphy.data.TouchData;
import co.adrianblan.calligraphy.vector.Vector3;

/**
 * Class which contains the writing primitives for the brush.
 */
public class Brush {

    private ArrayList<Bristle> bristles;
    private Vector3 position;

    private static final float NUM_BRISTLES = 100;
    public static final float BRISTLE_THICKNESS = 10f;

    public Brush() {
        bristles = new ArrayList<>();
        position = new Vector3();

        for(int i = 0; i < NUM_BRISTLES; i++){
            bristles.add(new Bristle());
        }
    }

    public void draw(float[] mvpMatrix) {

        for(Bristle bristle : bristles){
            bristle.draw(mvpMatrix);
        }
    }

    public void update(TouchData touchData) {
        position.set(touchData.getPosition(), Bristle.LENGTH);

        for(Bristle bristle : bristles){
            bristle.update(position);
        }
    }
}
